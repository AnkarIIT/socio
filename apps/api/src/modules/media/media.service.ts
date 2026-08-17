import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import type {
  PresignUploadDto,
  PresignResponse,
  CompleteUploadDto,
  MediaAsset,
} from '@bharat/contracts';
import { PrismaService } from '../../prisma/prisma.service';
import {
  validationError,
  notFound,
} from '../../common/errors/domain-exceptions';
import { randomBytes } from 'crypto';

const MAX_IMAGE_BYTES = 10 * 1024 * 1024;
const MAX_VIDEO_BYTES = 100 * 1024 * 1024;
const PRESIGN_TTL_SECONDS = 600;

@Injectable()
export class MediaService {
  private readonly logger = new Logger(MediaService.name);

  constructor(
    private readonly prisma: PrismaService,
    private readonly config: ConfigService,
  ) {}

  async presign(
    userId: string,
    dto: PresignUploadDto,
  ): Promise<PresignResponse> {
    if (dto.mimeType.startsWith('image/') && dto.sizeBytes > MAX_IMAGE_BYTES) {
      throw validationError('Image must be under 10MB');
    }
    if (dto.mimeType.startsWith('video/') && dto.sizeBytes > MAX_VIDEO_BYTES) {
      throw validationError('Video must be under 100MB');
    }

    const ext = this.extFromMime(dto.mimeType);
    const id = this.generateId();
    const key = `uploads/${userId}/${id}${ext}`;

    const asset = await this.prisma.mediaAsset.create({
      data: {
        userId,
        kind: dto.mimeType.startsWith('video/') ? 'VIDEO' : 'IMAGE',
        mimeType: dto.mimeType,
        sizeBytes: dto.sizeBytes,
        status: 'PROCESSING',
      },
    });

    const bucket = this.config.getOrThrow<string>('R2_BUCKET');
    const accountId = this.config.getOrThrow<string>('R2_ACCOUNT_ID');
    const uploadUrl = `https://${bucket}.${accountId}.r2.cloudflarestorage.com/${key}`;

    const expiresAt = new Date(Date.now() + PRESIGN_TTL_SECONDS * 1000);

    return {
      uploadUrl,
      assetId: asset.id,
      key,
      expiresAt: expiresAt.toISOString(),
    };
  }

  async complete(dto: CompleteUploadDto): Promise<MediaAsset> {
    const asset = await this.prisma.mediaAsset.findUnique({
      where: { id: dto.assetId },
    });
    if (!asset) throw notFound('Media asset not found');

    const updated = await this.prisma.mediaAsset.update({
      where: { id: dto.assetId },
      data: { status: 'READY' },
    });

    return {
      id: updated.id,
      kind: updated.kind,
      status: updated.status,
      mimeType: updated.mimeType,
      width: updated.width,
      height: updated.height,
      createdAt: updated.createdAt.toISOString(),
    };
  }

  private extFromMime(mime: string): string {
    const map: Record<string, string> = {
      'image/jpeg': '.jpg',
      'image/png': '.png',
      'image/webp': '.webp',
      'video/mp4': '.mp4',
      'video/quicktime': '.mov',
    };
    return map[mime] ?? '.bin';
  }

  private generateId(): string {
    return randomBytes(12).toString('hex');
  }
}
