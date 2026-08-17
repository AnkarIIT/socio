jest.mock('../../prisma/prisma.service', () => ({
  PrismaService: jest.fn().mockImplementation(() => ({})),
}));

import { Test, TestingModule } from '@nestjs/testing';
import { CommentService } from './comment.service';
import { PrismaService } from '../../prisma/prisma.service';
import { ErrorCode } from '@bharat/shared';

const mockPrisma = {
  post: { findUnique: jest.fn(), update: jest.fn() },
  comment: {
    create: jest.fn(),
    findUnique: jest.fn(),
    findMany: jest.fn(),
    delete: jest.fn(),
  },
  $transaction: jest.fn((fns: any[]) => Promise.all(fns)),
} as any;

describe('CommentService', () => {
  let service: CommentService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [CommentService, { provide: PrismaService, useValue: mockPrisma }],
    }).compile();

    service = module.get(CommentService);
    jest.clearAllMocks();
  });

  it('creates a comment', async () => {
    mockPrisma.post.findUnique.mockResolvedValue({ id: 'p1', status: 'PUBLISHED' });
    mockPrisma.comment.create.mockResolvedValue({
      id: 'c1',
      postId: 'p1',
      text: 'Nice!',
      parentId: null,
      createdAt: new Date(),
      author: { id: 'u1', username: 'a', name: 'A', avatarUrl: null, isVerified: false },
    });

    const result = await service.create('p1', 'u1', { text: 'Nice!' });
    expect(result.id).toBe('c1');
    expect(result.text).toBe('Nice!');
  });

  it('rejects comment on missing post', async () => {
    mockPrisma.post.findUnique.mockResolvedValue(null);
    await expect(
      service.create('missing', 'u1', { text: 'hi' }),
    ).rejects.toMatchObject({ code: ErrorCode.NOT_FOUND });
  });

  it('deletes own comment', async () => {
    mockPrisma.comment.findUnique.mockResolvedValue({ id: 'c1', authorId: 'u1', postId: 'p1' });
    mockPrisma.comment.delete.mockResolvedValue({});
    const result = await service.remove('c1', 'u1');
    expect(result).toEqual({ ok: true });
  });

  it('rejects delete of others comment', async () => {
    mockPrisma.comment.findUnique.mockResolvedValue({ id: 'c1', authorId: 'u2', postId: 'p1' });
    await expect(service.remove('c1', 'u1')).rejects.toMatchObject({
      code: ErrorCode.FORBIDDEN,
    });
  });
});
