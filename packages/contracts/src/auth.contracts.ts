import { z } from 'zod';
import { UserPublicSchema } from './user.contracts';

export const RequestOtpSchema = z.object({
  phone: z.string().min(10).max(14),
});

export type RequestOtpDto = z.infer<typeof RequestOtpSchema>;

export const VerifyOtpSchema = z.object({
  phone: z.string().min(10).max(14),
  code: z.string().regex(/^\d{6}$/, 'code must be 6 digits'),
});

export type VerifyOtpDto = z.infer<typeof VerifyOtpSchema>;

export const RefreshTokenSchema = z.object({
  refreshToken: z.string().min(10),
});

export type RefreshTokenDto = z.infer<typeof RefreshTokenSchema>;

export const AuthResponseSchema = z.object({
  accessToken: z.string(),
  refreshToken: z.string(),
  user: UserPublicSchema,
});

export type AuthResponse = z.infer<typeof AuthResponseSchema>;

export const VerifyOtpResponseSchema = AuthResponseSchema;
