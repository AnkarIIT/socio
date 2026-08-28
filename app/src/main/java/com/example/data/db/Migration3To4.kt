package com.example.data.db

import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 3 to 4.
 *
 * Adds performance indices to frequently queried columns.
 * This is a no-op for fresh installs; Room creates indices directly from @Entity definitions.
 * For existing installs, the indices are created via CREATE INDEX statements.
 */
val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS index_posts_authorUsername ON posts(authorUsername)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_posts_timestamp ON posts(timestamp)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_posts_isSaved ON posts(isSaved)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_comments_postId ON comments(postId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_follows_followerUsername ON follows(followerUsername)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_follows_followingUsername ON follows(followingUsername)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_stories_username ON stories(username)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_stories_timestamp ON stories(timestamp)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_direct_messages_conversationId ON direct_messages(conversationId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_direct_messages_timestamp ON direct_messages(timestamp)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_reels_authorUsername ON reels(authorUsername)")
    }
}
