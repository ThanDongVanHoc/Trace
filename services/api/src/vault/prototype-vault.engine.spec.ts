import { BadRequestException } from '@nestjs/common';
import { describe, expect, it } from 'vitest';
import { PrototypeVaultEngine } from './prototype-vault.engine.js';

describe('PrototypeVaultEngine', () => {
  it('round-trips AES-256-GCM with associated data', async () => {
    const engine = new PrototypeVaultEngine();
    const plaintext = Buffer.from('secret backpack location');
    const associatedData = Buffer.from('asset:123');
    const sealed = await engine.seal({ plaintext, associatedData });
    const opened = await engine.open({
      ...sealed,
      associatedData,
    });

    expect(opened.equals(plaintext)).toBe(true);
    expect(sealed.nonce).toHaveLength(12);
    expect(sealed.authenticationTag).toHaveLength(16);
  });

  it('uses a fresh nonce for each encryption', async () => {
    const engine = new PrototypeVaultEngine();
    const input = {
      plaintext: Buffer.from('same plaintext'),
      associatedData: Buffer.from('same aad'),
    };
    const first = await engine.seal(input);
    const second = await engine.seal(input);

    expect(first.nonce.equals(second.nonce)).toBe(false);
    expect(first.ciphertext.equals(second.ciphertext)).toBe(false);
  });

  it('rejects tampered ciphertext or associated data', async () => {
    const engine = new PrototypeVaultEngine();
    const associatedData = Buffer.from('asset:123');
    const sealed = await engine.seal({
      plaintext: Buffer.from('secret'),
      associatedData,
    });
    const tampered = Buffer.from(sealed.ciphertext);
    tampered[0] = (tampered[0] ?? 0) ^ 1;

    await expect(
      engine.open({
        ...sealed,
        ciphertext: tampered,
        associatedData,
      }),
    ).rejects.toBeInstanceOf(BadRequestException);
    await expect(
      engine.open({
        ...sealed,
        associatedData: Buffer.from('asset:another'),
      }),
    ).rejects.toBeInstanceOf(BadRequestException);
  });
});
