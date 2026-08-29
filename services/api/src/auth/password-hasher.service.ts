import { Injectable } from '@nestjs/common';
import { randomBytes, scrypt, timingSafeEqual } from 'node:crypto';

const KEY_LENGTH = 64;

@Injectable()
export class PasswordHasher {
  async hash(value: string): Promise<string> {
    const salt = randomBytes(16);
    const derived = await this.derive(value, salt);
    return `scrypt$v1$${salt.toString('base64url')}$${derived.toString('base64url')}`;
  }

  async verify(value: string, encoded: string): Promise<boolean> {
    const [algorithm, version, saltValue, hashValue] = encoded.split('$');
    if (
      algorithm !== 'scrypt' ||
      version !== 'v1' ||
      !saltValue ||
      !hashValue
    ) {
      return false;
    }
    const salt = Buffer.from(saltValue, 'base64url');
    const expected = Buffer.from(hashValue, 'base64url');
    const actual = await this.derive(value, salt);
    return (
      expected.length === actual.length && timingSafeEqual(expected, actual)
    );
  }

  private derive(value: string, salt: Buffer): Promise<Buffer> {
    return new Promise((resolve, reject) => {
      scrypt(value, salt, KEY_LENGTH, (error, key) => {
        if (error) reject(error);
        else resolve(key);
      });
    });
  }
}
