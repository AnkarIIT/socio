import { ErrorCode } from '@bharat/shared';
import {
  DomainException,
  conflict,
  notFound,
  rateLimited,
  validationError,
} from './domain-exceptions';

describe('domain-exceptions', () => {
  it('exposes taxonomy code and http status', () => {
    const err = conflict('Username taken');
    expect(err).toBeInstanceOf(DomainException);
    expect(err.code).toBe(ErrorCode.CONFLICT);
    expect(err.httpStatus).toBe(409);
  });

  it('carries validation details', () => {
    const err = validationError('Invalid payload', { phone: 'invalid' });
    expect(err.httpStatus).toBe(400);
    expect(err.details).toEqual({ phone: 'invalid' });
  });

  it('maps standard statuses', () => {
    expect(notFound().httpStatus).toBe(404);
    expect(rateLimited().httpStatus).toBe(429);
  });
});
