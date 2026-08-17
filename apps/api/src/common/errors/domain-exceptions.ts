import { ErrorCode } from '@bharat/shared';

/** Domain error carrying a taxonomy code, HTTP status, and optional details. */
export class DomainException extends Error {
  constructor(
    readonly code: ErrorCode,
    message: string,
    readonly httpStatus: number,
    readonly details?: Record<string, unknown>,
  ) {
    super(message);
    this.name = 'DomainException';
  }
}

export function validationError(
  message: string,
  details?: Record<string, unknown>,
): DomainException {
  return new DomainException(ErrorCode.VALIDATION, message, 400, details);
}

export function unauthorized(
  message = 'Authentication required',
): DomainException {
  return new DomainException(ErrorCode.UNAUTHORIZED, message, 401);
}

export function forbidden(
  message = 'You do not have permission to do this',
): DomainException {
  return new DomainException(ErrorCode.FORBIDDEN, message, 403);
}

export function notFound(message = 'Resource not found'): DomainException {
  return new DomainException(ErrorCode.NOT_FOUND, message, 404);
}

export function conflict(message = 'Resource already exists'): DomainException {
  return new DomainException(ErrorCode.CONFLICT, message, 409);
}

export function rateLimited(
  message = 'Too many requests, slow down',
): DomainException {
  return new DomainException(ErrorCode.RATE_LIMITED, message, 429);
}

export function serviceUnavailable(
  message = 'Service temporarily unavailable',
): DomainException {
  return new DomainException(ErrorCode.SERVICE_UNAVAILABLE, message, 503);
}
