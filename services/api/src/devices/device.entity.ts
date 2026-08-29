import {
  Column,
  CreateDateColumn,
  Entity,
  Index,
  PrimaryGeneratedColumn,
  Unique,
  UpdateDateColumn,
} from 'typeorm';

export enum DevicePlatform {
  ANDROID = 'android',
  IOS = 'ios',
}

@Entity({ name: 'devices' })
@Unique('uq_devices_user_installation', ['userId', 'installationId'])
export class DeviceEntity {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Index()
  @Column({ name: 'user_id', type: 'uuid' })
  userId: string;

  @Column({ name: 'installation_id', type: 'varchar', length: 100 })
  installationId: string;

  @Column({ type: 'enum', enum: DevicePlatform })
  platform: DevicePlatform;

  @Column({ name: 'push_token', type: 'text', nullable: true })
  pushToken: string | null;

  @Column({ type: 'varchar', length: 20, default: 'vi' })
  locale: string;

  @Column({ name: 'notifications_enabled', type: 'boolean', default: true })
  notificationsEnabled: boolean;

  @Column({ name: 'last_seen_at', type: 'timestamptz' })
  lastSeenAt: Date;

  @CreateDateColumn({ name: 'created_at', type: 'timestamptz' })
  createdAt: Date;

  @UpdateDateColumn({ name: 'updated_at', type: 'timestamptz' })
  updatedAt: Date;
}
