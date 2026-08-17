import { Controller, Param, Post } from '@nestjs/common';
import { LikePostParamsSchema } from '@bharat/contracts';
import type { LikeResponse } from '@bharat/contracts';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import type { AuthenticatedUser } from '../../common/guards/jwt-auth.guard';
import { ZodValidationPipe } from '../../common/pipes/zod-validation.pipe';
import { LikeService } from './like.service';

@Controller('posts')
export class LikeController {
  constructor(private readonly likes: LikeService) {}

  @Post(':postId/like')
  toggle(
    @Param(new ZodValidationPipe(LikePostParamsSchema))
    params: { postId: string },
    @CurrentUser() user: AuthenticatedUser,
  ): Promise<LikeResponse> {
    return this.likes.toggle(params.postId, user.userId);
  }
}
