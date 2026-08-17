import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  ScrollView,
  Pressable,
  StyleSheet,
} from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { LinearGradient } from 'expo-linear-gradient';
import { BharatColors, Spacing, FontSize, FontWeight, Radius } from '@/constants/theme';
import { useSearchUsers } from '@/hooks/use-api';
import type { User } from '@/types/api';

const TRENDING_TAGS = [
  { tag: 'DiwaliVibes', posts: '12.4k' },
  { tag: 'MadeInBharat', posts: '8.2k' },
  { tag: 'StreetFood', posts: '6.8k' },
  { tag: 'CricketFever', posts: '15.1k' },
  { tag: 'Bollywood', posts: '9.3k' },
];

export function ExploreScreen() {
  const insets = useSafeAreaInsets();
  const [query, setQuery] = useState('');
  const searchQuery = useSearchUsers(query);

  const users: User[] = searchQuery.data?.users ?? [];

  return (
    <LinearGradient
      colors={[BharatColors.bgGradientTop, BharatColors.bgGradientBottom]}
      style={[styles.container, { paddingTop: insets.top }]}
    >
      <StatusBar style="light" />

      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.title}>Explore</Text>
      </View>

      {/* Search bar */}
      <View style={styles.searchContainer}>
        <Ionicons name="search" size={18} color={BharatColors.textSecondary} />
        <TextInput
          style={styles.searchInput}
          placeholder="Search people, tags..."
          placeholderTextColor="#64748B"
          value={query}
          onChangeText={setQuery}
          returnKeyType="search"
        />
        {query.length > 0 && (
          <Pressable onPress={() => setQuery('')}>
            <Ionicons name="close-circle" size={18} color={BharatColors.textSecondary} />
          </Pressable>
        )}
      </View>

      <ScrollView
        style={styles.scrollView}
        contentContainerStyle={[styles.scrollContent, { paddingBottom: 120 }]}
        showsVerticalScrollIndicator={false}
      >
        {query.length >= 2 ? (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>People</Text>
            {users.length === 0 && !searchQuery.isLoading && (
              <Text style={styles.emptyText}>No users found</Text>
            )}
            {users.map((user) => (
              <View key={user.id} style={styles.userRow}>
                <View style={styles.userAvatar}>
                  <Text style={styles.userInitial}>
                    {user.username.charAt(0).toUpperCase()}
                  </Text>
                </View>
                <View style={styles.userInfo}>
                  <Text style={styles.userName}>
                    {user.username}
                    {user.isVerified && <Text style={styles.verified}> ✓</Text>}
                  </Text>
                  <Text style={styles.userBio} numberOfLines={1}>
                    {user.name}
                  </Text>
                </View>
                <Pressable style={styles.followButton}>
                  <Text style={styles.followButtonText}>Follow</Text>
                </Pressable>
              </View>
            ))}
          </View>
        ) : (
          <>
            <View style={styles.section}>
              <Text style={styles.sectionTitle}>Trending Now</Text>
              {TRENDING_TAGS.map((item) => (
                <Pressable key={item.tag} style={styles.trendingRow}>
                  <View style={styles.trendingIcon}>
                    <Ionicons name="trending-up" size={18} color={BharatColors.accent} />
                  </View>
                  <View style={styles.trendingInfo}>
                    <Text style={styles.trendingTag}>#{item.tag}</Text>
                    <Text style={styles.trendingCount}>{item.posts} posts</Text>
                  </View>
                  <Ionicons name="chevron-forward" size={16} color={BharatColors.textSecondary} />
                </Pressable>
              ))}
            </View>

            <View style={styles.section}>
              <Text style={styles.sectionTitle}>Suggested for You</Text>
              {[
                { username: 'techwithraj', name: 'Raj Kumar', bio: 'Building in public' },
                { username: 'foodie.nisha', name: 'Nisha Gupta', bio: 'Food is love' },
                { username: 'fitness.suresh', name: 'Suresh Patel', bio: 'Train. Eat. Repeat.' },
              ].map((user) => (
                <View key={user.username} style={styles.userRow}>
                  <View style={styles.userAvatar}>
                    <Text style={styles.userInitial}>
                      {user.username.charAt(0).toUpperCase()}
                    </Text>
                  </View>
                  <View style={styles.userInfo}>
                    <Text style={styles.userName}>{user.username}</Text>
                    <Text style={styles.userBio} numberOfLines={1}>{user.bio}</Text>
                  </View>
                  <Pressable style={styles.followButton}>
                    <Text style={styles.followButtonText}>Follow</Text>
                  </Pressable>
                </View>
              ))}
            </View>
          </>
        )}
      </ScrollView>
    </LinearGradient>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  header: {
    paddingHorizontal: Spacing.xxl,
    paddingVertical: Spacing.lg,
  },
  title: {
    fontSize: FontSize.xxl,
    fontWeight: FontWeight.bold,
    color: BharatColors.textPrimary,
  },
  searchContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: BharatColors.surface,
    marginHorizontal: Spacing.xxl,
    borderRadius: Radius.pill,
    paddingHorizontal: Spacing.lg,
    paddingVertical: Spacing.md,
    gap: Spacing.md,
  },
  searchInput: {
    flex: 1,
    color: BharatColors.textOnSurface,
    fontSize: FontSize.md,
  },
  scrollView: {
    flex: 1,
  },
  scrollContent: {
    marginTop: Spacing.xl,
    backgroundColor: BharatColors.surface,
    borderRadius: Radius.xxl,
    marginHorizontal: Spacing.lg,
    paddingVertical: Spacing.xl,
    paddingHorizontal: Spacing.lg,
  },
  section: {
    marginBottom: Spacing.xxl,
  },
  sectionTitle: {
    color: BharatColors.textOnSurface,
    fontSize: FontSize.lg,
    fontWeight: FontWeight.bold,
    marginBottom: Spacing.lg,
  },
  trendingRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: Spacing.md,
    gap: Spacing.lg,
  },
  trendingIcon: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: '#EEF2FF',
    alignItems: 'center',
    justifyContent: 'center',
  },
  trendingInfo: {
    flex: 1,
  },
  trendingTag: {
    color: BharatColors.textOnSurface,
    fontSize: FontSize.md,
    fontWeight: FontWeight.semibold,
  },
  trendingCount: {
    color: BharatColors.textSecondary,
    fontSize: FontSize.xs,
  },
  userRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: Spacing.md,
    gap: Spacing.md,
  },
  userAvatar: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: BharatColors.accent,
    alignItems: 'center',
    justifyContent: 'center',
  },
  userInitial: {
    color: '#FFFFFF',
    fontSize: FontSize.lg,
    fontWeight: FontWeight.bold,
  },
  userInfo: {
    flex: 1,
  },
  userName: {
    color: BharatColors.textOnSurface,
    fontSize: FontSize.md,
    fontWeight: FontWeight.semibold,
  },
  verified: {
    color: BharatColors.accent,
  },
  userBio: {
    color: BharatColors.textSecondary,
    fontSize: FontSize.sm,
  },
  followButton: {
    backgroundColor: BharatColors.accent,
    borderRadius: Radius.pill,
    paddingHorizontal: Spacing.xl,
    paddingVertical: Spacing.sm,
  },
  followButtonText: {
    color: '#FFFFFF',
    fontSize: FontSize.sm,
    fontWeight: FontWeight.semibold,
  },
  emptyText: {
    color: BharatColors.textSecondary,
    fontSize: FontSize.sm,
    textAlign: 'center',
    paddingVertical: Spacing.xxl,
  },
});
