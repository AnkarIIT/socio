import React from 'react';
import { View, Text, ScrollView, Pressable, StyleSheet } from 'react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { LinearGradient } from 'expo-linear-gradient';
import { BharatColors, Spacing, FontSize, FontWeight, Radius } from '@/constants/theme';
import { useProfile, useFollowUser } from '@/hooks/use-api';

export function UserProfileScreen() {
  const { username } = useLocalSearchParams<{ username: string }>();
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const profileQuery = useProfile(username ?? '');
  const followMutation = useFollowUser();

  const profile = profileQuery.data;

  return (
    <LinearGradient
      colors={[BharatColors.bgGradientTop, BharatColors.bgGradientBottom]}
      style={styles.container}
    >
      <StatusBar style="light" />

      {/* Header */}
      <View style={[styles.header, { paddingTop: insets.top + Spacing.sm }]}>
        <Pressable onPress={() => router.back()} style={styles.headerButton}>
          <Ionicons name="arrow-back" size={24} color={BharatColors.textPrimary} />
        </Pressable>
        <Text style={styles.headerTitle}>{username}</Text>
        <Pressable style={styles.headerButton}>
          <Ionicons name="ellipsis-vertical" size={20} color={BharatColors.textPrimary} />
        </Pressable>
      </View>

      <ScrollView
        contentContainerStyle={[styles.scrollContent, { paddingBottom: 120 }]}
        showsVerticalScrollIndicator={false}
      >
        {/* Profile info */}
        <View style={styles.profileSection}>
          <View style={styles.avatarLarge}>
            <Text style={styles.avatarInitial}>
              {username?.charAt(0).toUpperCase() ?? '?'}
            </Text>
          </View>
          <Text style={styles.profileName}>{profile?.user.name ?? username}</Text>
          {profile?.user.bio && (
            <Text style={styles.profileBio}>{profile.user.bio}</Text>
          )}
        </View>

        {/* Stats */}
        <View style={styles.statsRow}>
          <View style={styles.statItem}>
            <Text style={styles.statNumber}>{profile?.postCount ?? 0}</Text>
            <Text style={styles.statLabel}>Posts</Text>
          </View>
          <View style={styles.statItem}>
            <Text style={styles.statNumber}>{profile?.followerCount ?? 0}</Text>
            <Text style={styles.statLabel}>Followers</Text>
          </View>
          <View style={styles.statItem}>
            <Text style={styles.statNumber}>{profile?.followingCount ?? 0}</Text>
            <Text style={styles.statLabel}>Following</Text>
          </View>
        </View>

        {/* Follow / Message buttons */}
        <View style={styles.actionsRow}>
          <Pressable
            style={[styles.followButton, profile?.user.isFollowing && styles.followingButton]}
            onPress={() => profile && followMutation.mutate(profile.user.id)}
          >
            <Text style={[styles.followButtonText, profile?.user.isFollowing && styles.followingButtonText]}>
              {profile?.user.isFollowing ? 'Following' : 'Follow'}
            </Text>
          </Pressable>
          <Pressable style={styles.messageButton}>
            <Text style={styles.messageButtonText}>Message</Text>
          </Pressable>
        </View>

        {/* Grid */}
        <View style={styles.gridToggle}>
          <Pressable style={[styles.toggleButton, styles.toggleActive]}>
            <Ionicons name="grid-outline" size={20} color={BharatColors.textOnSurface} />
          </Pressable>
          <Pressable style={styles.toggleButton}>
            <Ionicons name="heart-outline" size={20} color={BharatColors.textSecondary} />
          </Pressable>
        </View>

        <View style={styles.grid}>
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <View key={i} style={styles.gridItem}>
              <View style={styles.gridPlaceholder} />
            </View>
          ))}
        </View>
      </ScrollView>
    </LinearGradient>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: Spacing.lg,
    paddingBottom: Spacing.md,
  },
  headerButton: {
    padding: Spacing.sm,
  },
  headerTitle: {
    fontSize: FontSize.lg,
    fontWeight: FontWeight.semibold,
    color: BharatColors.textPrimary,
  },
  scrollContent: {
    backgroundColor: BharatColors.surface,
    borderRadius: Radius.xxl,
    marginHorizontal: Spacing.lg,
    paddingHorizontal: Spacing.xxl,
    paddingTop: Spacing.xxl,
  },
  profileSection: {
    alignItems: 'center',
    gap: Spacing.md,
  },
  avatarLarge: {
    width: 88,
    height: 88,
    borderRadius: 44,
    backgroundColor: BharatColors.accent,
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarInitial: {
    color: '#FFFFFF',
    fontSize: 36,
    fontWeight: FontWeight.bold,
  },
  profileName: {
    fontSize: FontSize.lg,
    fontWeight: FontWeight.semibold,
    color: BharatColors.textOnSurface,
  },
  profileBio: {
    fontSize: FontSize.sm,
    color: BharatColors.textSecondary,
    textAlign: 'center',
  },
  statsRow: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    paddingVertical: Spacing.xxl,
  },
  statItem: {
    alignItems: 'center',
  },
  statNumber: {
    fontSize: FontSize.xl,
    fontWeight: FontWeight.bold,
    color: BharatColors.textOnSurface,
  },
  statLabel: {
    fontSize: FontSize.xs,
    color: BharatColors.textSecondary,
  },
  actionsRow: {
    flexDirection: 'row',
    gap: Spacing.md,
    marginBottom: Spacing.xl,
  },
  followButton: {
    flex: 1,
    backgroundColor: BharatColors.accent,
    borderRadius: Radius.sm,
    paddingVertical: Spacing.md,
    alignItems: 'center',
  },
  followingButton: {
    backgroundColor: BharatColors.surfaceOverlay,
  },
  followButtonText: {
    color: '#FFFFFF',
    fontSize: FontSize.sm,
    fontWeight: FontWeight.semibold,
  },
  followingButtonText: {
    color: BharatColors.textOnSurface,
  },
  messageButton: {
    flex: 1,
    backgroundColor: BharatColors.surfaceOverlay,
    borderRadius: Radius.sm,
    paddingVertical: Spacing.md,
    alignItems: 'center',
  },
  messageButtonText: {
    color: BharatColors.textOnSurface,
    fontSize: FontSize.sm,
    fontWeight: FontWeight.semibold,
  },
  gridToggle: {
    flexDirection: 'row',
    justifyContent: 'center',
    gap: Spacing.xxxl,
    borderBottomWidth: 1,
    borderBottomColor: BharatColors.border,
    paddingBottom: Spacing.md,
    marginBottom: Spacing.md,
  },
  toggleButton: {
    paddingVertical: Spacing.sm,
  },
  toggleActive: {
    borderBottomWidth: 2,
    borderBottomColor: BharatColors.textOnSurface,
  },
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 2,
  },
  gridItem: {
    width: '33.33%',
    aspectRatio: 1,
  },
  gridPlaceholder: {
    flex: 1,
    backgroundColor: '#F1F5F9',
    borderRadius: 2,
  },
});