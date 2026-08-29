import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { DeviceEntity } from '../devices/device.entity.js';
import { FirebaseGateway } from './firebase-gateway.service.js';
import { NotificationOutboxEntity } from './notification-outbox.entity.js';
import { NotificationWorker } from './notification-worker.service.js';
import { NotificationsController } from './notifications.controller.js';
import { NotificationsService } from './notifications.service.js';

@Module({
  imports: [TypeOrmModule.forFeature([NotificationOutboxEntity, DeviceEntity])],
  controllers: [NotificationsController],
  providers: [NotificationsService, NotificationWorker, FirebaseGateway],
})
export class NotificationsModule {}
