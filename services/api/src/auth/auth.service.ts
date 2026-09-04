import {
  ConflictException,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { JwtService } from '@nestjs/jwt';
import { InjectRepository } from '@nestjs/typeorm';
import { randomUUID } from 'node:crypto';
import { Repository } from 'typeorm';
import { LoginDto } from './dto/login.dto.js';
import { RegisterDto } from './dto/register.dto.js';
import { SessionEntity } from './entities/session.entity.js';
import { UserEntity } from './entities/user.entity.js';
import { PasswordHasher } from './password-hasher.service.js';

interface RefreshPayload {
  sub: string;
  sid: string;
  type: 'refresh';
}

export interface AuthResponse {
  user: { id: string; email: string; displayName: string };
  accessToken: string;
  refreshToken: string;
  expiresInSeconds: number;
}

@Injectable()
export class AuthService {
  private readonly accessTtlSeconds: number;
  private readonly refreshTtlSeconds: number;

  constructor(
    @InjectRepository(UserEntity)
    private readonly users: Repository<UserEntity>,
    @InjectRepository(SessionEntity)
    private readonly sessions: Repository<SessionEntity>,
    private readonly jwt: JwtService,
    private readonly config: ConfigService,
    private readonly hasher: PasswordHasher,
  ) {
    this.accessTtlSeconds = Number(config.get('JWT_ACCESS_TTL_SECONDS', 900));
    this.refreshTtlSeconds = Number(
      config.get('JWT_REFRESH_TTL_SECONDS', 2_592_000),
    );
  }

  async register(dto: RegisterDto): Promise<AuthResponse> {
    const email = dto.email.trim().toLowerCase();
    if (await this.users.exists({ where: { email } })) {
      throw new ConflictException('Email is already registered');
    }
    const user = await this.users.save(
      this.users.create({
        email,
        displayName: dto.displayName.trim(),
        passwordHash: await this.hasher.hash(dto.password),
      }),
    );
    return this.createSession(user);
  }

  async login(dto: LoginDto): Promise<AuthResponse> {
    const email = dto.email.trim().toLowerCase();
    const user = await this.users
      .createQueryBuilder('user')
      .addSelect('user.passwordHash')
      .where('user.email = :email', { email })
      .getOne();
    if (!user || !(await this.hasher.verify(dto.password, user.passwordHash))) {
      throw new UnauthorizedException('Invalid email or password');
    }
    return this.createSession(user);
  }

  async refresh(rawToken: string): Promise<AuthResponse> {
    let payload: RefreshPayload;
    try {
      payload = await this.jwt.verifyAsync<RefreshPayload>(rawToken, {
        secret: this.config.getOrThrow<string>('JWT_REFRESH_SECRET'),
      });
    } catch {
      throw new UnauthorizedException('Invalid or expired refresh token');
    }
    if (payload.type !== 'refresh') {
      throw new UnauthorizedException('Invalid token type');
    }

    const session = await this.sessions.findOne({
      where: { id: payload.sid, userId: payload.sub },
      relations: { user: true },
    });
    const isValid =
      session &&
      !session.revokedAt &&
      session.expiresAt.getTime() > Date.now() &&
      (await this.hasher.verify(rawToken, session.refreshTokenHash));
    if (!isValid || !session) {
      if (session && !session.revokedAt) {
        session.revokedAt = new Date();
        await this.sessions.save(session);
      }
      throw new UnauthorizedException(
        'Refresh token reuse or expiration detected',
      );
    }

    session.lastUsedAt = new Date();
    return this.issueTokens(session.user, session);
  }

  async logout(userId: string, sessionId: string): Promise<void> {
    await this.sessions.update(
      { id: sessionId, userId },
      { revokedAt: new Date() },
    );
  }

  async getProfile(userId: string) {
    const user = await this.users.findOneByOrFail({ id: userId });
    return { id: user.id, email: user.email, displayName: user.displayName };
  }

  private async createSession(user: UserEntity): Promise<AuthResponse> {
    const session = await this.sessions.save(
      this.sessions.create({
        userId: user.id,
        refreshTokenHash: 'pending',
        expiresAt: new Date(Date.now() + this.refreshTtlSeconds * 1000),
        revokedAt: null,
        lastUsedAt: null,
      }),
    );
    return this.issueTokens(user, session);
  }

  private async issueTokens(
    user: UserEntity,
    session: SessionEntity,
  ): Promise<AuthResponse> {
    const accessToken = await this.jwt.signAsync(
      { sub: user.id, sid: session.id, type: 'access', jti: randomUUID() },
      {
        secret: this.config.getOrThrow<string>('JWT_ACCESS_SECRET'),
        expiresIn: this.accessTtlSeconds,
      },
    );
    const refreshToken = await this.jwt.signAsync(
      { sub: user.id, sid: session.id, type: 'refresh', jti: randomUUID() },
      {
        secret: this.config.getOrThrow<string>('JWT_REFRESH_SECRET'),
        expiresIn: this.refreshTtlSeconds,
      },
    );
    session.refreshTokenHash = await this.hasher.hash(refreshToken);
    session.lastUsedAt = new Date();
    await this.sessions.save(session);

    return {
      user: { id: user.id, email: user.email, displayName: user.displayName },
      accessToken,
      refreshToken,
      expiresInSeconds: this.accessTtlSeconds,
    };
  }
}
