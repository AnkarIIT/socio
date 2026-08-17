import { Injectable } from '@nestjs/common';
import { UpdateMeDto, UserPublic } from '@bharat/contracts';
import { PrismaService } from '../../prisma/prisma.service';
import { notFound } from '../../common/errors/domain-exceptions';
import { toUserPublic } from './user.serializer';

@Injectable()
export class UserService {
  constructor(private readonly prisma: PrismaService) {}

  async me(userId: string): Promise<UserPublic> {
    const user = await this.prisma.user.findUnique({ where: { id: userId } });
    if (!user) throw notFound('User not found');
    return toUserPublic(user);
  }

  async updateMe(userId: string, dto: UpdateMeDto): Promise<UserPublic> {
    // Username conflicts surface as P2002 -> 409 via the global filter.
    const user = await this.prisma.user.update({
      where: { id: userId },
      data: { ...dto },
    });
    return toUserPublic(user);
  }

  async getByUsername(username: string): Promise<UserPublic> {
    const user = await this.prisma.user.findUnique({ where: { username } });
    if (!user || user.bannedAt) throw notFound('User not found');
    return toUserPublic(user);
  }
}
