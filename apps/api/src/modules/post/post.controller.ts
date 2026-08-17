import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  Patch,
  Post,
  Query,
} from '@nestjs/common';
import {
  CreatePostSchema,
  UpdatePostSchema,
  PostParamsSchema,
  FeedQuerySchema,
} from '@bharat/contracts';
import type {
  CreatePostDto,
  UpdatePostDto,
  PostParams,
  PostPublic,
  PostList,
  FeedQuery,
} from '@bharat/contracts';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { Public } from '../../common/decorators/public.decorator';
import type { AuthenticatedUser } from '../../common/guards/jwt-auth.guard';
import { ZodValidationPipe } from '../../common/pipes/zod-validation.pipe';
import { PostService } from './post.service';

@Controller('posts')
export class PostController {
  constructor(private readonly posts: PostService) {}

  @Post()
  create(
    @CurrentUser() user: AuthenticatedUser,
    @Body(new ZodValidationPipe(CreatePostSchema)) dto: CreatePostDto,
  ): Promise<PostPublic> {
    return this.posts.create(user.userId, dto);
  }

  @Public()
  @Get('feed')
  feed(
    @CurrentUser() user: AuthenticatedUser | undefined,
    @Query(new ZodValidationPipe(FeedQuerySchema)) query: FeedQuery,
  ): Promise<PostList> {
    return this.posts.feed(user?.userId ?? '', query);
  }

  @Public()
  @Get(':id')
  getById(
    @Param(new ZodValidationPipe(PostParamsSchema)) params: PostParams,
    @CurrentUser() user: AuthenticatedUser | undefined,
  ): Promise<PostPublic> {
    return this.posts.getById(params.id, user?.userId);
  }

  @Patch(':id')
  update(
    @Param(new ZodValidationPipe(PostParamsSchema)) params: PostParams,
    @CurrentUser() user: AuthenticatedUser,
    @Body(new ZodValidationPipe(UpdatePostSchema)) dto: UpdatePostDto,
  ): Promise<PostPublic> {
    return this.posts.update(params.id, user.userId, dto);
  }

  @Delete(':id')
  remove(
    @Param(new ZodValidationPipe(PostParamsSchema)) params: PostParams,
    @CurrentUser() user: AuthenticatedUser,
  ): Promise<{ ok: true }> {
    return this.posts.remove(params.id, user.userId);
  }
}
