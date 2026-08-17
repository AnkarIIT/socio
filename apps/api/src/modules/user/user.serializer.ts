import { UserPublic } from '@bharat/contracts';

export type PublicUserRow = {
  id: string;
  username: string;
  name: string;
  avatarUrl: string | null;
  coverUrl: string | null;
  isVerified: boolean;
  isPrivate: boolean;
  locale: string;
  createdAt: Date;
};

/** Maps a User row to the public contract shape. */
export function toUserPublic(user: PublicUserRow): UserPublic {
  return {
    id: user.id,
    username: user.username,
    name: user.name,
    avatarUrl: user.avatarUrl,
    coverUrl: user.coverUrl,
    bio: null,
    isVerified: user.isVerified,
    isPrivate: user.isPrivate,
    locale: user.locale,
    createdAt: user.createdAt.toISOString(),
  };
}
