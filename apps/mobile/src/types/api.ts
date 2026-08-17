export interface User {
  id: string;
  username: string;
  name: string;
  avatarUrl: string | null;
  coverUrl: string | null;
  bio: string | null;
  isVerified: boolean;
  isPrivate: boolean;
  locale: string;
  createdAt: string;
}

export interface Post {
  id: string;
  authorId: string;
  body: string;
  mediaUrl: string | null;
  mediaType: 'image' | 'video' | null;
  likeCount: number;
  commentCount: number;
  isLiked: boolean;
  author: User;
  createdAt: string;
}

export interface Comment {
  id: string;
  postId: string;
  authorId: string;
  body: string;
  author: User;
  createdAt: string;
}

export interface FeedResponse {
  items: Post[];
  nextCursor: string | null;
}

export interface ProfileResponse {
  user: User;
  postsCount: number;
  followersCount: number;
  followingCount: number;
  isFollowing: boolean;
  isMuted: boolean;
  isBlocked: boolean;
}

export interface Story {
  id: string;
  user: User;
  hasUnviewed: boolean;
}

export interface SearchResponse {
  users: User[];
  posts: {
    id: string;
    text: string | null;
    author: { username: string; name: string; avatarUrl: string | null };
    createdAt: string;
  }[];
  hashtags: { tag: string; postCount: number }[];
}

export interface PresignResponse {
  uploadUrl: string;
  assetId: string;
  key: string;
  expiresAt: string;
}

export interface MediaAsset {
  id: string;
  kind: 'IMAGE' | 'VIDEO';
  status: 'PROCESSING' | 'READY' | 'FAILED';
  mimeType: string | null;
  width: number | null;
  height: number | null;
  createdAt: string;
}
