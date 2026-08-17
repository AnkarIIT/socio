import React from 'react';
import { View, Text, ScrollView, Pressable, StyleSheet } from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { LinearGradient } from 'expo-linear-gradient';
import { BharatColors, Spacing, FontSize, FontWeight, Radius } from '@/constants/theme';
import { useAuthStore } from '@/stores/auth-store';
import { useProfile } from '@/hooks/use-api';

export function ProfileScreen() {
  const insets = useSafeAreaInsets();
  const { user: authUser, logout } = useAuthStore();
  const profileQuery = useProfile(authUser?.username ?? '');
  const profile = profileQuery.data;

  return (
    <LinearGradient
      colors={[BharatColors.bgGradientTop, BharatColors.bgGradientBottom]}
      style={[styles.container, { paddingTop: insets.top }]}
    >
      <StatusBar style="light" />

      {/* Header */}
      <View style={styles.headerRow}>
        <Text style={styles.headerTitle}>{authUser?.username ?? 'you'}</Text>
        <View style={styles.headerActions}>
          <Pressable style={styles.headerButton}>
            <Ionicons name="add-circle-outline" size={24} color={BharatColors.textPrimary} />
          </Pressable>
          <Pressable style={styles.headerButton} onPress={logout}>
            <Ionicons name="menu-outline" size={24} color={BharatColors.textPrimary} />
          </Pressable>
        </View>
      </View>

      <ScrollView
        contentContainerStyle={[styles.scrollContent, { paddingBottom: 120 }]}
        showsVerticalScrollIndicator={false}
      >
        {/* Profile info */}
        <View style={styles.profileSection}>
          <View style={styles.avatarLarge}>
            <Text style={styles.avatarInitial}>
              {authUser?.username?.charAt(0).toUpperCase() ?? '?'}
            </Text>
          </View>
          <Text style={styles.profileName}>{authUser?.name ?? ''}</Text>
          {authUser?.bio && <Text style={styles.profileBio}>{authUser.bio}</Text>}
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

        {/* Edit profile button */}
        <View style={styles.actionsRow}>
          <Pressable style={styles.editButton}>
            <Text style={styles.editButtonText}>Edit Profile</Text>
          </Pressable>
          <Pressable style={styles.shareButton}>
            <Ionicons name="share-outline" size={18} color={BharatColors.textOnSurface} />
          </Pressable>
        </View>

        {/* Grid/Favorites toggle */}
        <View style={styles.gridToggle}>
          <Pressable style={[styles.toggleButton, styles.toggleActive]}>
            <Ionicons name="grid-outline" size={20} color={BharatColors.textOnSurface} />
          </Pressable>
          <Pressable style={styles.toggleButton}>
            <Ionicons name="heart-outline" size={20} color={BharatColors.textSecondary} />
          </Pressable>
          <Pressable style={styles.toggleButton}>
            <Ionicons name="person-outline" size={20} color={BharatColors.textSecondary} />
          </Pressable>
        </View>

        {/* Placeholder grid */}
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
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: Spacing.xxl,
    paddingVertical: Spacing.lg,
  },
  headerTitle: {
    fontSize: FontSize.xxl,
    fontWeight: FontWeight.bold,
    color: BharatColors.textPrimary,
  },
  headerActions: {
    flexDirection: 'row',
    gap: Spacing.md,
  },
  headerButton: {
    padding: Spacing.sm,
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
  editButton: {
    flex: 1,
    backgroundColor: BharatColors.surfaceOverlay,
    borderRadius: Radius.sm,
    paddingVertical: Spacing.md,
    alignItems: 'center',
  },
  editButtonText: {
    color: BharatColors.textOnSurface,
    fontSize: FontSize.sm,
    fontWeight: FontWeight.semibold,
  },
  shareButton: {
    backgroundColor: BharatColors.surfaceOverlay,
    borderRadius: Radius.sm,
    paddingHorizontal: Spacing.lg,
    paddingVertical: Spacing.md,
    alignItems: 'center',
    justifyContent: 'center',
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