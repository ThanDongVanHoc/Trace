import { describe, expect, it } from 'vitest';
import { PasswordHasher } from './password-hasher.service.js';

describe('PasswordHasher', () => {
  const hasher = new PasswordHasher();

  it('round-trips a valid password and rejects another password', async () => {
    const encoded = await hasher.hash('a strong prototype password');

    await expect(
      hasher.verify('a strong prototype password', encoded),
    ).resolves.toBe(true);
    await expect(hasher.verify('a wrong password', encoded)).resolves.toBe(
      false,
    );
    expect(encoded).not.toContain('a strong prototype password');
  });

  it('uses a fresh salt for every hash', async () => {
    const first = await hasher.hash('same password');
    const second = await hasher.hash('same password');

    expect(first).not.toBe(second);
  });
});
