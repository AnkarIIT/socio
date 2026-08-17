import { Test, TestingModule } from '@nestjs/testing';
import { INestApplication } from '@nestjs/common';
import request from 'supertest';
import { AppModule } from './../src/app.module';
import { configureApp } from './../src/app.setup';

/**
 * e2e smoke: requires local Postgres + Redis (npm run db:up).
 * Run via: npm run test:e2e -w @bharat/api
 */
describe('Bharat API (e2e)', () => {
  let app: INestApplication;

  beforeEach(async () => {
    const moduleFixture: TestingModule = await Test.createTestingModule({
      imports: [AppModule],
    }).compile();

    app = configureApp(moduleFixture.createNestApplication());
    await app.init();
  });

  it('/healthz (GET) is public and reports ok', () => {
    return request(app.getHttpServer())
      .get('/healthz')
      .expect(200)
      .expect((res) => {
        expect(res.body).toMatchObject({ status: 'ok' });
      });
  });

  it('/readyz (GET) reports dependencies up', () => {
    return request(app.getHttpServer())
      .get('/readyz')
      .expect(200)
      .expect((res) => {
        expect(res.body).toMatchObject({
          status: 'ok',
          database: true,
          redis: true,
        });
      });
  });

  it('/v1/users/me (GET) rejects missing token with 401 envelope', () => {
    return request(app.getHttpServer())
      .get('/v1/users/me')
      .expect(401)
      .expect((res) => {
        const body = res.body as {
          error: { code: string; requestId?: string };
        };
        expect(body.error.code).toBe('UNAUTHORIZED');
        expect(body.error.requestId).toBeDefined();
      });
  });

  afterEach(async () => {
    await app.close();
  });
});
