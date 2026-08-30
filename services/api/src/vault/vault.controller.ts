import { Body, Controller, Post } from '@nestjs/common';
import {
  ApiBadRequestResponse,
  ApiBearerAuth,
  ApiCreatedResponse,
  ApiOperation,
  ApiTags,
} from '@nestjs/swagger';
import { Throttle } from '@nestjs/throttler';
import {
  OpenedVaultResponseDto,
  OpenVaultDto,
  SealedVaultResponseDto,
  SealVaultDto,
} from './vault.dto.js';
import { VaultService } from './vault.service.js';

@ApiBearerAuth()
@ApiTags('vault')
@Controller('vault')
export class VaultController {
  constructor(private readonly vault: VaultService) {}

  @Post('seal')
  @ApiOperation({ summary: 'Seal a Base64 payload with AES-256-GCM and AAD' })
  @ApiCreatedResponse({ type: SealedVaultResponseDto })
  @ApiBadRequestResponse({ description: 'Invalid Base64 or payload size' })
  @Throttle({ default: { limit: 30, ttl: 60_000 } })
  seal(@Body() dto: SealVaultDto): Promise<SealedVaultResponseDto> {
    return this.vault.seal(dto);
  }

  @Post('open')
  @ApiOperation({ summary: 'Authenticate and open an AES-256-GCM bundle' })
  @ApiCreatedResponse({ type: OpenedVaultResponseDto })
  @ApiBadRequestResponse({ description: 'CRYPTO_FAILURE or invalid payload' })
  @Throttle({ default: { limit: 30, ttl: 60_000 } })
  open(@Body() dto: OpenVaultDto): Promise<OpenedVaultResponseDto> {
    return this.vault.open(dto);
  }
}
