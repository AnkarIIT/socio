import { createHash, randomInt, randomUUID } from 'node:crypto';
import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { JwtService } from '@nestjs/jwt';
import { unauthorized } from '../../common/errors/domain-exceptions';

export interface TokenPair {
  accessToken: string;
  refreshToken: string;
  sessionId: string;
}

/**
 * JWT access (short-lived, stateless) + rotating refresh (opaque hash stored
 * in the Session table). Access tokens never touch the DB; refresh tokens do.
 */
@Injectable()
export class TokensService {
  constructor(
    private readonly jwt: JwtService,
    private readonly config: ConfigService,
  ) {}

  async issuePair(userId: string, phone: string): Promise<TokenPair> {
    const sessionId = randomUUID();
    const accessToken = await this.jwt.signAsync(
      { sub: userId, sessionId, phone },
      {
        secret: this.config.getOrThrow<string>('JWT_ACCESS_SECRET'),
        expiresIn: this.config.get('JWT_ACCESS_TTL', '15m'),
      },
    );
    const refreshToken = await this.jwt.signAsync(
      { sub: userId, sessionId, typ: 'refresh' },
      {
        secret: this.config.getOrThrow<string>('JWT_REFRESH_SECRET'),
        expiresIn: `${this.config.get('JWT_REFRESH_TTL_DAYS', 30)}d`,
      },
    );
    return { accessToken, refreshToken, sessionId };
  }

  async verifyRefreshToken(
    token: string,
  ): Promise<{ sub: string; sessionId: string }> {
    try {
      const payload = await this.jwt.verifyAsync<{
        sub: string;
        sessionId: string;
        typ?: string;
      }>(token, {
        secret: this.config.getOrThrow<string>('JWT_REFRESH_SECRET'),
      });
      if (payload.typ !== 'refresh') throw new Error('wrong token type');
      return { sub: payload.sub, sessionId: payload.sessionId };
    } catch {
      throw unauthorized('Invalid refresh token');
    }
  }

  hashRefreshToken(token: string): string {
    return createHash('sha256').update(token).digest('hex');
  }

  generateOtp(): string {
    return String(randomInt(100_000, 1_000_000));
  }

  hashOtp(code: string): string {
    return createHash('sha256').update(code).digest('hex');
  }

  sessionExpiresAt(): Date {
    const days = Number(this.config.get('JWT_REFRESH_TTL_DAYS', 30));
    return new Date(Date.now() + days * 24 * 60 * 60 * 1000);
  }
}
