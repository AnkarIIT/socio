import React from 'react';
import { View, Text, Pressable, StyleSheet } from 'react-native';
import { BharatColors, Spacing, FontSize, FontWeight } from '@/constants/theme';

interface StoryRingProps {
  username: string;
  avatarUrl?: string | null;
  isOwn?: boolean;
  hasUnviewed?: boolean;
  size?: number;
  onPress?: () => void;
}

export function StoryRing({
  username,
  isOwn = false,
  hasUnviewed = true,
  size = 64,
  onPress,
}: StoryRingProps) {
  const borderWidth = 2.5;
  const ringColor = hasUnviewed ? BharatColors.storyBorder : BharatColors.border;

  return (
    <Pressable onPress={onPress} style={styles.container} accessibilityLabel={`${username}'s story`}>
      <View
        style={[
          styles.ring,
          {
            width: size + borderWidth * 2,
            height: size + borderWidth * 2,
            borderRadius: (size + borderWidth * 2) / 2,
            borderWidth,
            borderColor: isOwn ? BharatColors.border : ringColor,
          },
        ]}
      >
        <View
          style={[
            styles.avatar,
            {
              width: size,
              height: size,
              borderRadius: size / 2,
              backgroundColor: BharatColors.accentLight,
            },
          ]}
        >
          <Text style={[styles.initial, { fontSize: size * 0.35 }]}>
            {username.charAt(0).toUpperCase()}
          </Text>
        </View>
      </View>
      {isOwn && (
        <View style={styles.plusBadge}>
          <Text style={styles.plusText}>+</Text>
        </View>
      )}
      <Text style={styles.username} numberOfLines={1}>
        {username.length > 10 ? `${username.slice(0, 8)}…` : username}
      </Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    gap: Spacing.xs,
    width: 80,
  },
  ring: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatar: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  initial: {
    color: '#FFFFFF',
    fontWeight: FontWeight.bold,
  },
  plusBadge: {
    position: 'absolute',
    right: 6,
    bottom: 18,
    backgroundColor: BharatColors.accent,
    width: 20,
    height: 20,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 2,
    borderColor: BharatColors.bgGradientTop,
  },
  plusText: {
    color: '#FFFFFF',
    fontSize: 13,
    fontWeight: FontWeight.bold,
    marginTop: -1,
  },
  username: {
    color: BharatColors.textPrimary,
    fontSize: FontSize.xs,
    fontWeight: FontWeight.medium,
    textAlign: 'center',
  },
});
