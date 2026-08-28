package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.FollowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowDao {
    @Query("SELECT EXISTS(SELECT 1 FROM follows WHERE followerUsername = :follower AND followingUsername = :target)")
    fun isFollowing(follower: String, target: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM follows WHERE followerUsername = :follower AND followingUsername = :target)")
    suspend fun isFollowingDirect(follower: String, target: String): Boolean

    @Query("SELECT followingUsername FROM follows WHERE followerUsername = :follower")
    fun getFollowingUsernames(follower: String): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM follows WHERE followerUsername = :follower")
    fun getFollowingCount(follower: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM follows WHERE followingUsername = :target")
    fun getFollowersCount(target: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollow(follow: FollowEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollows(follows: List<FollowEntity>)

    @Query("DELETE FROM follows WHERE followerUsername = :follower AND followingUsername = :target")
    suspend fun deleteFollow(follower: String, target: String)

    @Query("SELECT COUNT(*) FROM follows")
    suspend fun getCount(): Int

    @Query("UPDATE follows SET followerUsername = :newUsername WHERE followerUsername = :oldUsername")
    suspend fun updateFollowerUsername(oldUsername: String, newUsername: String)

    @Query("UPDATE follows SET followingUsername = :newUsername WHERE followingUsername = :oldUsername")
    suspend fun updateFollowingUsername(oldUsername: String, newUsername: String)
}
