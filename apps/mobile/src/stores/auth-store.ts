import { create } from 'zustand';
import { api } from '@/lib/api';

export interface User {
  id: string;
  username: string;
  name: string;
  avatarUrl: string | null;
  coverUrl: string | null;
  bio: string | null;
  isVerified: boolean;
  isPrivate: boolean;
  locale: string;
  createdAt: string;
}

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;

  requestOtp: (phone: string) => Promise<{ sent: boolean; retryAfterSeconds: number }>;
  verifyOtp: (phone: string, code: string) => Promise<void>;
  logout: () => Promise<void>;
  loadUser: () => Promise<void>;
  setUser: (user: User) => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isAuthenticated: false,
  isLoading: true,

  requestOtp: async (phone) => {
    const data = await api.post<{ sent: boolean; retryAfterSeconds: number }>(
      '/auth/otp/request',
      { phone },
    );
    return data;
  },

  verifyOtp: async (phone, code) => {
    const data = await api.post<{
      accessToken: string;
      refreshToken: string;
      user: User;
    }>('/auth/otp/verify', { phone, code });

    await api.setTokens(data.accessToken, data.refreshToken);
    set({ user: data.user, isAuthenticated: true });
  },

  logout: async () => {
    try {
      await api.post('/auth/logout');
    } finally {
      await api.clearTokens();
      set({ user: null, isAuthenticated: false });
    }
  },

  loadUser: async () => {
    set({ isLoading: true });
    try {
      const user = await api.get<User>('/users/me');
      set({ user, isAuthenticated: true, isLoading: false });
    } catch {
      set({ user: null, isAuthenticated: false, isLoading: false });
    }
  },

  setUser: (user) => set({ user }),
}));
