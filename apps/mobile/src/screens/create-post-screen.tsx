import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  ScrollView,
  Pressable,
  StyleSheet,
  Image,
  Alert,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import { useRouter } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import * as ImagePicker from 'expo-image-picker';
import { LinearGradient } from 'expo-linear-gradient';
import { BharatColors, Spacing, FontSize, FontWeight, Radius } from '@/constants/theme';
import { useCreatePost } from '@/hooks/use-api';

export function CreatePostScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const [body, setBody] = useState('');
  const [imageUri, setImageUri] = useState<string | null>(null);
  const createPost = useCreatePost();

  const handlePickImage = async () => {
    const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (status !== 'granted') {
      Alert.alert('Permission needed', 'Allow photo access to upload images.');
      return;
    }
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ['images'],
      quality: 0.8,
      allowsEditing: true,
      aspect: [1, 1],
    });
    if (!result.canceled && result.assets[0]) {
      setImageUri(result.assets[0].uri);
    }
  };

  const handleTakePhoto = async () => {
    const { status } = await ImagePicker.requestCameraPermissionsAsync();
    if (status !== 'granted') {
      Alert.alert('Permission needed', 'Allow camera access to take photos.');
      return;
    }
    const result = await ImagePicker.launchCameraAsync({
      quality: 0.8,
      allowsEditing: true,
      aspect: [1, 1],
    });
    if (!result.canceled && result.assets[0]) {
      setImageUri(result.assets[0].uri);
    }
  };

  const handlePost = async () => {
    if (!body.trim() && !imageUri) {
      Alert.alert('Empty post', 'Write something or add a photo.');
      return;
    }
    try {
      await createPost.mutateAsync({ body: body.trim() });
      router.back();
    } catch (err: any) {
      Alert.alert('Error', err?.message ?? 'Failed to post. Try again.');
    }
  };

  return (
    <LinearGradient
      colors={[BharatColors.bgGradientTop, BharatColors.bgGradientBottom]}
      style={styles.container}
    >
      <StatusBar style="light" />
      <KeyboardAvoidingView
        style={styles.flex}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      >
        {/* Header */}
        <View style={[styles.header, { paddingTop: insets.top + Spacing.sm }]}>
          <Pressable onPress={() => router.back()} style={styles.headerButton}>
            <Ionicons name="close" size={24} color={BharatColors.textPrimary} />
          </Pressable>
          <Text style={styles.headerTitle}>New Post</Text>
          <Pressable
            onPress={handlePost}
            style={[styles.postButton, (!body.trim() && !imageUri) && styles.postButtonDisabled]}
            disabled={createPost.isPending}
          >
            <Text style={styles.postButtonText}>
              {createPost.isPending ? '...' : 'Post'}
            </Text>
          </Pressable>
        </View>

        <ScrollView
          style={styles.scrollContent}
          contentContainerStyle={{ paddingBottom: 200 }}
          showsVerticalScrollIndicator={false}
        >
          {/* Author preview */}
          <View style={styles.authorRow}>
            <View style={styles.avatarSmall}>
              <Text style={styles.avatarInitial}>Y</Text>
            </View>
            <Text style={styles.authorName}>you</Text>
          </View>

          {/* Text input area */}
          <View style={styles.textArea}>
            <TextInput
              style={styles.textInput}
              placeholder="What's on your mind?"
              placeholderTextColor={BharatColors.textSecondary}
              value={body}
              onChangeText={setBody}
              multiline
              autoFocus
            />
          </View>

          {/* Image preview */}
          {imageUri && (
            <View style={styles.imagePreview}>
              <Image source={{ uri: imageUri }} style={styles.previewImage} />
              <Pressable
                onPress={() => setImageUri(null)}
                style={styles.removeImage}
              >
                <Ionicons name="close-circle" size={24} color="#FFFFFF" />
              </Pressable>
            </View>
          )}
        </ScrollView>

        {/* Bottom action bar */}
        <View style={[styles.bottomBar, { paddingBottom: insets.bottom + Spacing.lg }]}>
          <Pressable onPress={handleTakePhoto} style={styles.actionIcon}>
            <Ionicons name="camera-outline" size={24} color={BharatColors.accent} />
          </Pressable>
          <Pressable onPress={handlePickImage} style={styles.actionIcon}>
            <Ionicons name="image-outline" size={24} color={BharatColors.accent} />
          </Pressable>
          <Pressable style={styles.actionIcon}>
            <Ionicons name="location-outline" size={24} color={BharatColors.accent} />
          </Pressable>
          <Pressable style={styles.actionIcon}>
            <Ionicons name="pricetag-outline" size={24} color={BharatColors.accent} />
          </Pressable>
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
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: Spacing.lg,
    paddingBottom: Spacing.md,
    borderBottomWidth: 1,
    borderBottomColor: 'rgba(255,255,255,0.1)',
  },
  headerButton: {
    padding: Spacing.sm,
  },
  headerTitle: {
    color: BharatColors.textPrimary,
    fontSize: FontSize.lg,
    fontWeight: FontWeight.semibold,
  },
  postButton: {
    backgroundColor: BharatColors.accent,
    borderRadius: Radius.pill,
    paddingHorizontal: Spacing.xl,
    paddingVertical: Spacing.sm,
  },
  postButtonDisabled: {
    opacity: 0.5,
  },
  postButtonText: {
    color: '#FFFFFF',
    fontSize: FontSize.sm,
    fontWeight: FontWeight.semibold,
  },
  scrollContent: {
    flex: 1,
    paddingHorizontal: Spacing.lg,
  },
  authorRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.md,
    paddingTop: Spacing.xl,
  },
  avatarSmall: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: BharatColors.accent,
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarInitial: {
    color: '#FFFFFF',
    fontSize: FontSize.md,
    fontWeight: FontWeight.bold,
  },
  authorName: {
    color: BharatColors.textPrimary,
    fontSize: FontSize.md,
    fontWeight: FontWeight.semibold,
  },
  textArea: {
    minHeight: 120,
    paddingTop: Spacing.xl,
  },
  textInput: {
    color: BharatColors.textPrimary,
    fontSize: FontSize.lg,
    lineHeight: 26,
  },
  imagePreview: {
    marginTop: Spacing.xl,
    borderRadius: Radius.lg,
    overflow: 'hidden',
    position: 'relative',
  },
  previewImage: {
    width: '100%',
    aspectRatio: 1,
    borderRadius: Radius.lg,
  },
  removeImage: {
    position: 'absolute',
    top: Spacing.md,
    right: Spacing.md,
  },
  bottomBar: {
    flexDirection: 'row',
    gap: Spacing.xxxl,
    paddingHorizontal: Spacing.xl,
    paddingTop: Spacing.lg,
    borderTopWidth: 1,
    borderTopColor: 'rgba(255,255,255,0.1)',
  },
  actionIcon: {
    padding: Spacing.sm,
  },
});
