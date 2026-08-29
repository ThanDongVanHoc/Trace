import { Column, Entity, Index, PrimaryColumn } from 'typeorm';

@Entity({ name: 'sightings' })
@Index('idx_sightings_object_time', ['objectId', 'detectedAt'])
@Index('idx_sightings_user_time', ['userId', 'detectedAt'])
export class SightingEntity {
  @PrimaryColumn({ type: 'uuid' })
  id: string;

  @Column({ name: 'user_id', type: 'uuid' })
  userId: string;

  @Column({ name: 'object_id', type: 'uuid' })
  objectId: string;

  @Column({ name: 'detected_at', type: 'timestamptz' })
  detectedAt: Date;

  @Column({ type: 'double precision', nullable: true })
  latitude: number | null;

  @Column({ type: 'double precision', nullable: true })
  longitude: number | null;

  @Column({ name: 'accuracy_meters', type: 'real', nullable: true })
  accuracyMeters: number | null;

  @Column({ type: 'real' })
  confidence: number;

  @Column({ name: 'evidence_asset_id', type: 'uuid', nullable: true })
  evidenceAssetId: string | null;

  @Column({
    name: 'created_at',
    type: 'timestamptz',
    default: () => 'CURRENT_TIMESTAMP',
  })
  createdAt: Date;
}
