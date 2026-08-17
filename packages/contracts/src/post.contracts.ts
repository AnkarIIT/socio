import { z } from 'zod';

export const CreatePostSchema = z
  .object({
    text: z.string().trim().max(2000).optional(),
    langTag: z.string().min(2).max(8).optional(),
    mediaKeys: z.array(z.string()).max(10).optional(),
  })
  .strict();

export type CreatePostDto = z.infer<typeof CreatePostSchema>;

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
  media: z.array(
    z.object({
      kind: z.enum(['IMAGE', 'VIDEO']),
      url: z.string(),
      width: z.number().nullable(),
      height: z.number().nullable(),
      durationMs: z.number().nullable(),
    }),
  ),
  likeCount: z.number(),
  commentCount: z.number(),
  shareCount: z.number(),
  reacted: z.boolean(),
  publishedAt: z.string(),
});

export type PostPublic = z.infer<typeof PostPublicSchema>;

export const PostListSchema = z.object({
  items: z.array(PostPublicSchema),
  nextCursor: z.string().nullable(),
});

export type PostList = z.infer<typeof PostListSchema>;
