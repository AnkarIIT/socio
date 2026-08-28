package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE id = :postId LIMIT 1")
    suspend fun getPostById(postId: Long): PostEntity?

    @Query("SELECT * FROM posts WHERE isSaved = 1 ORDER BY timestamp DESC")
    fun getSavedPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE authorUsername = :username ORDER BY timestamp DESC")
    fun getPostsByAuthor(username: String): Flow<List<PostEntity>>

    @Query("SELECT COUNT(*) FROM posts WHERE authorUsername = :username")
    fun getPostsCountByAuthor(username: String): Flow<Int>

    @Query("""
        SELECT DISTINCT p.* FROM posts p
        WHERE p.authorUsername = :currentUsername 
           OR p.authorUsername IN (SELECT followingUsername FROM follows WHERE followerUsername = :currentUsername)
        ORDER BY p.timestamp DESC
    """)
    fun getFeedPostsForUser(currentUsername: String): Flow<List<PostEntity>>

    @Query("""
        SELECT DISTINCT p.* FROM posts p
        WHERE p.authorUsername = :currentUsername 
           OR p.authorUsername IN (SELECT followingUsername FROM follows WHERE followerUsername = :currentUsername)
        ORDER BY p.timestamp DESC
        LIMIT :limit
    """)
    fun getFeedPostsForUserPaged(currentUsername: String, limit: Int): Flow<List<PostEntity>>

    @Query("""
        SELECT COUNT(DISTINCT p.id) FROM posts p
        WHERE p.authorUsername = :currentUsername 
           OR p.authorUsername IN (SELECT followingUsername FROM follows WHERE followerUsername = :currentUsername)
    """)
    fun getFeedPostsCount(currentUsername: String): Flow<Int>

    @Query("UPDATE posts SET authorUsername = :newUsername, authorAvatarUrl = :newAvatarUrl WHERE authorUsername = :oldUsername")
    suspend fun updateAuthorInfo(oldUsername: String, newUsername: String, newAvatarUrl: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Update
    suspend fun updatePost(post: PostEntity)

    @Query("UPDATE posts SET isLiked = :isLiked, likesCount = :likesCount WHERE id = :postId")
    suspend fun updateLikeStatus(postId: Long, isLiked: Boolean, likesCount: Int)

    @Query("UPDATE posts SET isSaved = :isSaved WHERE id = :postId")
    suspend fun updateSavedStatus(postId: Long, isSaved: Boolean)

    @Query("UPDATE posts SET commentsCount = commentsCount + 1 WHERE id = :postId")
    suspend fun incrementCommentsCount(postId: Long)

    @Delete
    suspend fun deletePost(post: PostEntity)

    @Query("SELECT COUNT(*) FROM posts")
    suspend fun getCount(): Int
}
