import { ValidationPipe } from '@nestjs/common';
import { Test } from '@nestjs/testing';
import request from 'supertest';
import { afterAll, beforeAll, describe, expect, it } from 'vitest';
import { PrototypeVaultEngine } from './prototype-vault.engine.js';
import { VAULT_ENGINE } from './vault-engine.js';
import { VaultController } from './vault.controller.js';
import { VaultService } from './vault.service.js';

describe('VaultController', () => {
  let app: Awaited<ReturnType<typeof createTestApp>>;

  beforeAll(async () => {
    app = await createTestApp();
  });

  afterAll(async () => app.close());

  it('seals and opens a payload through HTTP', async () => {
    const plaintextBase64 = Buffer.from('my secret tag').toString('base64');
    const sealed = await request(app.getHttpServer())
      .post('/v1/vault/seal')
      .send({ plaintextBase64, associatedData: 'object:123' })
      .expect(201);

    expect(sealed.body.algorithm).toBe('AES-256-GCM');
    expect(sealed.body.ciphertextBase64).not.toBe(plaintextBase64);

    const opened = await request(app.getHttpServer())
      .post('/v1/vault/open')
      .send({
        nonceBase64: sealed.body.nonceBase64,
        ciphertextBase64: sealed.body.ciphertextBase64,
        authenticationTagBase64: sealed.body.authenticationTagBase64,
        associatedData: 'object:123',
        keyVersion: sealed.body.keyVersion,
      })
      .expect(201);
    expect(opened.body.plaintextBase64).toBe(plaintextBase64);
  });

  it('returns CRYPTO_FAILURE when AAD is changed', async () => {
    const sealed = await request(app.getHttpServer())
      .post('/v1/vault/seal')
      .send({
        plaintextBase64: Buffer.from('secret').toString('base64'),
        associatedData: 'object:123',
      })
      .expect(201);

    const response = await request(app.getHttpServer())
      .post('/v1/vault/open')
      .send({
        nonceBase64: sealed.body.nonceBase64,
        ciphertextBase64: sealed.body.ciphertextBase64,
        authenticationTagBase64: sealed.body.authenticationTagBase64,
        associatedData: 'object:tampered',
        keyVersion: sealed.body.keyVersion,
      })
      .expect(400);
    expect(response.body.code).toBe('CRYPTO_FAILURE');
  });
});

async function createTestApp() {
  const moduleFixture = await Test.createTestingModule({
    controllers: [VaultController],
    providers: [
      VaultService,
      PrototypeVaultEngine,
      { provide: VAULT_ENGINE, useExisting: PrototypeVaultEngine },
    ],
  }).compile();
  const app = moduleFixture.createNestApplication();
  app.setGlobalPrefix('v1');
  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,
      forbidNonWhitelisted: true,
      transform: true,
    }),
  );
  await app.init();
  return app;
}
