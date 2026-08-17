import React from 'react';
import {
  View,
  Text,
  ScrollView,
  FlatList,
  StyleSheet,
  RefreshControl,
} from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { StoryRing } from '@/components/story-ring';
import { PostCard } from '@/components/post-card';
import { BharatColors, Spacing, FontSize, FontWeight, Radius } from '@/constants/theme';
import { useFeedWithMock } from '@/hooks/use-api';
import type { Post } from '@/types/api';

const MOCK_STORIES = [
  { id: 'own', username: 'you', isOwn: true, hasUnviewed: false },
  { id: '1', username: 'priya.designs', isOwn: false, hasUnviewed: true },
  { id: '2', username: 'arjun.travels', isOwn: false, hasUnviewed: true },
  { id: '3', username: 'meera.cooks', isOwn: false, hasUnviewed: false },
  { id: '4', username: 'vikram.fit', isOwn: false, hasUnviewed: true },
  { id: '5', username: 'ananya.art', isOwn: false, hasUnviewed: false },
];

export function FeedScreen() {
  const insets = useSafeAreaInsets();
  const feedQuery = useFeedWithMock();

  const posts: Post[] =
    feedQuery.data?.pages.flatMap((page) => page.items) ?? [];

  const [refreshing, setRefreshing] = React.useState(false);

  const handleRefresh = async () => {
    setRefreshing(true);
    await feedQuery.refetch();
    setRefreshing(false);
  };

  return (
    <View style={[styles.container, { paddingTop: insets.top }]}>
      <StatusBar style="light" />

      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.logo}>Bharat</Text>
      </View>

      <FlatList
        data={posts}
        keyExtractor={(item) => item.id}
        renderItem={({ item }) => <PostCard post={item} />}
        ListHeaderComponent={
          <View style={styles.storiesSection}>
            <ScrollView
              horizontal
              showsHorizontalScrollIndicator={false}
              contentContainerStyle={styles.storiesScroll}
            >
              {MOCK_STORIES.map((story) => (
                <StoryRing
                  key={story.id}
                  username={story.username}
                  isOwn={story.isOwn}
                  hasUnviewed={story.hasUnviewed}
                />
              ))}
            </ScrollView>
          </View>
        }
        ListEmptyComponent={
          !feedQuery.isLoading ? (
            <View style={styles.emptyContainer}>
              <Text style={styles.emptyText}>No posts yet</Text>
              <Text style={styles.emptySubtext}>Follow people to see their posts here</Text>
            </View>
          ) : null
        }
        contentContainerStyle={[
          styles.feedContent,
          { paddingBottom: 120 },
        ]}
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={handleRefresh}
            tintColor={BharatColors.accent}
          />
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: BharatColors.bgGradientTop,
  },
  header: {
    paddingHorizontal: Spacing.xxl,
    paddingVertical: Spacing.lg,
  },
  logo: {
    fontSize: FontSize.xxl,
    fontWeight: FontWeight.bold,
    color: BharatColors.textPrimary,
    letterSpacing: -0.5,
  },
  storiesSection: {
    paddingBottom: Spacing.lg,
  },
  storiesScroll: {
    paddingHorizontal: Spacing.lg,
    gap: Spacing.md,
  },
  feedContent: {
    backgroundColor: BharatColors.surface,
    borderRadius: Radius.xxl,
    paddingHorizontal: Spacing.lg,
    paddingTop: Spacing.xl,
    minHeight: 600,
  },
  emptyContainer: {
    alignItems: 'center',
    paddingVertical: Spacing.huge,
    gap: Spacing.sm,
  },
  emptyText: {
    color: BharatColors.textOnSurface,
    fontSize: FontSize.lg,
    fontWeight: FontWeight.semibold,
  },
  emptySubtext: {
    color: BharatColors.textSecondary,
    fontSize: FontSize.sm,
  },
});
