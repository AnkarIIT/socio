import { Body, Controller, Post as HttpPost } from '@nestjs/common';
import { PresignUploadSchema, CompleteUploadSchema } from '@bharat/contracts';
import type {
  PresignUploadDto,
  PresignResponse,
  CompleteUploadDto,
  MediaAsset,
} from '@bharat/contracts';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import type { AuthenticatedUser } from '../../common/guards/jwt-auth.guard';
import { ZodValidationPipe } from '../../common/pipes/zod-validation.pipe';
import { MediaService } from './media.service';

@Controller('media')
export class MediaController {
  constructor(private readonly media: MediaService) {}

  @HttpPost('presign')
  presign(
    @CurrentUser() user: AuthenticatedUser,
    @Body(new ZodValidationPipe(PresignUploadSchema)) dto: PresignUploadDto,
  ): Promise<PresignResponse> {
    return this.media.presign(user.userId, dto);
  }

  @HttpPost('complete')
  complete(
    @CurrentUser() user: AuthenticatedUser,
    @Body(new ZodValidationPipe(CompleteUploadSchema)) dto: CompleteUploadDto,
  ): Promise<MediaAsset> {
    return this.media.complete(dto);
  }
}
