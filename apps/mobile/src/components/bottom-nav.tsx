import React from 'react';
import { View, Pressable, StyleSheet } from 'react-native';
import { useRouter, usePathname } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { BharatColors, Spacing, Radius, NavHeight, FABSize, BottomTabInset } from '@/constants/theme';

type TabRoute = '/' | '/explore' | '/messages' | '/profile';

const TABS: { route: TabRoute; icon: keyof typeof Ionicons.glyphMap; activeIcon: keyof typeof Ionicons.glyphMap }[] = [
  { route: '/', icon: 'home-outline', activeIcon: 'home' },
  { route: '/explore', icon: 'compass-outline', activeIcon: 'compass' },
  { route: '/messages', icon: 'chatbubble-outline', activeIcon: 'chatbubble' },
  { route: '/profile', icon: 'person-outline', activeIcon: 'person' },
];

export function BottomNav() {
  const router = useRouter();
  const pathname = usePathname();

  return (
    <View style={styles.container}>
      <View style={styles.navBar}>
        {TABS.map((tab, index) => {
          const isCenter = index === 2;
          const isActive =
            pathname === tab.route ||
            (tab.route === '/profile' && pathname.startsWith('/profile'));

          if (isCenter) {
            return (
              <React.Fragment key={tab.route}>
                <Pressable
                  onPress={() => router.push(tab.route)}
                  style={styles.tabItem}
                  accessibilityLabel={tab.route === '/messages' ? 'Messages' : 'Create post'}
                >
                  <Ionicons
                    name={isActive ? tab.activeIcon : tab.icon}
                    size={24}
                    color={isActive ? '#FFFFFF' : '#94A3B8'}
                  />
                </Pressable>

                {/* FAB */}
                <Pressable
                  style={styles.fab}
                  onPress={() => router.push('/create')}
                  accessibilityLabel="Create post"
                >
                  <Ionicons name="add" size={28} color="#FFFFFF" />
                </Pressable>
              </React.Fragment>
            );
          }

          return (
            <Pressable
              key={tab.route}
              onPress={() => router.push(tab.route)}
              style={styles.tabItem}
              accessibilityLabel={tab.route === '/' ? 'Home' : tab.route === '/explore' ? 'Explore' : 'Profile'}
            >
              <Ionicons
                name={isActive ? tab.activeIcon : tab.icon}
                size={24}
                color={isActive ? '#FFFFFF' : '#94A3B8'}
              />
            </Pressable>
          );
        })}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    alignItems: 'center',
    paddingBottom: BottomTabInset > 0 ? Spacing.sm : Spacing.lg,
    zIndex: 50,
  },
  navBar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-around',
    backgroundColor: BharatColors.navBgAlpha,
    borderRadius: Radius.pill,
    paddingHorizontal: Spacing.xl,
    height: NavHeight,
    width: '88%',
    // subtle shadow
    shadowColor: '#000',
    shadowOffset: { width: 0, height: -4 },
    shadowOpacity: 0.3,
    shadowRadius: 12,
    elevation: 12,
  },
  tabItem: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    height: NavHeight,
  },
  fab: {
    width: FABSize,
    height: FABSize,
    borderRadius: FABSize / 2,
    backgroundColor: BharatColors.accent,
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: -(FABSize / 2 + 4),
    // shadow for pop effect
    shadowColor: BharatColors.accent,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.4,
    shadowRadius: 12,
    elevation: 8,
    borderWidth: 3,
    borderColor: BharatColors.bgGradientTop,
  },
});
