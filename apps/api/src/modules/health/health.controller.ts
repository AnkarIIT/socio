import { Controller, Get } from '@nestjs/common';
import { Public } from '../../common/decorators/public.decorator';
import { PrismaService } from '../../prisma/prisma.service';
import { RedisService } from '../../redis/redis.service';
import { serviceUnavailable } from '../../common/errors/domain-exceptions';

/**
 * Liveness: process is up. Readiness: dependencies reachable.
 * Both are excluded from the /v1 prefix and from auth in main.ts / @Public().
 */
@Public()
@Controller()
export class HealthController {
  constructor(
    private readonly prisma: PrismaService,
    private readonly redis: RedisService,
  ) {}

  @Get('healthz')
  live(): { status: string; uptime: number } {
    return { status: 'ok', uptime: process.uptime() };
  }

  @Get('readyz')
  async ready(): Promise<{
    status: string;
    database: boolean;
    redis: boolean;
  }> {
    const database = await this.checkDatabase();
    if (!database) throw serviceUnavailable('Database unreachable');
    return { status: 'ok', database, redis: await this.redis.ping() };
  }

  private async checkDatabase(): Promise<boolean> {
    try {
      await this.prisma.$queryRaw`SELECT 1`;
      return true;
    } catch {
      return false;
    }
  }
}
