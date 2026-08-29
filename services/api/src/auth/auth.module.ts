import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { AuthController } from './auth.controller.js';
import { AuthService } from './auth.service.js';
import { SessionEntity } from './entities/session.entity.js';
import { UserEntity } from './entities/user.entity.js';
import { PasswordHasher } from './password-hasher.service.js';

@Module({
  imports: [TypeOrmModule.forFeature([UserEntity, SessionEntity])],
  controllers: [AuthController],
  providers: [AuthService, PasswordHasher],
  exports: [AuthService],
})
export class AuthModule {}
