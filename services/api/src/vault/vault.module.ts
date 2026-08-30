import { Module } from '@nestjs/common';
import { PrototypeVaultEngine } from './prototype-vault.engine.js';
import { VAULT_ENGINE } from './vault-engine.js';
import { VaultController } from './vault.controller.js';
import { VaultService } from './vault.service.js';

@Module({
  controllers: [VaultController],
  providers: [
    VaultService,
    PrototypeVaultEngine,
    { provide: VAULT_ENGINE, useExisting: PrototypeVaultEngine },
  ],
})
export class VaultModule {}
