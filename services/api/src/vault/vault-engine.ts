export const VAULT_ENGINE = Symbol('VAULT_ENGINE');

export interface VaultSealInput {
  plaintext: Buffer;
  associatedData: Buffer;
}

export interface VaultOpenInput {
  nonce: Buffer;
  ciphertext: Buffer;
  authenticationTag: Buffer;
  associatedData: Buffer;
  keyVersion: string;
}

export interface VaultSealedData {
  algorithm: 'AES-256-GCM';
  keyVersion: string;
  nonce: Buffer;
  ciphertext: Buffer;
  authenticationTag: Buffer;
  warnings: readonly string[];
}

export interface VaultEngine {
  seal(input: VaultSealInput): Promise<VaultSealedData>;
  open(input: VaultOpenInput): Promise<Buffer>;
}
