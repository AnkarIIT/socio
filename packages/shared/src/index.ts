/**
 * Shared domain utilities and error taxonomy for Bharat.
 * Consumed by both the API and mobile app (via packages/contracts where typed).
 */

/** Canonical error codes — mirrors the taxonomy in architecture-decision.md §8.1 */
export enum ErrorCode {
  VALIDATION = 'VALIDATION',
  UNAUTHORIZED = 'UNAUTHORIZED',
  FORBIDDEN = 'FORBIDDEN',
  NOT_FOUND = 'NOT_FOUND',
  CONFLICT = 'CONFLICT',
  RATE_LIMITED = 'RATE_LIMITED',
  INTERNAL = 'INTERNAL',
  SERVICE_UNAVAILABLE = 'SERVICE_UNAVAILABLE',
  OFFLINE = 'OFFLINE',
}

/** The error envelope returned by the API for every failed request. */
export interface ErrorEnvelope {
  error: {
    code: ErrorCode;
    message: string;
    details?: Record<string, unknown>;
    requestId: string;
  };
}

/** Standard paginated response shape (keyset cursor). */
export interface Paginated<T> {
  items: T[];
  nextCursor: string | null;
}

/**
 * Normalizes an Indian phone number to 10-digit national format.
 * Accepts: +91XXXXXXXXXX, 91XXXXXXXXXX, 0XXXXXXXXXX, XXXXXXXXXX (with separators).
 * Returns null if the number is not a valid Indian mobile.
 */
export function normalizeIndiaPhone(input: string): string | null {
  const digits = input.replace(/[\s\-().]/g, '');
  if (/^\d{13}$/.test(digits) && digits.startsWith('91')) return digits.slice(2);
  if (/^\d{11}$/.test(digits) && digits.startsWith('0')) return digits.slice(1);
  if (/^\d{10}$/.test(digits)) return digits;
  return null;
}

/** Encodes a keyset cursor (opaque JSON blob) into a URL-safe string. */
export function encodeCursor(payload: Record<string, unknown>): string {
  return Buffer.from(JSON.stringify(payload), 'utf8').toString('base64url');
}

/** Decodes a keyset cursor. Returns null when malformed. */
export function decodeCursor(cursor: string): Record<string, unknown> | null {
  try {
    const raw = Buffer.from(cursor, 'base64url').toString('utf8');
    const parsed: unknown = JSON.parse(raw);
    if (typeof parsed !== 'object' || parsed === null) return null;
    return parsed as Record<string, unknown>;
  } catch {
    return null;
  }
}

/** Mask a phone number for display: +91 98******12 */
export function maskIndiaPhone(phone10: string): string {
  if (phone10.length !== 10) return phone10;
  return `+91 ${phone10.slice(0, 2)}******${phone10.slice(8)}`;
}
