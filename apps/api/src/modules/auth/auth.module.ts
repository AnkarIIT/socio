import { Module } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { RateLimiter } from '../../common/rate-limiter';
import { AuthController } from './auth.controller';
import { AuthService } from './auth.service';
import {
  DevOtpProvider,
  Msg91OtpProvider,
  OTP_PROVIDER,
  OtpProvider,
} from './otp/otp.provider';
import { TokensService } from './tokens.service';

@Module({
  controllers: [AuthController],
  providers: [
    AuthService,
    TokensService,
    RateLimiter,
    {
      provide: OTP_PROVIDER,
      useFactory: (config: ConfigService): OtpProvider => {
        const provider = config.get<string>('OTP_PROVIDER', 'dev');
        switch (provider) {
          case 'msg91':
            return new Msg91OtpProvider();
          case 'dev':
          default:
            return new DevOtpProvider();
        }
      },
      inject: [ConfigService],
    },
  ],
  exports: [TokensService],
})
export class AuthModule {}
