import { Controller, Post } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { CurrentUserId } from '../auth/decorators/current-user.decorator.js';
import { NotificationsService } from './notifications.service.js';

@ApiBearerAuth()
@ApiTags('notifications')
@Controller('notifications')
export class NotificationsController {
  constructor(private readonly notifications: NotificationsService) {}

  @Post('test')
  enqueueTest(@CurrentUserId() userId: string) {
    return this.notifications.enqueueTest(userId);
  }
}
