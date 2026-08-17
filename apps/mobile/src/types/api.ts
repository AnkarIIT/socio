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

/** UserSummary from backend — minimal user for lists */
export interface UserSummary {
  id: string;
  username: string;
  name: string;
  avatarUrl: string | null;
  isVerified: boolean;
  bio: string | null;
  isFollowing: boolean;
  followerCount: number;
  postCount: number;
}

export interface Media {
  kind: 'IMAGE' | 'VIDEO';
  url: string;
  width: number | null;
  height: number | null;
  durationMs: number | null;
}

export interface Post {
  id: string;
  authorId: string;
  text: string | null;
  langTag: string | null;
  media: Media[];
  likeCount: number;
  commentCount: number;
  shareCount: number;
  reacted: boolean;
  publishedAt: string | null;
  createdAt: string;
  author: UserSummary;
}

export interface Comment {
  id: string;
  postId: string;
  authorId: string;
  text: string;
  parentId: string | null;
  author: UserSummary;
  createdAt: string;
}

export interface FeedResponse {
  items: Post[];
  nextCursor: string | null;
}

export interface ProfileResponse {
  user: UserSummary;
  postCount: number;
  followerCount: number;
  followingCount: number;
}

export interface Story {
  id: string;
  user: User;
  hasUnviewed: boolean;
}

export interface SearchResponse {
  users: UserSummary[];
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

export interface CreatePostDto {
  text?: string;
  langTag?: string;
  mediaKeys?: string[];
  status?: 'DRAFT' | 'PENDING';
}