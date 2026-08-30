import { Module } from '@nestjs/common';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { APP_GUARD } from '@nestjs/core';
import { JwtModule } from '@nestjs/jwt';
import { ScheduleModule } from '@nestjs/schedule';
import { ThrottlerGuard, ThrottlerModule } from '@nestjs/throttler';
import { TypeOrmModule } from '@nestjs/typeorm';
import { AuthModule } from './auth/auth.module.js';
import { AccessTokenGuard } from './auth/guards/access-token.guard.js';
import { DevicesModule } from './devices/devices.module.js';
import { EnrollmentsModule } from './enrollments/enrollments.module.js';
import { HealthController } from './health/health.controller.js';
import { MemoryModule } from './memory/memory.module.js';
import { NotificationsModule } from './notifications/notifications.module.js';
import { ObjectsModule } from './objects/objects.module.js';
import { RecognitionsModule } from './recognitions/recognitions.module.js';
import { SightingsModule } from './sightings/sightings.module.js';
import { VaultModule } from './vault/vault.module.js';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
      envFilePath: ['.env', '../../.env'],
    }),
    JwtModule.register({ global: true }),
    ScheduleModule.forRoot(),
    ThrottlerModule.forRoot([{ ttl: 60_000, limit: 120 }]),
    TypeOrmModule.forRootAsync({
      inject: [ConfigService],
      useFactory: (config: ConfigService) => ({
        type: 'postgres' as const,
        url: config.getOrThrow<string>('DATABASE_URL'),
        autoLoadEntities: true,
        synchronize: config.get('DB_SYNCHRONIZE', 'false') === 'true',
        ssl:
          config.get('DATABASE_SSL', 'false') === 'true'
            ? { rejectUnauthorized: true }
            : false,
      }),
    }),
    AuthModule,
    DevicesModule,
    EnrollmentsModule,
    MemoryModule,
    ObjectsModule,
    RecognitionsModule,
    SightingsModule,
    NotificationsModule,
    VaultModule,
  ],
  controllers: [HealthController],
  providers: [
    { provide: APP_GUARD, useClass: ThrottlerGuard },
    { provide: APP_GUARD, useClass: AccessTokenGuard },
  ],
})
export class AppModule {}
