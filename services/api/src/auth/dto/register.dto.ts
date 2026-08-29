import { ApiProperty } from '@nestjs/swagger';
import { IsEmail, IsString, Length, MaxLength } from 'class-validator';

export class RegisterDto {
  @ApiProperty({ example: 'minh@example.com' })
  @IsEmail()
  @MaxLength(320)
  email: string;

  @ApiProperty({ minLength: 10, maxLength: 128 })
  @IsString()
  @Length(10, 128)
  password: string;

  @ApiProperty({ example: 'Minh Nguyen' })
  @IsString()
  @Length(2, 80)
  displayName: string;
}
