import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Stack } from 'expo-router';
import { BottomNav } from '@/components/bottom-nav';
import { BharatColors } from '@/constants/theme';

export default function MainLayout() {
  return (
    <View style={styles.container}>
      <Stack
        screenOptions={{
          headerShown: false,
          contentStyle: { backgroundColor: BharatColors.bgGradientTop },
        }}
      />
      <BottomNav />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: BharatColors.bgGradientTop,
  },
});
