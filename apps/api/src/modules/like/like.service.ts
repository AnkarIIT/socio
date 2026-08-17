import { Injectable, Logger } from '@nestjs/common';
import type { LikeResponse } from '@bharat/contracts';
import { PrismaService } from '../../prisma/prisma.service';
import { notFound } from '../../common/errors/domain-exceptions';

@Injectable()
export class LikeService {
  private readonly logger = new Logger(LikeService.name);

  constructor(private readonly prisma: PrismaService) {}

  async toggle(postId: string, userId: string): Promise<LikeResponse> {
    const post = await this.prisma.post.findUnique({ where: { id: postId } });
    if (!post || post.status !== 'PUBLISHED') throw notFound('Post not found');

    const existing = await this.prisma.like.findUnique({
      where: { postId_userId: { postId, userId } },
    });

    if (existing) {
      await this.prisma.$transaction([
        this.prisma.like.delete({ where: { id: existing.id } }),
        this.prisma.post.update({
          where: { id: postId },
          data: { likeCount: { decrement: 1 } },
        }),
      ]);
      return { liked: false, likeCount: post.likeCount - 1 };
    }

    await this.prisma.$transaction([
      this.prisma.like.create({ data: { postId, userId } }),
      this.prisma.post.update({
        where: { id: postId },
        data: { likeCount: { increment: 1 } },
      }),
    ]);

    return { liked: true, likeCount: post.likeCount + 1 };
  }
}
