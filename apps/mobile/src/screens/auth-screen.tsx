import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  Pressable,
  StyleSheet,
  KeyboardAvoidingView,
  Platform,
  Alert,
} from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { LinearGradient } from 'expo-linear-gradient';
import { useAuthStore } from '@/stores/auth-store';
import { BharatColors, Radius, Spacing, FontSize, FontWeight } from '@/constants/theme';

export function AuthScreen() {
  const [step, setStep] = useState<'phone' | 'otp'>('phone');
  const [phone, setPhone] = useState('');
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);

  const { requestOtp, verifyOtp } = useAuthStore();

  const handleRequestOtp = async () => {
    if (phone.length < 10) {
      Alert.alert('Invalid number', 'Please enter a valid 10-digit mobile number.');
      return;
    }
    setLoading(true);
    try {
      await requestOtp(phone);
      setStep('otp');
    } catch (err: any) {
      Alert.alert('Error', err?.message ?? 'Failed to send OTP. Try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async () => {
    if (code.length < 4) {
      Alert.alert('Invalid code', 'Please enter the OTP sent to your phone.');
      return;
    }
    setLoading(true);
    try {
      await verifyOtp(phone, code);
    } catch (err: any) {
      Alert.alert('Error', err?.message ?? 'Invalid OTP. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <LinearGradient
      colors={[BharatColors.bgGradientTop, BharatColors.bgGradientBottom]}
      style={styles.container}
    >
      <KeyboardAvoidingView
        style={styles.flex}
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      >
        <StatusBar style="light" />
        <View style={styles.content}>
          <View style={styles.header}>
            <Text style={styles.logo}>Bharat</Text>
            <Text style={styles.tagline}>Instagram for Bharat</Text>
          </View>

          {step === 'phone' ? (
            <View style={styles.form}>
              <Text style={styles.label}>Enter your mobile number</Text>
              <View style={styles.phoneRow}>
                <View style={styles.countryCode}>
                  <Text style={styles.countryCodeText}>+91</Text>
                </View>
                <TextInput
                  style={styles.phoneInput}
                  placeholder="98765 43210"
                  placeholderTextColor="#64748B"
                  keyboardType="phone-pad"
                  maxLength={10}
                  value={phone}
                  onChangeText={setPhone}
                  autoFocus
                />
              </View>
              <Pressable
                style={[styles.button, loading && styles.buttonDisabled]}
                onPress={handleRequestOtp}
                disabled={loading}
              >
                <Text style={styles.buttonText}>
                  {loading ? 'Sending...' : 'Send OTP'}
                </Text>
              </Pressable>
            </View>
          ) : (
            <View style={styles.form}>
              <Text style={styles.label}>Enter the code sent to +91 {phone}</Text>
              <TextInput
                style={styles.otpInput}
                placeholder="0000"
                placeholderTextColor="#64748B"
                keyboardType="number-pad"
                maxLength={6}
                value={code}
                onChangeText={setCode}
                autoFocus
              />
              <Pressable
                style={[styles.button, loading && styles.buttonDisabled]}
                onPress={handleVerifyOtp}
                disabled={loading}
              >
                <Text style={styles.buttonText}>
                  {loading ? 'Verifying...' : 'Verify'}
                </Text>
              </Pressable>
              <Pressable onPress={() => setStep('phone')} style={styles.linkButton}>
                <Text style={styles.linkText}>Change number</Text>
              </Pressable>
            </View>
          )}

          <Text style={styles.footer}>
            By continuing, you agree to our Terms & Privacy Policy
          </Text>
        </View>
      </KeyboardAvoidingView>
    </LinearGradient>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  flex: {
    flex: 1,
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: Spacing.xxl,
    gap: Spacing.huge,
  },
  header: {
    alignItems: 'center',
    gap: Spacing.sm,
  },
  logo: {
    fontSize: 42,
    fontWeight: FontWeight.bold,
    color: BharatColors.textPrimary,
    letterSpacing: -1,
  },
  tagline: {
    fontSize: FontSize.md,
    color: BharatColors.textSecondary,
  },
  form: {
    width: '100%',
    gap: Spacing.lg,
  },
  label: {
    color: BharatColors.textSecondary,
    fontSize: FontSize.sm,
    textAlign: 'center',
  },
  phoneRow: {
    flexDirection: 'row',
    gap: Spacing.md,
  },
  countryCode: {
    backgroundColor: BharatColors.surface,
    borderRadius: Radius.lg,
    paddingHorizontal: Spacing.lg,
    alignItems: 'center',
    justifyContent: 'center',
  },
  countryCodeText: {
    color: BharatColors.textOnSurface,
    fontSize: FontSize.lg,
    fontWeight: FontWeight.semibold,
  },
  phoneInput: {
    flex: 1,
    backgroundColor: BharatColors.surface,
    borderRadius: Radius.lg,
    paddingHorizontal: Spacing.lg,
    color: BharatColors.textOnSurface,
    fontSize: FontSize.lg,
    fontWeight: FontWeight.medium,
    letterSpacing: 2,
  },
  otpInput: {
    backgroundColor: BharatColors.surface,
    borderRadius: Radius.lg,
    paddingHorizontal: Spacing.lg,
    paddingVertical: Spacing.xl,
    color: BharatColors.textOnSurface,
    fontSize: FontSize.xxl,
    fontWeight: FontWeight.bold,
    textAlign: 'center',
    letterSpacing: 8,
  },
  button: {
    backgroundColor: BharatColors.accent,
    borderRadius: Radius.pill,
    paddingVertical: Spacing.lg,
    alignItems: 'center',
  },
  buttonDisabled: {
    opacity: 0.6,
  },
  buttonText: {
    color: '#FFFFFF',
    fontSize: FontSize.lg,
    fontWeight: FontWeight.semibold,
  },
  linkButton: {
    alignItems: 'center',
    paddingVertical: Spacing.sm,
  },
  linkText: {
    color: BharatColors.accentLight,
    fontSize: FontSize.sm,
  },
  footer: {
    color: BharatColors.textSecondary,
    fontSize: FontSize.xs,
    textAlign: 'center',
  },
});
