import { z } from 'zod';

export const CreateCommentSchema = z
  .object({
    text: z.string().trim().min(1).max(500),
    parentId: z.string().optional(),
  })
  .strict();

export type CreateCommentDto = z.infer<typeof CreateCommentSchema>;

export const CommentParamsSchema = z.object({
  postId: z.string().min(1),
});

export type CommentParams = z.infer<typeof CommentParamsSchema>;

export const CommentIdParamsSchema = z.object({
  id: z.string().min(1),
});

export const CommentPublicSchema = z.object({
  id: z.string(),
  postId: z.string(),
  author: z.object({
    id: z.string(),
    username: z.string(),
    name: z.string(),
    avatarUrl: z.string().nullable(),
    isVerified: z.boolean(),
  }),
  text: z.string(),
  parentId: z.string().nullable(),
  createdAt: z.string(),
});

export type CommentPublic = z.infer<typeof CommentPublicSchema>;

export const CommentListSchema = z.object({
  items: z.array(CommentPublicSchema),
  nextCursor: z.string().nullable(),
});

export type CommentList = z.infer<typeof CommentListSchema>;

export const CommentQuerySchema = z.object({
  cursor: z.string().optional(),
  limit: z.coerce.number().int().min(1).max(50).default(20),
});

export type CommentQuery = z.infer<typeof CommentQuerySchema>;
