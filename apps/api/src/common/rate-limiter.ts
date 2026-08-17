import { Injectable } from '@nestjs/common';
import { RedisService } from '../redis/redis.service';

interface MemoryWindow {
  count: number;
  resetAt: number;
}

/**
 * Sliding fixed-window rate limiter. Redis-backed when available,
 * in-memory fallback when degraded (solo-instance only — fine for MVP).
 */
@Injectable()
export class RateLimiter {
  private readonly memory = new Map<string, MemoryWindow>();

  constructor(private readonly redis: RedisService) {}

  /** Returns true when the key has exceeded `limit` in `windowSeconds`. */
  async isRateLimited(
    key: string,
    limit: number,
    windowSeconds: number,
  ): Promise<boolean> {
    if (this.redis.available) {
      try {
        const count = await this.redis.raw.incr(key);
        if (count === 1) await this.redis.raw.expire(key, windowSeconds);
        return count > limit;
      } catch {
        /* fall through to in-memory */
      }
    }

    const now = Date.now();
    const entry = this.memory.get(key);
    if (!entry || entry.resetAt < now) {
      this.memory.set(key, { count: 1, resetAt: now + windowSeconds * 1000 });
      return false;
    }
    entry.count += 1;
    if (entry.count > limit) return true;
    this.memory.set(key, entry);
    return false;
  }
}
