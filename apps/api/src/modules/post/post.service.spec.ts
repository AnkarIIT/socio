jest.mock('../../prisma/prisma.service', () => ({
  PrismaService: jest.fn().mockImplementation(() => ({})),
}));

import { Test, TestingModule } from '@nestjs/testing';
import { PostService } from './post.service';
import { PrismaService } from '../../prisma/prisma.service';
import { ErrorCode } from '@bharat/shared';

const mockPrisma = {
  post: {
    create: jest.fn(),
    findUnique: jest.fn(),
    findMany: jest.fn(),
    update: jest.fn(),
    delete: jest.fn(),
  },
  mediaAsset: { updateMany: jest.fn() },
  like: {
    findUnique: jest.fn(),
    findMany: jest.fn(),
  },
} satisfies Record<string, { [k: string]: jest.Mock }>;

describe('PostService', () => {
  let service: PostService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        PostService,
        { provide: PrismaService, useValue: mockPrisma },
      ],
    }).compile();

    service = module.get(PostService);
    jest.clearAllMocks();
  });

  describe('create', () => {
    it('creates a text post', async () => {
      mockPrisma.post.create.mockResolvedValue({
        id: 'p1',
        text: 'Hello',
        langTag: null,
        likeCount: 0,
        commentCount: 0,
        shareCount: 0,
        publishedAt: new Date(),
        createdAt: new Date(),
        author: {
          id: 'u1',
          username: 'test',
          name: 'Test',
          avatarUrl: null,
          isVerified: false,
        },
        media: [],
      });

      const result = await service.create('u1', {
        text: 'Hello',
        status: 'PENDING',
      });
      expect(result.id).toBe('p1');
      expect(result.text).toBe('Hello');
    });

    it('rejects empty posts', async () => {
      await expect(
        service.create('u1', { status: 'PENDING' }),
      ).rejects.toMatchObject({ code: ErrorCode.VALIDATION });
    });
  });

  describe('getById', () => {
    it('returns published posts', async () => {
      mockPrisma.post.findUnique.mockResolvedValue({
        id: 'p1',
        status: 'PUBLISHED',
        text: 'Hi',
        langTag: null,
        likeCount: 0,
        commentCount: 0,
        shareCount: 0,
        publishedAt: new Date(),
        createdAt: new Date(),
        author: {
          id: 'u1',
          username: 'a',
          name: 'A',
          avatarUrl: null,
          isVerified: false,
        },
        media: [],
      });
      mockPrisma.like.findUnique.mockResolvedValue(null);

      const result = await service.getById('p1', 'u2');
      expect(result.id).toBe('p1');
      expect(result.reacted).toBe(false);
    });

    it('throws 404 for missing posts', async () => {
      mockPrisma.post.findUnique.mockResolvedValue(null);
      await expect(service.getById('missing')).rejects.toMatchObject({
        code: ErrorCode.NOT_FOUND,
      });
    });
  });

  describe('remove', () => {
    it('allows owner to delete', async () => {
      mockPrisma.post.findUnique.mockResolvedValue({
        id: 'p1',
        authorId: 'u1',
      });
      mockPrisma.post.delete.mockResolvedValue({});
      const result = await service.remove('p1', 'u1');
      expect(result).toEqual({ ok: true });
    });

    it('rejects non-owner', async () => {
      mockPrisma.post.findUnique.mockResolvedValue({
        id: 'p1',
        authorId: 'u1',
      });
      await expect(service.remove('p1', 'u2')).rejects.toMatchObject({
        code: ErrorCode.FORBIDDEN,
      });
    });
  });
});
