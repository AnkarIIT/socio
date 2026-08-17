import React from 'react';
import { View, Text, Pressable, StyleSheet } from 'react-native';
import { useRouter } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Image } from 'expo-image';
import { BharatColors, Radius, Spacing, FontSize, FontWeight } from '@/constants/theme';
import { useLikePost } from '@/hooks/use-api';
import type { Post } from '@/types/api';

interface PostCardProps {
  post: Post;
}

function formatTimeAgo(dateString: string): string {
  const diff = Date.now() - new Date(dateString).getTime();
  const minutes = Math.floor(diff / 60_000);
  if (minutes < 1) return 'just now';
  if (minutes < 60) return `${minutes}m`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h`;
  const days = Math.floor(hours / 24);
  if (days < 7) return `${days}d`;
  return `${Math.floor(days / 7)}w`;
}

export function PostCard({ post }: PostCardProps) {
  const router = useRouter();
  const likeMutation = useLikePost();
  const isLiked = post.isLiked;

  const handleLike = () => {
    likeMutation.mutate(post.id);
  };

  const hasImage = !!post.mediaUrl;

  return (
    <View style={styles.card}>
      {/* Author row */}
      <View style={styles.authorRow}>
        <View style={styles.avatarSmall}>
          <Text style={styles.avatarInitial}>
            {post.author.username.charAt(0).toUpperCase()}
          </Text>
        </View>
        <View style={styles.authorInfo}>
          <Pressable
            onPress={() => router.push(`/profile/${post.author.username}`)}
          >
            <Text style={styles.authorName}>
              {post.author.username}
              {post.author.isVerified && (
                <Text style={styles.verified}> ✓</Text>
              )}
            </Text>
          </Pressable>
          <Text style={styles.timestamp}>{formatTimeAgo(post.createdAt)}</Text>
        </View>
        <Pressable style={styles.moreButton} accessibilityLabel="More options">
          <Ionicons name="ellipsis-horizontal" size={16} color={BharatColors.textSecondary} />
        </Pressable>
      </View>

      {/* Post image */}
      {hasImage ? (
        <Image
          source={{ uri: post.mediaUrl! }}
          style={styles.postImage}
          contentFit="cover"
          transition={300}
          placeholder={{ blurhash: 'LGF5]+Yk^6#M@-5c,1J5@[or[Q6.' }}
        />
      ) : (
        <View style={styles.textOnlyPost}>
          <Text style={styles.textOnlyBody}>{post.body}</Text>
        </View>
      )}

      {/* Post body (shown below image if image exists) */}
      {hasImage && (
        <Text style={styles.body}>{post.body}</Text>
      )}

      {/* Action row */}
      <View style={styles.actionRow}>
        <Pressable onPress={handleLike} style={styles.actionButton} accessibilityLabel="Like">
          <Ionicons
            name={isLiked ? 'heart' : 'heart-outline'}
            size={22}
            color={isLiked ? BharatColors.danger : BharatColors.textOnSurface}
          />
          <Text style={[styles.actionCount, isLiked && styles.actionCountActive]}>
            {post.likeCount}
          </Text>
        </Pressable>

        <Pressable style={styles.actionButton} accessibilityLabel="Comment">
          <Ionicons name="chatbubble-outline" size={20} color={BharatColors.textOnSurface} />
          <Text style={styles.actionCount}>{post.commentCount}</Text>
        </Pressable>

        <Pressable style={styles.actionButton} accessibilityLabel="Share">
          <Ionicons name="paper-plane-outline" size={20} color={BharatColors.textOnSurface} />
        </Pressable>

        <View style={styles.actionSpacer} />

        <Pressable style={styles.actionButton} accessibilityLabel="Save">
          <Ionicons name="bookmark-outline" size={20} color={BharatColors.textOnSurface} />
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: BharatColors.surface,
    borderRadius: Radius.xl,
    marginBottom: Spacing.lg,
    overflow: 'hidden',
  },
  authorRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: Spacing.lg,
    paddingTop: Spacing.lg,
    paddingBottom: Spacing.md,
  },
  avatarSmall: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: BharatColors.accent,
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarInitial: {
    color: '#FFFFFF',
    fontSize: FontSize.md,
    fontWeight: FontWeight.bold,
  },
  authorInfo: {
    flex: 1,
    marginLeft: Spacing.md,
    gap: 2,
  },
  authorName: {
    color: BharatColors.textOnSurface,
    fontSize: FontSize.sm,
    fontWeight: FontWeight.semibold,
  },
  verified: {
    color: BharatColors.accent,
  },
  timestamp: {
    color: BharatColors.textSecondary,
    fontSize: FontSize.xs,
  },
  moreButton: {
    padding: Spacing.sm,
  },
  postImage: {
    width: '100%',
    aspectRatio: 1,
  },
  textOnlyPost: {
    paddingHorizontal: Spacing.lg,
    paddingVertical: Spacing.xl,
  },
  textOnlyBody: {
    color: BharatColors.textOnSurface,
    fontSize: FontSize.lg,
    lineHeight: 24,
  },
  body: {
    color: BharatColors.textOnSurface,
    fontSize: FontSize.sm,
    lineHeight: 20,
    paddingHorizontal: Spacing.lg,
    paddingTop: Spacing.md,
    paddingBottom: Spacing.sm,
  },
  actionRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: Spacing.lg,
    paddingBottom: Spacing.lg,
    paddingTop: Spacing.xs,
    gap: Spacing.xl,
  },
  actionButton: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.xs,
  },
  actionCount: {
    color: BharatColors.textOnSurface,
    fontSize: FontSize.sm,
    fontWeight: FontWeight.medium,
  },
  actionCountActive: {
    color: BharatColors.danger,
  },
  actionSpacer: {
    flex: 1,
  },
});
