import { Injectable, Logger, OnModuleInit } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { cert, getApps, initializeApp } from 'firebase-admin/app';
import { getMessaging } from 'firebase-admin/messaging';

@Injectable()
export class FirebaseGateway implements OnModuleInit {
  private readonly logger = new Logger(FirebaseGateway.name);
  private enabled = false;

  constructor(private readonly config: ConfigService) {}

  onModuleInit(): void {
    const rawCredentials = this.config.get<string>(
      'FIREBASE_SERVICE_ACCOUNT_JSON',
    );
    if (!rawCredentials) {
      this.logger.warn(
        'Push delivery disabled: FIREBASE_SERVICE_ACCOUNT_JSON is missing',
      );
      return;
    }
    try {
      const serviceAccount = JSON.parse(rawCredentials) as Parameters<
        typeof cert
      >[0];
      if (getApps().length === 0)
        initializeApp({ credential: cert(serviceAccount) });
      this.enabled = true;
    } catch (error) {
      this.logger.error('Firebase initialization failed', error);
    }
  }

  async send(
    tokens: string[],
    payload: { title: string; body: string; data?: Record<string, string> },
  ): Promise<boolean> {
    if (!this.enabled || tokens.length === 0) return false;
    await getMessaging().sendEachForMulticast({
      tokens,
      notification: { title: payload.title, body: payload.body },
      data: payload.data,
    });
    return true;
  }
}
