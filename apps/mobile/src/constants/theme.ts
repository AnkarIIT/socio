import '@/global.css';

import { Platform } from 'react-native';

export const Colors = {
  light: {
    text: '#0F172A',
    textSecondary: '#64748B',
    background: '#FFFFFF',
    surface: '#F8FAFC',
    backgroundElement: '#F1F5F9',
    backgroundSelected: '#E2E8F0',
    accent: '#4F46E5',
    accentLight: '#818CF8',
    border: '#E2E8F0',
    danger: '#EF4444',
    success: '#10B981',
  },
  dark: {
    text: '#F8FAFC',
    textSecondary: '#94A3B8',
    background: '#0F172A',
    surface: '#1E293B',
    backgroundElement: '#1E293B',
    backgroundSelected: '#334155',
    accent: '#4F46E5',
    accentLight: '#818CF8',
    border: '#334155',
    danger: '#EF4444',
    success: '#10B981',
  },
} as const;

export type ThemeColor = keyof typeof Colors.light & keyof typeof Colors.dark;

/** Bharat-specific design tokens for the dark gradient + white surface theme */
export const BharatColors = {
  bgGradientTop: '#0F172A',
  bgGradientBottom: '#312E81',
  bgGlow: '#4C1D95',
  surface: '#FFFFFF',
  surfaceOverlay: '#F8FAFC',
  accent: '#4F46E5',
  accentLight: '#818CF8',
  accentDark: '#4338CA',
  textPrimary: '#F8FAFC',
  textOnSurface: '#0F172A',
  textSecondary: '#64748B',
  storyBorder: '#4F46E5',
  border: '#334155',
  backgroundElement: '#1E293B',
  danger: '#EF4444',
  success: '#10B981',
  navBg: '#0F172A',
  navBgAlpha: 'rgba(15, 23, 42, 0.95)',
} as const;

export const Fonts = Platform.select({
  ios: {
    sans: 'system-ui',
    serif: 'ui-serif',
    rounded: 'ui-rounded',
    mono: 'ui-monospace',
  },
  default: {
    sans: 'normal',
    serif: 'serif',
    rounded: 'normal',
    mono: 'monospace',
  },
  web: {
    sans: 'var(--font-display)',
    serif: 'var(--font-serif)',
    rounded: 'var(--font-rounded)',
    mono: 'var(--font-mono)',
  },
});

export const Spacing = {
  xs: 2,
  sm: 4,
  md: 8,
  lg: 12,
  xl: 16,
  xxl: 24,
  xxxl: 32,
  huge: 48,
} as const;

export const Radius = {
  sm: 8,
  md: 12,
  lg: 16,
  xl: 24,
  xxl: 32,
  pill: 9999,
} as const;

export const FontSize = {
  xs: 11,
  sm: 13,
  md: 15,
  lg: 17,
  xl: 20,
  xxl: 24,
  hero: 32,
} as const;

export const FontWeight = {
  regular: '400' as const,
  medium: '500' as const,
  semibold: '600' as const,
  bold: '700' as const,
};

export const BottomTabInset = Platform.select({ ios: 34, android: 24 }) ?? 0;
export const NavHeight = 64;
export const FABSize = 56;
export const StorySize = 64;
export const MaxContentWidth = 480;
