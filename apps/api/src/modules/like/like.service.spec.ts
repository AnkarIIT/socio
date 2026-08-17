jest.mock('../../prisma/prisma.service', () => ({
  PrismaService: jest.fn().mockImplementation(() => ({})),
}));

import { Test, TestingModule } from '@nestjs/testing';
import { LikeService } from './like.service';
import { PrismaService } from '../../prisma/prisma.service';
import { ErrorCode } from '@bharat/shared';

const mockPrisma = {
  post: { findUnique: jest.fn(), update: jest.fn() },
  like: { findUnique: jest.fn(), create: jest.fn(), delete: jest.fn() },
  $transaction: jest.fn((fns: any[]) => Promise.all(fns)),
} as any;

describe('LikeService', () => {
  let service: LikeService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [LikeService, { provide: PrismaService, useValue: mockPrisma }],
    }).compile();

    service = module.get(LikeService);
    jest.clearAllMocks();
  });

  it('adds a like', async () => {
    mockPrisma.post.findUnique.mockResolvedValue({
      id: 'p1', status: 'PUBLISHED', likeCount: 5,
    });
    mockPrisma.like.findUnique.mockResolvedValue(null);
    mockPrisma.like.create.mockResolvedValue({});
    mockPrisma.post.update.mockResolvedValue({});

    const result = await service.toggle('p1', 'u1');
    expect(result.liked).toBe(true);
    expect(result.likeCount).toBe(6);
  });

  it('removes a like', async () => {
    mockPrisma.post.findUnique.mockResolvedValue({
      id: 'p1', status: 'PUBLISHED', likeCount: 5,
    });
    mockPrisma.like.findUnique.mockResolvedValue({ id: 'l1' });
    mockPrisma.like.delete.mockResolvedValue({});
    mockPrisma.post.update.mockResolvedValue({});

    const result = await service.toggle('p1', 'u1');
    expect(result.liked).toBe(false);
    expect(result.likeCount).toBe(4);
  });

  it('throws for missing post', async () => {
    mockPrisma.post.findUnique.mockResolvedValue(null);
    await expect(service.toggle('missing', 'u1')).rejects.toMatchObject({
      code: ErrorCode.NOT_FOUND,
    });
  });
});
