import { Body, Controller, HttpCode, Ip, Post } from '@nestjs/common';
import {
  RefreshTokenSchema,
  RequestOtpSchema,
  VerifyOtpSchema,
} from '@bharat/contracts';
import type {
  AuthResponse,
  RefreshTokenDto,
  RequestOtpDto,
  VerifyOtpDto,
} from '@bharat/contracts';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { Public } from '../../common/decorators/public.decorator';
import type { AuthenticatedUser } from '../../common/guards/jwt-auth.guard';
import { ZodValidationPipe } from '../../common/pipes/zod-validation.pipe';
import { AuthService } from './auth.service';

@Controller('auth')
export class AuthController {
  constructor(private readonly auth: AuthService) {}

  @Public()
  @Post('otp/request')
  @HttpCode(200)
  requestOtp(
    @Body(new ZodValidationPipe(RequestOtpSchema)) dto: RequestOtpDto,
    @Ip() ip: string,
  ): Promise<{ sent: boolean; retryAfterSeconds: number }> {
    return this.auth.requestOtp(dto, ip ?? 'unknown');
  }

  @Public()
  @Post('otp/verify')
  @HttpCode(200)
  verifyOtp(
    @Body(new ZodValidationPipe(VerifyOtpSchema)) dto: VerifyOtpDto,
    @Ip() ip: string,
  ): Promise<AuthResponse> {
    return this.auth.verifyOtp(dto, ip ?? 'unknown');
  }

  @Public()
  @Post('refresh')
  @HttpCode(200)
  refresh(
    @Body(new ZodValidationPipe(RefreshTokenSchema)) dto: RefreshTokenDto,
  ): Promise<AuthResponse> {
    return this.auth.refresh(dto);
  }

  @Post('logout')
  @HttpCode(200)
  logout(@CurrentUser() user: AuthenticatedUser): Promise<{ ok: true }> {
    return this.auth.logout(user.sessionId);
  }
}
