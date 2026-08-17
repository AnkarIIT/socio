import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  Post,
  Query,
} from '@nestjs/common';
import {
  CreateCommentSchema,
  CommentParamsSchema,
  CommentIdParamsSchema,
  CommentQuerySchema,
} from '@bharat/contracts';
import type {
  CreateCommentDto,
  CommentParams,
  CommentQuery,
  CommentPublic,
  CommentList,
} from '@bharat/contracts';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { Public } from '../../common/decorators/public.decorator';
import type { AuthenticatedUser } from '../../common/guards/jwt-auth.guard';
import { ZodValidationPipe } from '../../common/pipes/zod-validation.pipe';
import { CommentService } from './comment.service';

@Controller('posts')
export class CommentController {
  constructor(private readonly comments: CommentService) {}

  @Post(':postId/comments')
  create(
    @Param(new ZodValidationPipe(CommentParamsSchema)) params: CommentParams,
    @CurrentUser() user: AuthenticatedUser,
    @Body(new ZodValidationPipe(CreateCommentSchema)) dto: CreateCommentDto,
  ): Promise<CommentPublic> {
    return this.comments.create(params.postId, user.userId, dto);
  }

  @Public()
  @Get(':postId/comments')
  listByPost(
    @Param(new ZodValidationPipe(CommentParamsSchema)) params: CommentParams,
    @Query(new ZodValidationPipe(CommentQuerySchema)) query: CommentQuery,
  ): Promise<CommentList> {
    return this.comments.listByPost(params.postId, query);
  }

  @Delete('comments/:id')
  remove(
    @Param(new ZodValidationPipe(CommentIdParamsSchema)) params: { id: string },
    @CurrentUser() user: AuthenticatedUser,
  ): Promise<{ ok: true }> {
    return this.comments.remove(params.id, user.userId);
  }
}
