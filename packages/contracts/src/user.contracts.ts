import { z } from 'zod';

/** Public user shape — safe to return to any client. */
export const UserPublicSchema = z.object({
  id: z.string(),
  username: z.string(),
  name: z.string(),
  avatarUrl: z.string().nullable(),
  coverUrl: z.string().nullable(),
  bio: z.string().nullable(),
  isVerified: z.boolean(),
  isPrivate: z.boolean(),
  locale: z.string(),
  createdAt: z.string(),
});

export type UserPublic = z.infer<typeof UserPublicSchema>;

export const UpdateMeSchema = z
  .object({
    name: z.string().trim().min(1).max(60).optional(),
    username: z
      .string()
      .trim()
      .regex(/^[a-z0-9_]{3,24}$/, '3-24 chars, lowercase letters, digits, underscore')
      .optional(),
    bio: z.string().trim().max(160).nullable().optional(),
    avatarUrl: z.string().url().nullable().optional(),
    coverUrl: z.string().url().nullable().optional(),
    locale: z.string().min(2).max(8).optional(),
  })
  .strict();

export type UpdateMeDto = z.infer<typeof UpdateMeSchema>;

export const GetUserParamsSchema = z.object({
  username: z.string().min(1).max(30),
});

export type GetUserParams = z.infer<typeof GetUserParamsSchema>;
