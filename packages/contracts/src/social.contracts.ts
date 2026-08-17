import { z } from 'zod';

export const FollowParamsSchema = z.object({
  userId: z.string().min(1),
});

export const FollowResponseSchema = z.object({
  following: z.boolean(),
  followerCount: z.number(),
  followingCount: z.number(),
});

export type FollowResponse = z.infer<typeof FollowResponseSchema>;

export const FollowerQuerySchema = z.object({
  cursor: z.string().optional(),
  limit: z.coerce.number().int().min(1).max(50).default(20),
});

export type FollowerQuery = z.infer<typeof FollowerQuerySchema>;

export const FollowerItemSchema = z.object({
  id: z.string(),
  username: z.string(),
  name: z.string(),
  avatarUrl: z.string().nullable(),
  isVerified: z.boolean(),
  isFollowing: z.boolean(),
});

export type FollowerItem = z.infer<typeof FollowerItemSchema>;

export const FollowerListSchema = z.object({
  items: z.array(FollowerItemSchema),
  nextCursor: z.string().nullable(),
});

export type FollowerList = z.infer<typeof FollowerListSchema>;

export const UserSummarySchema = z.object({
  id: z.string(),
  username: z.string(),
  name: z.string(),
  avatarUrl: z.string().nullable(),
  isVerified: z.boolean(),
  bio: z.string().nullable(),
  isFollowing: z.boolean(),
  followerCount: z.number(),
  postCount: z.number(),
});

export type UserSummary = z.infer<typeof UserSummarySchema>;

export const BlockParamsSchema = z.object({
  userId: z.string().min(1),
});

export const BlockResponseSchema = z.object({
  blocked: z.boolean(),
});

export type BlockResponse = z.infer<typeof BlockResponseSchema>;

export const MuteParamsSchema = z.object({
  userId: z.string().min(1),
});

export const MuteResponseSchema = z.object({
  muted: z.boolean(),
});

export type MuteResponse = z.infer<typeof MuteResponseSchema>;

export const ProfileParamsSchema = z.object({
  username: z.string().min(1).max(30),
});

export type ProfileParams = z.infer<typeof ProfileParamsSchema>;

export const UserProfileSchema = z.object({
  user: UserSummarySchema,
  postCount: z.number(),
  followerCount: z.number(),
  followingCount: z.number(),
});

export type UserProfile = z.infer<typeof UserProfileSchema>;
