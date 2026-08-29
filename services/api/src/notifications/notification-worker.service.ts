import { Injectable, Logger } from '@nestjs/common';
import { Interval } from '@nestjs/schedule';
import { InjectRepository } from '@nestjs/typeorm';
import { LessThanOrEqual, Repository } from 'typeorm';
import { DeviceEntity } from '../devices/device.entity.js';
import { FirebaseGateway } from './firebase-gateway.service.js';
import {
  NotificationOutboxEntity,
  NotificationStatus,
} from './notification-outbox.entity.js';

@Injectable()
export class NotificationWorker {
  private readonly logger = new Logger(NotificationWorker.name);
  private running = false;

  constructor(
    @InjectRepository(NotificationOutboxEntity)
    private readonly outbox: Repository<NotificationOutboxEntity>,
    @InjectRepository(DeviceEntity)
    private readonly devices: Repository<DeviceEntity>,
    private readonly firebase: FirebaseGateway,
  ) {}

  @Interval(15_000)
  async dispatch(): Promise<void> {
    if (this.running) return;
    this.running = true;
    try {
      const jobs = await this.outbox.find({
        where: {
          status: NotificationStatus.PENDING,
          scheduledAt: LessThanOrEqual(new Date()),
        },
        order: { createdAt: 'ASC' },
        take: 25,
      });
      for (const job of jobs) await this.dispatchOne(job);
    } finally {
      this.running = false;
    }
  }

  private async dispatchOne(job: NotificationOutboxEntity): Promise<void> {
    try {
      const devices = await this.devices.findBy({
        userId: job.userId,
        notificationsEnabled: true,
      });
      const tokens = devices
        .map((device) => device.pushToken)
        .filter((token): token is string => Boolean(token));
      const sent = await this.firebase.send(tokens, job.payload);
      job.status = sent ? NotificationStatus.SENT : NotificationStatus.FAILED;
      job.lastError = sent
        ? null
        : 'No configured provider or registered device';
    } catch (error) {
      job.status = NotificationStatus.FAILED;
      job.lastError =
        error instanceof Error ? error.message : 'Unknown push error';
      this.logger.error(`Notification ${job.id} failed`, error);
    }
    job.attempts += 1;
    await this.outbox.save(job);
  }
}
