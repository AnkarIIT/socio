import { Injectable, Logger } from '@nestjs/common';
import type {
  CreateCommentDto,
  CommentPublic,
  CommentList,
  CommentQuery,
} from '@bharat/contracts';
import { encodeCursor, decodeCursor } from '@bharat/shared';
import { PrismaService } from '../../prisma/prisma.service';
import { notFound, forbidden } from '../../common/errors/domain-exceptions';

@Injectable()
export class CommentService {
  private readonly logger = new Logger(CommentService.name);

  constructor(private readonly prisma: PrismaService) {}

  async create(
    postId: string,
    authorId: string,
    dto: CreateCommentDto,
  ): Promise<CommentPublic> {
    const post = await this.prisma.post.findUnique({ where: { id: postId } });
    if (!post || post.status !== 'PUBLISHED') throw notFound('Post not found');

    if (dto.parentId) {
      const parent = await this.prisma.comment.findUnique({
        where: { id: dto.parentId },
      });
      if (!parent || parent.postId !== postId) {
        throw notFound('Parent comment not found');
      }
    }

    const [comment] = await this.prisma.$transaction([
      this.prisma.comment.create({
        data: {
          postId,
          authorId,
          text: dto.text,
          parentId: dto.parentId,
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
        },
      }),
      this.prisma.post.update({
        where: { id: postId },
        data: { commentCount: { increment: 1 } },
      }),
    ]);

    return {
      id: comment.id,
      postId: comment.postId,
      author: comment.author,
      text: comment.text,
      parentId: comment.parentId,
      createdAt: comment.createdAt.toISOString(),
    };
  }

  async listByPost(postId: string, query: CommentQuery): Promise<CommentList> {
    const limit = query.limit;
    const cursor = query.cursor ? decodeCursor(query.cursor) : null;

    const where: Record<string, unknown> = {
      postId,
      parentId: null, // top-level comments only
    };

    if (cursor) {
      where.createdAt = { lt: new Date(cursor.createdAt as string) };
    }

    const comments = await this.prisma.comment.findMany({
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
      },
      orderBy: { createdAt: 'desc' },
      take: limit + 1,
    });

    const hasMore = comments.length > limit;
    const items = hasMore ? comments.slice(0, limit) : comments;

    const nextCursor =
      hasMore && items.length > 0
        ? encodeCursor({
            createdAt: items[items.length - 1].createdAt.toISOString(),
          })
        : null;

    return {
      items: items.map((c) => ({
        id: c.id,
        postId: c.postId,
        author: c.author,
        text: c.text,
        parentId: c.parentId,
        createdAt: c.createdAt.toISOString(),
      })),
      nextCursor,
    };
  }

  async remove(commentId: string, userId: string): Promise<{ ok: true }> {
    const comment = await this.prisma.comment.findUnique({
      where: { id: commentId },
    });
    if (!comment) throw notFound('Comment not found');
    if (comment.authorId !== userId) throw forbidden('Not your comment');

    await this.prisma.$transaction([
      this.prisma.comment.delete({ where: { id: commentId } }),
      this.prisma.post.update({
        where: { id: comment.postId },
        data: { commentCount: { decrement: 1 } },
      }),
    ]);

    return { ok: true };
  }
}
