import { BadRequestException, Injectable } from '@nestjs/common';
import { createCipheriv, createDecipheriv, randomBytes } from 'node:crypto';
import type {
  VaultEngine,
  VaultOpenInput,
  VaultSealInput,
  VaultSealedData,
} from './vault-engine.js';

@Injectable()
export class PrototypeVaultEngine implements VaultEngine {
  private readonly key = randomBytes(32);
  private readonly keyVersion = 'prototype-ephemeral-v1';

  async seal(input: VaultSealInput): Promise<VaultSealedData> {
    const nonce = randomBytes(12);
    const cipher = createCipheriv('aes-256-gcm', this.key, nonce);
    cipher.setAAD(input.associatedData);
    const ciphertext = Buffer.concat([
      cipher.update(input.plaintext),
      cipher.final(),
    ]);
    return Promise.resolve({
      algorithm: 'AES-256-GCM',
      keyVersion: this.keyVersion,
      nonce,
      ciphertext,
      authenticationTag: cipher.getAuthTag(),
      warnings: [
        'Prototype key is memory-only and is replaced whenever the API restarts.',
      ],
    });
  }

  async open(input: VaultOpenInput): Promise<Buffer> {
    if (input.keyVersion !== this.keyVersion) {
      throw this.cryptoFailure();
    }
    try {
      const decipher = createDecipheriv('aes-256-gcm', this.key, input.nonce);
      decipher.setAAD(input.associatedData);
      decipher.setAuthTag(input.authenticationTag);
      return Promise.resolve(
        Buffer.concat([decipher.update(input.ciphertext), decipher.final()]),
      );
    } catch {
      throw this.cryptoFailure();
    }
  }

  private cryptoFailure(): BadRequestException {
    return new BadRequestException({
      code: 'CRYPTO_FAILURE',
      message: 'Ciphertext, AAD, authentication tag or key version is invalid',
    });
  }
}
