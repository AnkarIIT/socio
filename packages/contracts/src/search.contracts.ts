import { z } from 'zod';

export const SearchQuerySchema = z.object({
  q: z.string().trim().min(1).max(200),
  cursor: z.string().optional(),
  limit: z.coerce.number().int().min(1).max(50).default(20),
});

export type SearchQuery = z.infer<typeof SearchQuerySchema>;

export const SearchResultSchema = z.object({
  users: z.array(
    z.object({
      id: z.string(),
      username: z.string(),
      name: z.string(),
      avatarUrl: z.string().nullable(),
      isVerified: z.boolean(),
    }),
  ),
  posts: z.array(
    z.object({
      id: z.string(),
      text: z.string().nullable(),
      author: z.object({
        username: z.string(),
        name: z.string(),
        avatarUrl: z.string().nullable(),
      }),
      createdAt: z.string(),
    }),
  ),
  hashtags: z.array(
    z.object({
      tag: z.string(),
      postCount: z.number(),
    }),
  ),
});

export type SearchResult = z.infer<typeof SearchResultSchema>;
