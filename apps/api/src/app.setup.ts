import type { INestApplication } from '@nestjs/common';
import helmet from 'helmet';
import { requestIdMiddleware } from './common/request-id.middleware';

/**
 * Shared HTTP configuration used by both the production bootstrap (main.ts)
 * and the e2e test harness so tests exercise the real wiring.
 */
export function configureApp(app: INestApplication): INestApplication {
  app.use(helmet());
  app.use(requestIdMiddleware);
  app.setGlobalPrefix('v1', { exclude: ['healthz', 'readyz'] });
  app.enableCors({
    origin: (process.env.CORS_ORIGINS ?? '')
      .split(',')
      .map((o) => o.trim())
      .filter(Boolean),
    credentials: true,
  });
  app.enableShutdownHooks();
  return app;
}
