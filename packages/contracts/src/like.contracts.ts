import { z } from 'zod';

export const LikePostParamsSchema = z.object({
  postId: z.string().min(1),
});

export const LikeResponseSchema = z.object({
  liked: z.boolean(),
  likeCount: z.number(),
});

export type LikeResponse = z.infer<typeof LikeResponseSchema>;
