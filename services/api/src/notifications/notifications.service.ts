import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import {
  NotificationOutboxEntity,
  NotificationStatus,
} from './notification-outbox.entity.js';

@Injectable()
export class NotificationsService {
  constructor(
    @InjectRepository(NotificationOutboxEntity)
    private readonly outbox: Repository<NotificationOutboxEntity>,
  ) {}

  enqueueTest(userId: string) {
    return this.outbox.save(
      this.outbox.create({
        userId,
        type: 'test',
        payload: {
          title: 'TRACE đã sẵn sàng',
          body: 'Thông báo trên thiết bị của bạn đang hoạt động.',
          data: { route: '/' },
        },
        status: NotificationStatus.PENDING,
        scheduledAt: new Date(),
        attempts: 0,
        lastError: null,
      }),
    );
  }
}
