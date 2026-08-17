import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '@/lib/api';
import type {
  CreatePostDto,
  FeedResponse,
  Post,
  ProfileResponse,
  SearchResponse,
  UserSummary,
} from '@/types/api';

interface FeedData {
  pages: { items: Post[]; nextCursor: string | null }[];
  pageParams: (string | null)[];
}

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
    mutationFn: (dto: CreatePostDto) => api.post<Post>('/posts', dto),
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
      return { reacted: data.liked, likeCount: data.likeCount };
    },
    onMutate: async (postId) => {
      await qc.cancelQueries({ queryKey: ['feed'] });
      const previous = qc.getQueriesData({ queryKey: ['feed'] });

      qc.setQueriesData({ queryKey: ['feed'] }, (old: FeedData | undefined) => {
        if (!old) return old;
        return {
          ...old,
          pages: old.pages.map((page) => ({
            ...page,
            items: page.items.map((item) =>
              item.id === postId
                ? {
                    ...item,
                    reacted: !item.reacted,
                    likeCount: item.reacted ? item.likeCount - 1 : item.likeCount + 1,
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
      const data = await api.post<{ following: boolean; followerCount: number }>(`/users/${userId}/follow`);
      return data;
    },
    onMutate: async (userId) => {
      await qc.cancelQueries({ queryKey: ['profile'] });
      const previous = qc.getQueriesData({ queryKey: ['profile'] });

      qc.setQueriesData({ queryKey: ['profile'] }, (old: ProfileResponse | undefined) => {
        if (!old) return old;
        return {
          ...old,
          user: {
            ...old.user,
            isFollowing: !old.user.isFollowing,
            followerCount: old.user.isFollowing
              ? old.user.followerCount - 1
              : old.user.followerCount + 1,
          },
          followerCount: old.user.isFollowing
            ? old.followerCount - 1
            : old.followerCount + 1,
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

const MOCK_USER: UserSummary = {
  id: 'mock-1',
  username: 'priya.designs',
  name: 'Priya Sharma',
  avatarUrl: null,
  isVerified: true,
  bio: 'Designer & photographer. Capturing the beauty of everyday life.',
  isFollowing: false,
  followerCount: 12400,
  postCount: 247,
};

const MOCK_POSTS: Post[] = [
  {
    id: 'mock-post-1',
    authorId: 'mock-1',
    text: 'Beautiful sunset at Marina Beach, Chennai. The colors were unreal!',
    langTag: 'en',
    media: [],
    likeCount: 234,
    commentCount: 42,
    shareCount: 5,
    reacted: false,
    publishedAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
    createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
    author: MOCK_USER,
  },
  {
    id: 'mock-post-2',
    authorId: 'mock-2',
    text: 'New coffee shop in Bangalore — the filter kaapi here is incredible',
    langTag: 'en',
    media: [],
    likeCount: 89,
    commentCount: 12,
    shareCount: 2,
    reacted: true,
    publishedAt: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString(),
    createdAt: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString(),
    author: {
      ...MOCK_USER,
      id: 'mock-2',
      username: 'arjun.travels',
      name: 'Arjun Menon',
      bio: 'Travel blogger. Exploring Bharat one city at a time.',
      isVerified: false,
      followerCount: 8900,
      postCount: 156,
    },
  },
  {
    id: 'mock-post-3',
    authorId: 'mock-3',
    text: 'Diwali prep at full speed! The rangoli this year is next level',
    langTag: 'en',
    media: [],
    likeCount: 567,
    commentCount: 88,
    shareCount: 12,
    reacted: false,
    publishedAt: new Date(Date.now() - 8 * 60 * 60 * 1000).toISOString(),
    createdAt: new Date(Date.now() - 8 * 60 * 60 * 1000).toISOString(),
    author: {
      ...MOCK_USER,
      id: 'mock-3',
      username: 'meera.cooks',
      name: 'Meera Iyer',
      bio: 'Food & lifestyle. Made with love from Madurai.',
      isVerified: true,
      followerCount: 23100,
      postCount: 432,
    },
  },
];

export { MOCK_POSTS, MOCK_USER };