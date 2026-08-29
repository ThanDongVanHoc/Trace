import { Body, Controller, Get, HttpCode, Post, Req } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import type { AuthenticatedRequest } from './guards/access-token.guard.js';
import { CurrentUserId } from './decorators/current-user.decorator.js';
import { Public } from './decorators/public.decorator.js';
import { AuthService } from './auth.service.js';
import { LoginDto } from './dto/login.dto.js';
import { RefreshDto } from './dto/refresh.dto.js';
import { RegisterDto } from './dto/register.dto.js';

@ApiTags('auth')
@Controller('auth')
export class AuthController {
  constructor(private readonly auth: AuthService) {}

  @Public()
  @Post('register')
  register(@Body() dto: RegisterDto) {
    return this.auth.register(dto);
  }

  @Public()
  @HttpCode(200)
  @Post('login')
  login(@Body() dto: LoginDto) {
    return this.auth.login(dto);
  }

  @Public()
  @HttpCode(200)
  @Post('refresh')
  refresh(@Body() dto: RefreshDto) {
    return this.auth.refresh(dto.refreshToken);
  }

  @ApiBearerAuth()
  @HttpCode(204)
  @Post('logout')
  logout(
    @CurrentUserId() userId: string,
    @Req() request: AuthenticatedRequest,
  ) {
    return this.auth.logout(userId, request.auth.sessionId);
  }

  @ApiBearerAuth()
  @Get('me')
  me(@CurrentUserId() userId: string) {
    return this.auth.getProfile(userId);
  }
}
