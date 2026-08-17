import { CanActivate, ExecutionContext, Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { Reflector } from '@nestjs/core';
import { JwtService } from '@nestjs/jwt';
import type { Request } from 'express';
import { IS_PUBLIC_KEY } from '../decorators/public.decorator';
import { unauthorized } from '../errors/domain-exceptions';

export interface AuthenticatedUser {
  userId: string;
  sessionId: string;
  phone: string;
}

interface AccessTokenPayload {
  sub: string;
  sessionId: string;
  phone: string;
}

/** Global guard: verifies the Bearer access token, or skips @Public() routes. */
@Injectable()
export class JwtAuthGuard implements CanActivate {
  constructor(
    private readonly jwt: JwtService,
    private readonly reflector: Reflector,
    private readonly config: ConfigService,
  ) {}

  canActivate(context: ExecutionContext): boolean {
    const isPublic = this.reflector.getAllAndOverride<boolean>(IS_PUBLIC_KEY, [
      context.getHandler(),
      context.getClass(),
    ]);
    if (isPublic) return true;

    const request = context
      .switchToHttp()
      .getRequest<Request & { user?: AuthenticatedUser }>();
    const header = request.headers['authorization'];
    if (!header || !header.startsWith('Bearer ')) {
      throw unauthorized('Missing access token');
    }

    const token = header.slice('Bearer '.length);
    try {
      const payload = this.jwt.verify<AccessTokenPayload>(token, {
        secret: this.config.getOrThrow<string>('JWT_ACCESS_SECRET'),
      });
      request.user = {
        userId: payload.sub,
        sessionId: payload.sessionId,
        phone: payload.phone,
      };
      return true;
    } catch {
      throw unauthorized('Invalid or expired access token');
    }
  }
}
