import {
  ArgumentsHost,
  Catch,
  ExceptionFilter,
  HttpException,
  HttpStatus,
  Logger,
} from '@nestjs/common';
import { Request, Response } from 'express';
import { ErrorCode, ErrorEnvelope } from '@bharat/shared';
import { Prisma } from '../../generated/prisma/client';
import { DomainException } from '../errors/domain-exceptions';
import { requestContext } from '../request-context';

/**
 * Global exception filter — every failure leaves the process through this
 * filter as the documented error envelope: { error: { code, message, details, requestId } }.
 */
@Catch()
export class GlobalExceptionFilter implements ExceptionFilter {
  private readonly logger = new Logger(GlobalExceptionFilter.name);

  catch(exception: unknown, host: ArgumentsHost): void {
    const ctx = host.switchToHttp();
    const response = ctx.getResponse<Response>();
    const request = ctx.getRequest<Request>();
    const requestId =
      requestContext.getStore()?.requestId ??
      (request.headers['x-request-id'] as string) ??
      'unknown';

    const mapped = this.map(exception);

    if (mapped.status >= 500) {
      this.logger.error(
        `[${requestId}] ${request.method} ${request.url} -> ${mapped.code} ${mapped.message}`,
        exception instanceof Error ? exception.stack : String(exception),
      );
    } else {
      this.logger.warn(
        `[${requestId}] ${request.method} ${request.url} -> ${mapped.status} ${mapped.code}`,
      );
    }

    const envelope: ErrorEnvelope = {
      error: {
        code: mapped.code,
        message: mapped.message,
        details: mapped.details,
        requestId,
      },
    };
    response.status(mapped.status).json(envelope);
  }

  private map(exception: unknown): {
    code: ErrorCode;
    status: number;
    message: string;
    details?: Record<string, unknown>;
  } {
    if (exception instanceof DomainException) {
      return {
        code: exception.code,
        status: exception.httpStatus,
        message: exception.message,
        details: exception.details,
      };
    }

    if (exception instanceof HttpException) {
      const status = exception.getStatus();
      const body = exception.getResponse();
      if (typeof body === 'string') {
        return { code: httpStatusToCode(status), status, message: body };
      }
      const record = body as Record<string, unknown>;
      const message =
        typeof record.message === 'string'
          ? record.message
          : JSON.stringify(record.message);
      const { message: _omitted, ...details } = record;
      void _omitted;
      return { code: httpStatusToCode(status), status, message, details };
    }

    if (exception instanceof Prisma.PrismaClientKnownRequestError) {
      switch (exception.code) {
        case 'P2002':
          return {
            code: ErrorCode.CONFLICT,
            status: 409,
            message: 'This already exists',
          };
        case 'P2025':
          return {
            code: ErrorCode.NOT_FOUND,
            status: 404,
            message: 'Resource not found',
          };
        case 'P2003':
          return {
            code: ErrorCode.VALIDATION,
            status: 400,
            message: 'Referenced resource does not exist',
          };
        case 'P2034':
          return {
            code: ErrorCode.SERVICE_UNAVAILABLE,
            status: 503,
            message: 'Write conflict, please retry',
          };
        default:
          return {
            code: ErrorCode.INTERNAL,
            status: 500,
            message: 'Internal server error',
          };
      }
    }

    if (exception instanceof Prisma.PrismaClientValidationError) {
      return {
        code: ErrorCode.VALIDATION,
        status: 400,
        message: 'Invalid request payload',
      };
    }

    return {
      code: ErrorCode.INTERNAL,
      status: HttpStatus.INTERNAL_SERVER_ERROR,
      message: 'Internal server error',
    };
  }
}

function httpStatusToCode(status: number): ErrorCode {
  switch (status) {
    case 400:
      return ErrorCode.VALIDATION;
    case 401:
      return ErrorCode.UNAUTHORIZED;
    case 403:
      return ErrorCode.FORBIDDEN;
    case 404:
      return ErrorCode.NOT_FOUND;
    case 409:
      return ErrorCode.CONFLICT;
    case 429:
      return ErrorCode.RATE_LIMITED;
    case 503:
      return ErrorCode.SERVICE_UNAVAILABLE;
    default:
      return ErrorCode.INTERNAL;
  }
}
