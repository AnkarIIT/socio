import { Injectable, Logger, OnModuleDestroy } from '@nestjs/common';
import Redis from 'ioredis';

/**
 * Redis wrapper with graceful degradation: every operation degrades instead
 * of throwing when Redis is down (architecture-decision.md §8.2).
 */
@Injectable()
export class RedisService implements OnModuleDestroy {
  private readonly logger = new Logger(RedisService.name);
  private readonly client: Redis;
  private readonly url: string;

  constructor() {
    this.url = process.env.REDIS_URL ?? 'redis://localhost:6379';
    this.client = new Redis(this.url, {
      lazyConnect: true,
      maxRetriesPerRequest: 1,
      enableOfflineQueue: false,
      retryStrategy: () => null,
    });
    this.client.on('error', (err) => {
      this.logger.warn(`Redis unavailable, running degraded: ${err.message}`);
    });
    this.client.connect().catch(() => {
      this.logger.warn(`Could not connect to Redis at ${this.url}`);
    });
  }

  get available(): boolean {
    return this.client.status === 'ready';
  }

  get raw(): Redis {
    return this.client;
  }

  async ping(): Promise<boolean> {
    if (!this.available) return false;
    try {
      return (await this.client.ping()) === 'PONG';
    } catch {
      return false;
    }
  }

  async get(key: string): Promise<string | null> {
    if (!this.available) return null;
    try {
      return await this.client.get(key);
    } catch {
      return null;
    }
  }

  async set(key: string, value: string, ttlSeconds?: number): Promise<void> {
    if (!this.available) return;
    try {
      if (ttlSeconds) await this.client.set(key, value, 'EX', ttlSeconds);
      else await this.client.set(key, value);
    } catch {
      /* degraded */
    }
  }

  async del(key: string): Promise<void> {
    if (!this.available) return;
    try {
      await this.client.del(key);
    } catch {
      /* degraded */
    }
  }

  async onModuleDestroy(): Promise<void> {
    await this.client.quit().catch(() => undefined);
  }
}
