import { z } from 'zod';

export const PostTypeSchema = z.enum(['TEXT', 'IMAGE', 'CLIP', 'MIXED']);
export const PostStatusSchema = z.enum([
  'DRAFT',
  'PENDING',
  'PUBLISHED',
  'REJECTED',
  'TAKEDOWN',
]);

export const CreatePostSchema = z
  .object({
    text: z.string().trim().max(2000).optional(),
    langTag: z.string().min(2).max(8).optional(),
    mediaKeys: z.array(z.string()).max(10).optional(),
    status: z.enum(['DRAFT', 'PENDING']).default('PENDING'),
  })
  .strict();

export type CreatePostDto = z.infer<typeof CreatePostSchema>;

export const UpdatePostSchema = z
  .object({
    text: z.string().trim().max(2000).optional(),
    langTag: z.string().min(2).max(8).nullable().optional(),
  })
  .strict();

export type UpdatePostDto = z.infer<typeof UpdatePostSchema>;

export const PostParamsSchema = z.object({
  id: z.string().min(1),
});

export type PostParams = z.infer<typeof PostParamsSchema>;

export const MediaSchema = z.object({
  kind: z.enum(['IMAGE', 'VIDEO']),
  url: z.string(),
  width: z.number().nullable(),
  height: z.number().nullable(),
  durationMs: z.number().nullable(),
});

export const PostPublicSchema = z.object({
  id: z.string(),
  author: z.object({
    id: z.string(),
    username: z.string(),
    name: z.string(),
    avatarUrl: z.string().nullable(),
    isVerified: z.boolean(),
  }),
  text: z.string().nullable(),
  langTag: z.string().nullable(),
  media: z.array(MediaSchema),
  likeCount: z.number(),
  commentCount: z.number(),
  shareCount: z.number(),
  reacted: z.boolean(),
  publishedAt: z.string().nullable(),
  createdAt: z.string(),
});

export type PostPublic = z.infer<typeof PostPublicSchema>;

export const PostListSchema = z.object({
  items: z.array(PostPublicSchema),
  nextCursor: z.string().nullable(),
});

export type PostList = z.infer<typeof PostListSchema>;

export const FeedQuerySchema = z.object({
  cursor: z.string().optional(),
  limit: z.coerce.number().int().min(1).max(50).default(20),
});

export type FeedQuery = z.infer<typeof FeedQuerySchema>;
