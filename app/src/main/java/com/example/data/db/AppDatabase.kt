package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CommentEntity
import com.example.data.model.DirectMessageEntity
import com.example.data.model.FollowEntity
import com.example.data.model.PostEntity
import com.example.data.model.StoryEntity
import com.example.data.model.UserEntity

import com.example.data.model.ReelEntity

@Database(
    entities = [
        PostEntity::class,
        CommentEntity::class,
        StoryEntity::class,
        DirectMessageEntity::class,
        UserEntity::class,
        FollowEntity::class,
        ReelEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao
    abstract fun storyDao(): StoryDao
    abstract fun messageDao(): MessageDao
    abstract fun userDao(): UserDao
    abstract fun followDao(): FollowDao
    abstract fun reelDao(): ReelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "teagram_db"
                ).addMigrations(MIGRATION_3_4)
                 .fallbackToDestructiveMigration(false).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
