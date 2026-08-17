import { Controller, Get, Param, Post, Query } from '@nestjs/common';
import {
  FollowParamsSchema,
  BlockParamsSchema,
  MuteParamsSchema,
  ProfileParamsSchema,
  FollowerQuerySchema,
} from '@bharat/contracts';
import type {
  FollowResponse,
  FollowerList,
  FollowerQuery,
  BlockResponse,
  MuteResponse,
  UserProfile,
  ProfileParams,
} from '@bharat/contracts';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { Public } from '../../common/decorators/public.decorator';
import type { AuthenticatedUser } from '../../common/guards/jwt-auth.guard';
import { ZodValidationPipe } from '../../common/pipes/zod-validation.pipe';
import { SocialService } from './social.service';

@Controller()
export class SocialController {
  constructor(private readonly social: SocialService) {}

  @Post('users/:userId/follow')
  follow(
    @CurrentUser() user: AuthenticatedUser,
    @Param(new ZodValidationPipe(FollowParamsSchema))
    params: { userId: string },
  ): Promise<FollowResponse> {
    return this.social.follow(user.userId, params.userId);
  }

  @Get('users/:userId/followers')
  followers(
    @CurrentUser() user: AuthenticatedUser,
    @Param(new ZodValidationPipe(FollowParamsSchema))
    params: { userId: string },
    @Query(new ZodValidationPipe(FollowerQuerySchema)) query: FollowerQuery,
  ): Promise<FollowerList> {
    return this.social.getFollowers(params.userId, query, user.userId);
  }

  @Get('users/:userId/following')
  following(
    @CurrentUser() user: AuthenticatedUser,
    @Param(new ZodValidationPipe(FollowParamsSchema))
    params: { userId: string },
    @Query(new ZodValidationPipe(FollowerQuerySchema)) query: FollowerQuery,
  ): Promise<FollowerList> {
    return this.social.getFollowing(params.userId, query, user.userId);
  }

  @Public()
  @Get('users/profile/:username')
  getProfile(
    @Param(new ZodValidationPipe(ProfileParamsSchema)) params: ProfileParams,
    @CurrentUser() user: AuthenticatedUser | undefined,
  ): Promise<UserProfile> {
    return this.social.getProfile(params, user?.userId);
  }

  @Post('users/:userId/block')
  block(
    @CurrentUser() user: AuthenticatedUser,
    @Param(new ZodValidationPipe(BlockParamsSchema)) params: { userId: string },
  ): Promise<BlockResponse> {
    return this.social.block(user.userId, params.userId);
  }

  @Post('users/:userId/mute')
  mute(
    @CurrentUser() user: AuthenticatedUser,
    @Param(new ZodValidationPipe(MuteParamsSchema)) params: { userId: string },
  ): Promise<MuteResponse> {
    return this.social.mute(user.userId, params.userId);
  }
}
