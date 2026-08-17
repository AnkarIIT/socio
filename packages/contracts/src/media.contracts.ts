import { z } from 'zod';

export const PresignUploadSchema = z
  .object({
    filename: z.string().min(1).max(255),
    mimeType: z.enum([
      'image/jpeg',
      'image/png',
      'image/webp',
      'video/mp4',
      'video/quicktime',
    ]),
    sizeBytes: z.number().int().min(1).max(100 * 1024 * 1024),
  })
  .strict();

export type PresignUploadDto = z.infer<typeof PresignUploadSchema>;

export const PresignResponseSchema = z.object({
  uploadUrl: z.string().url(),
  assetId: z.string(),
  key: z.string(),
  expiresAt: z.string(),
});

export type PresignResponse = z.infer<typeof PresignResponseSchema>;

export const CompleteUploadSchema = z
  .object({
    assetId: z.string(),
  })
  .strict();

export type CompleteUploadDto = z.infer<typeof CompleteUploadSchema>;

export const MediaAssetSchema = z.object({
  id: z.string(),
  kind: z.enum(['IMAGE', 'VIDEO']),
  status: z.enum(['PROCESSING', 'READY', 'FAILED']),
  mimeType: z.string().nullable(),
  width: z.number().nullable(),
  height: z.number().nullable(),
  createdAt: z.string(),
});

export type MediaAsset = z.infer<typeof MediaAssetSchema>;
