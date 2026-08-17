import { Injectable, Inject, Logger } from '@nestjs/common';
import { normalizeIndiaPhone } from '@bharat/shared';
import type {
  AuthResponse,
  RequestOtpDto,
  RefreshTokenDto,
  VerifyOtpDto,
} from '@bharat/contracts';
import { PrismaService } from '../../prisma/prisma.service';
import { RateLimiter } from '../../common/rate-limiter';
import {
  forbidden,
  rateLimited,
  serviceUnavailable,
  unauthorized,
  validationError,
} from '../../common/errors/domain-exceptions';
import { TokensService } from './tokens.service';
import { OTP_PROVIDER } from './otp/otp.provider';
import type { OtpProvider } from './otp/otp.provider';
import { toUserPublic } from '../user/user.serializer';

const OTP_TTL_MINUTES = 10;
const MAX_VERIFY_ATTEMPTS = 5;

@Injectable()
export class AuthService {
  private readonly logger = new Logger(AuthService.name);

  constructor(
    private readonly prisma: PrismaService,
    private readonly tokens: TokensService,
    private readonly rateLimiter: RateLimiter,
    @Inject(OTP_PROVIDER) private readonly otpProvider: OtpProvider,
  ) {}

  async requestOtp(
    dto: RequestOtpDto,
    ip: string,
  ): Promise<{ sent: boolean; retryAfterSeconds: number }> {
    const phone = normalizeIndiaPhone(dto.phone);
    if (!phone) throw validationError('Enter a valid Indian phone number');

    const limited = await this.rateLimiter.isRateLimited(
      `otp:req:${phone}`,
      5,
      60 * 60,
    );
    if (limited)
      throw rateLimited(
        'Too many OTP requests for this number, try again later',
      );
    const ipLimited = await this.rateLimiter.isRateLimited(
      `otp:req:ip:${ip}`,
      20,
      60 * 60,
    );
    if (ipLimited) throw rateLimited('Too many requests from this device');

    const code = this.tokens.generateOtp();
    await this.prisma.otpChallenge.create({
      data: {
        phone,
        codeHash: this.tokens.hashOtp(code),
        expiresAt: new Date(Date.now() + OTP_TTL_MINUTES * 60 * 1000),
      },
    });

    try {
      await this.otpProvider.send(phone, code);
    } catch (err) {
      this.logger.error(`OTP send failed: ${(err as Error).message}`);
      throw serviceUnavailable('Could not send the OTP, please try again');
    }

    return { sent: true, retryAfterSeconds: 30 };
  }

  async verifyOtp(dto: VerifyOtpDto, ip: string): Promise<AuthResponse> {
    const phone = normalizeIndiaPhone(dto.phone);
    if (!phone) throw validationError('Enter a valid Indian phone number');

    const limited = await this.rateLimiter.isRateLimited(
      `otp:verify:${phone}`,
      MAX_VERIFY_ATTEMPTS,
      10 * 60,
    );
    if (limited) throw rateLimited('Too many attempts, request a new code');

    const challenge = await this.prisma.otpChallenge.findFirst({
      where: { phone, verifiedAt: null },
      orderBy: { createdAt: 'desc' },
    });
    if (!challenge) throw validationError('No OTP found, request a new code');
    if (challenge.expiresAt < new Date())
      throw validationError('OTP expired, request a new code');
    if (challenge.attempts >= MAX_VERIFY_ATTEMPTS)
      throw rateLimited('Too many attempts, request a new code');

    if (this.tokens.hashOtp(dto.code) !== challenge.codeHash) {
      await this.prisma.otpChallenge.update({
        where: { id: challenge.id },
        data: { attempts: { increment: 1 } },
      });
      throw validationError('Incorrect code, please try again');
    }

    await this.prisma.otpChallenge.update({
      where: { id: challenge.id },
      data: { verifiedAt: new Date() },
    });

    let user = await this.prisma.user.findUnique({ where: { phone } });
    if (!user) {
      user = await this.prisma.user.create({
        data: {
          phone,
          name: `Bharat User ${phone.slice(-4)}`,
          username: await this.generateUniqueUsername(phone),
        },
      });
    }
    if (user.bannedAt) throw forbidden('Account suspended');

    const pair = await this.tokens.issuePair(user.id, user.phone);
    await this.prisma.session.create({
      data: {
        id: pair.sessionId,
        userId: user.id,
        refreshTokenHash: this.tokens.hashRefreshToken(pair.refreshToken),
        expiresAt: this.tokens.sessionExpiresAt(),
        ip,
      },
    });

    return { ...pair, user: toUserPublic(user) };
  }

  async refresh(dto: RefreshTokenDto): Promise<AuthResponse> {
    const payload = await this.tokens.verifyRefreshToken(dto.refreshToken);

    const session = await this.prisma.session.findUnique({
      where: { id: payload.sessionId },
      include: { user: true },
    });
    if (!session || session.revokedAt || session.expiresAt < new Date()) {
      throw unauthorized('Session expired, please log in again');
    }
    if (
      session.refreshTokenHash !==
      this.tokens.hashRefreshToken(dto.refreshToken)
    ) {
      throw unauthorized('Refresh token was revoked');
    }
    if (session.user.bannedAt) throw forbidden('Account suspended');

    await this.prisma.session.update({
      where: { id: session.id },
      data: { revokedAt: new Date() },
    });

    const pair = await this.tokens.issuePair(
      session.user.id,
      session.user.phone,
    );
    await this.prisma.session.create({
      data: {
        id: pair.sessionId,
        userId: session.user.id,
        refreshTokenHash: this.tokens.hashRefreshToken(pair.refreshToken),
        expiresAt: this.tokens.sessionExpiresAt(),
      },
    });

    return { ...pair, user: toUserPublic(session.user) };
  }

  async logout(sessionId: string): Promise<{ ok: true }> {
    await this.prisma.session.update({
      where: { id: sessionId },
      data: { revokedAt: new Date() },
    });
    return { ok: true };
  }

  private async generateUniqueUsername(phone: string): Promise<string> {
    const base = `user${phone.slice(-4)}`;
    for (let i = 0; i < 20; i++) {
      const candidate = i === 0 ? base : `${base}${i + 1}`;
      const exists = await this.prisma.user.findUnique({
        where: { username: candidate },
      });
      if (!exists) return candidate;
    }
    throw serviceUnavailable('Could not generate a username, please try again');
  }
}
