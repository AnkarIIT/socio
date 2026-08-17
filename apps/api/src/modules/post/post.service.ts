import { Injectable, Logger } from '@nestjs/common';
import type {
  CreatePostDto,
  UpdatePostDto,
  PostPublic,
  PostList,
  FeedQuery,
} from '@bharat/contracts';
import { encodeCursor, decodeCursor } from '@bharat/shared';
import { PrismaService } from '../../prisma/prisma.service';
import {
  notFound,
  forbidden,
  validationError,
} from '../../common/errors/domain-exceptions';

type PostWithAuthor = {
  id: string;
  text: string | null;
  langTag: string | null;
  likeCount: number;
  commentCount: number;
  shareCount: number;
  publishedAt: Date | null;
  createdAt: Date;
  author: {
    id: string;
    username: string;
    name: string;
    avatarUrl: string | null;
    isVerified: boolean;
  };
  media: {
    kind: string;
    variants: unknown;
    width: number | null;
    height: number | null;
    durationMs: number | null;
  }[];
};

@Injectable()
export class PostService {
  private readonly logger = new Logger(PostService.name);

  constructor(private readonly prisma: PrismaService) {}

  async create(authorId: string, dto: CreatePostDto): Promise<PostPublic> {
    if (!dto.text && (!dto.mediaKeys || dto.mediaKeys.length === 0)) {
      throw validationError('Post must have text or media');
    }

    const post = await this.prisma.post.create({
      data: {
        authorId,
        text: dto.text,
        langTag: dto.langTag,
        status: dto.status,
        type: dto.mediaKeys?.length ? 'MIXED' : 'TEXT',
        publishedAt: dto.status === 'PENDING' ? new Date() : null,
      },
      include: {
        author: {
          select: {
            id: true,
            username: true,
            name: true,
            avatarUrl: true,
            isVerified: true,
          },
        },
        media: true,
      },
    });

    if (dto.mediaKeys?.length) {
      await this.prisma.mediaAsset.updateMany({
        where: { id: { in: dto.mediaKeys }, userId: authorId },
        data: { postId: post.id },
      });
    }

    return this.toPublic(post, false);
  }

  async getById(postId: string, viewerId?: string): Promise<PostPublic> {
    const post = await this.prisma.post.findUnique({
      where: { id: postId },
      include: {
        author: {
          select: {
            id: true,
            username: true,
            name: true,
            avatarUrl: true,
            isVerified: true,
          },
        },
        media: true,
      },
    });
    if (!post || post.status !== 'PUBLISHED') throw notFound('Post not found');

    let reacted = false;
    if (viewerId) {
      const like = await this.prisma.like.findUnique({
        where: { postId_userId: { postId, userId: viewerId } },
      });
      reacted = !!like;
    }

    return this.toPublic(post, reacted);
  }

  async update(
    postId: string,
    userId: string,
    dto: UpdatePostDto,
  ): Promise<PostPublic> {
    const post = await this.prisma.post.findUnique({ where: { id: postId } });
    if (!post) throw notFound('Post not found');
    if (post.authorId !== userId) throw forbidden('Not your post');

    const updated = await this.prisma.post.update({
      where: { id: postId },
      data: { ...dto },
      include: {
        author: {
          select: {
            id: true,
            username: true,
            name: true,
            avatarUrl: true,
            isVerified: true,
          },
        },
        media: true,
      },
    });

    return this.toPublic(updated, false);
  }

  async remove(postId: string, userId: string): Promise<{ ok: true }> {
    const post = await this.prisma.post.findUnique({ where: { id: postId } });
    if (!post) throw notFound('Post not found');
    if (post.authorId !== userId) throw forbidden('Not your post');

    await this.prisma.post.delete({ where: { id: postId } });
    return { ok: true };
  }

  async feed(userId: string, query: FeedQuery): Promise<PostList> {
    const limit = query.limit;
    const cursor = query.cursor ? decodeCursor(query.cursor) : null;

    const where: Record<string, unknown> = {
      status: 'PUBLISHED',
      author: { bannedAt: null },
    };

    if (cursor) {
      where.publishedAt = { lt: new Date(cursor.publishedAt as string) };
    }

    const posts = await this.prisma.post.findMany({
      where,
      include: {
        author: {
          select: {
            id: true,
            username: true,
            name: true,
            avatarUrl: true,
            isVerified: true,
          },
        },
        media: true,
      },
      orderBy: [{ score: 'desc' }, { publishedAt: 'desc' }],
      take: limit + 1,
    });

    const hasMore = posts.length > limit;
    const items = hasMore ? posts.slice(0, limit) : posts;

    const postIds = items.map((p) => p.id);
    const viewerLikes = await this.prisma.like.findMany({
      where: { postId: { in: postIds }, userId },
      select: { postId: true },
    });
    const likedSet = new Set(viewerLikes.map((l) => l.postId));

    const nextCursor =
      hasMore && items.length > 0
        ? encodeCursor({
            publishedAt: items[items.length - 1].publishedAt!.toISOString(),
          })
        : null;

    return {
      items: items.map((p) =>
        this.toPublic(p as PostWithAuthor, likedSet.has(p.id)),
      ),
      nextCursor,
    };
  }

  private toPublic(post: PostWithAuthor, reacted: boolean): PostPublic {
    return {
      id: post.id,
      author: post.author,
      text: post.text,
      langTag: post.langTag,
      media: (post.media ?? []).map((m) => ({
        kind: m.kind as 'IMAGE' | 'VIDEO',
        url: this.buildMediaUrl(m.variants),
        width: m.width,
        height: m.height,
        durationMs: m.durationMs,
      })),
      likeCount: post.likeCount,
      commentCount: post.commentCount,
      shareCount: post.shareCount,
      reacted,
      publishedAt: post.publishedAt?.toISOString() ?? null,
      createdAt: post.createdAt.toISOString(),
    };
  }

  private buildMediaUrl(variants: unknown): string {
    if (variants && typeof variants === 'object') {
      const v = variants as Record<string, string>;
      return v['cdnUrl'] ?? v['original'] ?? '';
    }
    return '';
  }
}
