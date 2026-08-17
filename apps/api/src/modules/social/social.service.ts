import { Injectable, Logger } from '@nestjs/common';
import type {
  FollowResponse,
  FollowerList,
  FollowerQuery,
  BlockResponse,
  MuteResponse,
  UserProfile,
  ProfileParams,
} from '@bharat/contracts';
import { encodeCursor, decodeCursor } from '@bharat/shared';
import { PrismaService } from '../../prisma/prisma.service';
import { notFound, forbidden } from '../../common/errors/domain-exceptions';

@Injectable()
export class SocialService {
  private readonly logger = new Logger(SocialService.name);

  constructor(private readonly prisma: PrismaService) {}

  async follow(
    followerId: string,
    followeeId: string,
  ): Promise<FollowResponse> {
    if (followerId === followeeId) {
      throw forbidden('Cannot follow yourself');
    }

    const followee = await this.prisma.user.findUnique({
      where: { id: followeeId },
    });
    if (!followee || followee.bannedAt) throw notFound('User not found');

    const existing = await this.prisma.follow.findUnique({
      where: { followerId_followeeId: { followerId, followeeId } },
    });

    if (existing) {
      await this.prisma.follow.delete({ where: { id: existing.id } });
    } else {
      await this.prisma.follow.create({
        data: { followerId, followeeId },
      });
    }

    const [followerCount, followingCount] = await Promise.all([
      this.prisma.follow.count({ where: { followeeId } }),
      this.prisma.follow.count({ where: { followerId } }),
    ]);

    return {
      following: !existing,
      followerCount,
      followingCount,
    };
  }

  async block(blockerId: string, blockedId: string): Promise<BlockResponse> {
    if (blockerId === blockedId) {
      throw forbidden('Cannot block yourself');
    }

    const blocked = await this.prisma.user.findUnique({
      where: { id: blockedId },
    });
    if (!blocked || blocked.bannedAt) throw notFound('User not found');

    const existing = await this.prisma.block.findUnique({
      where: { blockerId_blockedId: { blockerId, blockedId } },
    });

    if (existing) {
      await this.prisma.block.delete({ where: { id: existing.id } });
      // Also remove follow relationships
      await this.prisma.follow.deleteMany({
        where: {
          OR: [
            { followerId: blockerId, followeeId: blockedId },
            { followerId: blockedId, followeeId: blockerId },
          ],
        },
      });
    } else {
      await this.prisma.block.create({
        data: { blockerId, blockedId },
      });
      // Remove follow relationships on block
      await this.prisma.follow.deleteMany({
        where: {
          OR: [
            { followerId: blockerId, followeeId: blockedId },
            { followerId: blockedId, followeeId: blockerId },
          ],
        },
      });
    }

    return { blocked: !existing };
  }

  async mute(muterId: string, muteeId: string): Promise<MuteResponse> {
    if (muterId === muteeId) {
      throw forbidden('Cannot mute yourself');
    }

    const mutee = await this.prisma.user.findUnique({
      where: { id: muteeId },
    });
    if (!mutee || mutee.bannedAt) throw notFound('User not found');

    const existing = await this.prisma.mute.findUnique({
      where: { muterId_muteeId: { muterId, muteeId } },
    });

    if (existing) {
      await this.prisma.mute.delete({ where: { id: existing.id } });
    } else {
      await this.prisma.mute.create({
        data: { muterId, muteeId },
      });
    }

    return { muted: !existing };
  }

  async getFollowers(
    userId: string,
    query: FollowerQuery,
    viewerId?: string,
  ): Promise<FollowerList> {
    const limit = query.limit;
    const cursor = query.cursor ? decodeCursor(query.cursor) : null;

    const where: Record<string, unknown> = { followeeId: userId };
    if (cursor) {
      where.createdAt = { lt: new Date(cursor.createdAt as string) };
    }

    const follows = await this.prisma.follow.findMany({
      where,
      include: {
        follower: {
          select: {
            id: true,
            username: true,
            name: true,
            avatarUrl: true,
            isVerified: true,
          },
        },
      },
      orderBy: { createdAt: 'desc' },
      take: limit + 1,
    });

    const hasMore = follows.length > limit;
    const items = hasMore ? follows.slice(0, limit) : follows;

    // Check if viewer follows these users
    let viewerFollows = new Set<string>();
    if (viewerId) {
      const userIds = items.map((f) => f.followerId);
      const existingFollows = await this.prisma.follow.findMany({
        where: { followerId: viewerId, followeeId: { in: userIds } },
        select: { followeeId: true },
      });
      viewerFollows = new Set(existingFollows.map((f) => f.followeeId));
    }

    const nextCursor =
      hasMore && items.length > 0
        ? encodeCursor({
            createdAt: items[items.length - 1].createdAt.toISOString(),
          })
        : null;

    return {
      items: items.map((f) => ({
        ...f.follower,
        isFollowing: viewerFollows.has(f.followerId),
      })),
      nextCursor,
    };
  }

  async getFollowing(
    userId: string,
    query: FollowerQuery,
    viewerId?: string,
  ): Promise<FollowerList> {
    const limit = query.limit;
    const cursor = query.cursor ? decodeCursor(query.cursor) : null;

    const where: Record<string, unknown> = { followerId: userId };
    if (cursor) {
      where.createdAt = { lt: new Date(cursor.createdAt as string) };
    }

    const follows = await this.prisma.follow.findMany({
      where,
      include: {
        followee: {
          select: {
            id: true,
            username: true,
            name: true,
            avatarUrl: true,
            isVerified: true,
          },
        },
      },
      orderBy: { createdAt: 'desc' },
      take: limit + 1,
    });

    const hasMore = follows.length > limit;
    const items = hasMore ? follows.slice(0, limit) : follows;

    let viewerFollows = new Set<string>();
    if (viewerId) {
      const userIds = items.map((f) => f.followeeId);
      const existingFollows = await this.prisma.follow.findMany({
        where: { followerId: viewerId, followeeId: { in: userIds } },
        select: { followeeId: true },
      });
      viewerFollows = new Set(existingFollows.map((f) => f.followeeId));
    }

    const nextCursor =
      hasMore && items.length > 0
        ? encodeCursor({
            createdAt: items[items.length - 1].createdAt.toISOString(),
          })
        : null;

    return {
      items: items.map((f) => ({
        ...f.followee,
        isFollowing: viewerFollows.has(f.followeeId),
      })),
      nextCursor,
    };
  }

  async getProfile(
    params: ProfileParams,
    viewerId?: string,
  ): Promise<UserProfile> {
    const user = await this.prisma.user.findUnique({
      where: { username: params.username },
      include: { profile: true },
    });
    if (!user || user.bannedAt) throw notFound('User not found');

    const [followerCount, followingCount, postCount] = await Promise.all([
      this.prisma.follow.count({ where: { followeeId: user.id } }),
      this.prisma.follow.count({ where: { followerId: user.id } }),
      this.prisma.post.count({
        where: { authorId: user.id, status: 'PUBLISHED' },
      }),
    ]);

    let isFollowing = false;
    if (viewerId && viewerId !== user.id) {
      const follow = await this.prisma.follow.findUnique({
        where: {
          followerId_followeeId: { followerId: viewerId, followeeId: user.id },
        },
      });
      isFollowing = !!follow;
    }

    return {
      user: {
        id: user.id,
        username: user.username,
        name: user.name,
        avatarUrl: user.avatarUrl,
        isVerified: user.isVerified,
        bio: user.profile?.bio ?? null,
        isFollowing,
        followerCount,
        postCount,
      },
      postCount,
      followerCount,
      followingCount,
    };
  }

  /** Returns set of user IDs that `userId` is blocked by or has blocked. */
  async getBlockedIds(userId: string): Promise<Set<string>> {
    const blocks = await this.prisma.block.findMany({
      where: {
        OR: [{ blockerId: userId }, { blockedId: userId }],
      },
      select: {
        blockerId: true,
        blockedId: true,
      },
    });

    const ids = new Set<string>();
    for (const b of blocks) {
      if (b.blockerId !== userId) ids.add(b.blockerId);
      if (b.blockedId !== userId) ids.add(b.blockedId);
    }
    return ids;
  }
}
