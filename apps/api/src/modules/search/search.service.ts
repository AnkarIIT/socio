import { Injectable, Logger } from '@nestjs/common';
import type { SearchQuery, SearchResult } from '@bharat/contracts';
import { PrismaService } from '../../prisma/prisma.service';

@Injectable()
export class SearchService {
  private readonly logger = new Logger(SearchService.name);

  constructor(private readonly prisma: PrismaService) {}

  async search(query: SearchQuery): Promise<SearchResult> {
    const term = query.q;

    const [users, posts, hashtags] = await Promise.all([
      this.searchUsers(term),
      this.searchPosts(term),
      this.searchHashtags(term),
    ]);

    return { users, posts, hashtags };
  }

  private async searchUsers(term: string) {
    return this.prisma.user.findMany({
      where: {
        bannedAt: null,
        OR: [
          { username: { contains: term, mode: 'insensitive' } },
          { name: { contains: term, mode: 'insensitive' } },
        ],
      },
      select: {
        id: true,
        username: true,
        name: true,
        avatarUrl: true,
        isVerified: true,
      },
      take: 10,
    });
  }

  private async searchPosts(term: string) {
    const rows = await this.prisma.post.findMany({
      where: {
        status: 'PUBLISHED',
        text: { contains: term, mode: 'insensitive' },
        author: { bannedAt: null },
      },
      select: {
        id: true,
        text: true,
        author: {
          select: {
            username: true,
            name: true,
            avatarUrl: true,
          },
        },
        createdAt: true,
      },
      orderBy: { publishedAt: 'desc' },
      take: 20,
    });

    return rows.map((r) => ({
      id: r.id,
      text: r.text,
      author: r.author,
      createdAt: r.createdAt.toISOString(),
    }));
  }

  private async searchHashtags(term: string) {
    return this.prisma.hashtag.findMany({
      where: {
        tag: { contains: term, mode: 'insensitive' },
      },
      select: {
        tag: true,
        postCount: true,
      },
      orderBy: { postCount: 'desc' },
      take: 10,
    });
  }
}
