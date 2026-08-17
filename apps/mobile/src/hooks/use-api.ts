import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '@/lib/api';
import type { FeedResponse, Post, ProfileResponse, SearchResponse, User } from '@/types/api';

export function useFeed() {
  return useInfiniteQuery({
    queryKey: ['feed'],
    queryFn: ({ pageParam }) => {
      const params = pageParam ? `?cursor=${encodeURIComponent(pageParam)}` : '';
      return api.get<FeedResponse>(`/posts/feed${params}`);
    },
    initialPageParam: null as string | null,
    getNextPageParam: (lastPage) => lastPage.nextCursor,
  });
}

export function useFeedWithMock() {
  const query = useFeed();

  if (!query.data && query.isLoading) {
    const mockData = {
      pages: [
        {
          items: MOCK_POSTS,
          nextCursor: null as string | null,
        },
      ],
      pageParams: [null],
    };
    return {
      ...query,
      data: mockData,
    } as typeof query & { data: typeof mockData };
  }

  return query;
}

export function useCreatePost() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: { body: string; mediaUrl?: string; mediaType?: string }) =>
      api.post<Post>('/posts', body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['feed'] });
    },
  });
}

export function useLikePost() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (postId: string) => {
      const data = await api.post<{ liked: boolean; likeCount: number }>(
        `/posts/${postId}/like`,
      );
      return data;
    },
    onMutate: async (postId) => {
      await qc.cancelQueries({ queryKey: ['feed'] });
      const previous = qc.getQueriesData({ queryKey: ['feed'] });

      qc.setQueriesData({ queryKey: ['feed'] }, (old: { pages: { items: Post[]; nextCursor: string | null }[] } | undefined) => {
        if (!old) return old;
        return {
          ...old,
          pages: old.pages.map((page: { items: Post[]; nextCursor: string | null }) => ({
            ...page,
            items: page.items.map((item: Post) =>
              item.id === postId
                ? {
                    ...item,
                    isLiked: !item.isLiked,
                    likeCount: item.isLiked ? item.likeCount - 1 : item.likeCount + 1,
                  }
                : item,
            ),
          })),
        };
      });

      return { previous };
    },
    onError: (_err, _postId, context) => {
      if (context?.previous) {
        context.previous.forEach(([key, data]) => qc.setQueryData(key, data));
      }
    },
    onSettled: () => {
      qc.invalidateQueries({ queryKey: ['feed'] });
    },
  });
}

export function useProfile(username: string) {
  return useQuery({
    queryKey: ['profile', username],
    queryFn: () => api.get<ProfileResponse>(`/users/profile/${username}`),
    enabled: !!username,
  });
}

export function useFollowUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (userId: string) => {
      const data = await api.post<{ following: boolean }>(`/users/${userId}/follow`);
      return data;
    },
    onMutate: async (userId) => {
      await qc.cancelQueries({ queryKey: ['profile'] });
      const previous = qc.getQueriesData({ queryKey: ['profile'] });

      qc.setQueriesData({ queryKey: ['profile'] }, (old: ProfileResponse | undefined) => {
        if (!old) return old;
        return {
          ...old,
          isFollowing: !old.isFollowing,
          followersCount: old.isFollowing
            ? old.followersCount - 1
            : old.followersCount + 1,
        };
      });

      return { previous };
    },
    onError: (_err, _userId, context) => {
      if (context?.previous) {
        context.previous.forEach(([key, data]) => qc.setQueryData(key, data));
      }
    },
    onSettled: () => {
      qc.invalidateQueries({ queryKey: ['profile'] });
    },
  });
}

export function useSearchUsers(query: string) {
  return useQuery({
    queryKey: ['search', 'users', query],
    queryFn: () => api.get<SearchResponse>(`/search?q=${encodeURIComponent(query)}`),
    enabled: query.length >= 2,
  });
}

export async function uploadMedia(
  uri: string,
  mimeType: string,
  sizeBytes: number,
  filename: string,
): Promise<{ assetId: string; key: string }> {
  const presign = await api.post<{
    uploadUrl: string;
    assetId: string;
    key: string;
    expiresAt: string;
  }>('/media/presign', { filename, mimeType, sizeBytes });

  const response = await fetch(uri);
  const blob = await response.blob();

  await fetch(presign.uploadUrl, {
    method: 'PUT',
    headers: { 'Content-Type': mimeType },
    body: blob,
  });

  await api.post('/media/complete', { assetId: presign.assetId });

  return { assetId: presign.assetId, key: presign.key };
}

const MOCK_USER: User = {
  id: 'mock-1',
  username: 'priya.designs',
  name: 'Priya Sharma',
  avatarUrl: null,
  coverUrl: null,
  bio: 'Designer & photographer. Capturing the beauty of everyday life.',
  isVerified: true,
  isPrivate: false,
  locale: 'en',
  createdAt: '2025-01-15T00:00:00.000Z',
};

const MOCK_POSTS: Post[] = [
  {
    id: 'mock-post-1',
    authorId: 'mock-1',
    body: 'Beautiful sunset at Marina Beach, Chennai. The colors were unreal!',
    mediaUrl: null,
    mediaType: 'image',
    likeCount: 234,
    commentCount: 42,
    isLiked: false,
    author: MOCK_USER,
    createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
  },
  {
    id: 'mock-post-2',
    authorId: 'mock-2',
    body: 'New coffee shop in Bangalore — the filter kaapi here is incredible',
    mediaUrl: null,
    mediaType: 'image',
    likeCount: 89,
    commentCount: 12,
    isLiked: true,
    author: {
      ...MOCK_USER,
      id: 'mock-2',
      username: 'arjun.travels',
      name: 'Arjun Menon',
      bio: 'Travel blogger. Exploring Bharat one city at a time.',
      isVerified: false,
    },
    createdAt: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString(),
  },
  {
    id: 'mock-post-3',
    authorId: 'mock-3',
    body: 'Diwali prep at full speed! The rangoli this year is next level',
    mediaUrl: null,
    mediaType: 'image',
    likeCount: 567,
    commentCount: 88,
    isLiked: false,
    author: {
      ...MOCK_USER,
      id: 'mock-3',
      username: 'meera.cooks',
      name: 'Meera Iyer',
      bio: 'Food & lifestyle. Made with love from Madurai.',
      isVerified: true,
    },
    createdAt: new Date(Date.now() - 8 * 60 * 60 * 1000).toISOString(),
  },
];

export { MOCK_POSTS, MOCK_USER };
