import { IsBase64, IsString, Length, MaxLength } from 'class-validator';

export class SealVaultDto {
  @IsBase64()
  @Length(4, 1_500_000)
  plaintextBase64: string;

  @IsString()
  @Length(1, 256)
  associatedData: string;
}

export class OpenVaultDto {
  @IsBase64()
  @MaxLength(32)
  nonceBase64: string;

  @IsBase64()
  @MaxLength(1_500_000)
  ciphertextBase64: string;

  @IsBase64()
  @MaxLength(64)
  authenticationTagBase64: string;

  @IsString()
  @Length(1, 256)
  associatedData: string;

  @IsString()
  @Length(1, 100)
  keyVersion: string;
}

export class SealedVaultResponseDto {
  algorithm: 'AES-256-GCM';
  keyVersion: string;
  nonceBase64: string;
  ciphertextBase64: string;
  authenticationTagBase64: string;
  warnings: string[];
}

export class OpenedVaultResponseDto {
  plaintextBase64: string;
  keyVersion: string;
}
