import { Body, Controller, Get, Param, Patch } from '@nestjs/common';
import { GetUserParamsSchema, UpdateMeSchema } from '@bharat/contracts';
import type { GetUserParams, UpdateMeDto, UserPublic } from '@bharat/contracts';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import type { AuthenticatedUser } from '../../common/guards/jwt-auth.guard';
import { ZodValidationPipe } from '../../common/pipes/zod-validation.pipe';
import { UserService } from './user.service';

@Controller('users')
export class UserController {
  constructor(private readonly users: UserService) {}

  @Get('me')
  me(@CurrentUser() user: AuthenticatedUser): Promise<UserPublic> {
    return this.users.me(user.userId);
  }

  @Patch('me')
  updateMe(
    @CurrentUser() user: AuthenticatedUser,
    @Body(new ZodValidationPipe(UpdateMeSchema)) dto: UpdateMeDto,
  ): Promise<UserPublic> {
    return this.users.updateMe(user.userId, dto);
  }

  @Get(':username')
  getByUsername(
    @Param(new ZodValidationPipe(GetUserParamsSchema)) params: GetUserParams,
  ): Promise<UserPublic> {
    return this.users.getByUsername(params.username);
  }
}
