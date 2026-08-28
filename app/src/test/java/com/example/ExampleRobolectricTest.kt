package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.repository.TeaGramRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: TeaGramRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = TeaGramRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun read_string_from_context() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TeaGram", appName)
    }

    @Test
    fun seed_initial_data_and_verify_posts_and_stories() = runBlocking {
        repository.seedInitialDataIfNeeded()

        val posts = repository.allPosts.first()
        val stories = repository.allStories.first()

        assertTrue(posts.isNotEmpty())
        assertTrue(stories.isNotEmpty())
        assertEquals("elena.voyages", posts[0].authorUsername)
    }

    @Test
    fun toggle_post_like_updates_like_count_and_state() = runBlocking {
        repository.seedInitialDataIfNeeded()

        val initialPosts = repository.allPosts.first()
        val target = initialPosts.first()
        val initialLiked = target.isLiked
        val initialLikes = target.likesCount

        repository.toggleLike(target.id)

        val updatedPosts = repository.allPosts.first()
        val updatedTarget = updatedPosts.first { it.id == target.id }

        assertEquals(!initialLiked, updatedTarget.isLiked)
        assertEquals(if (initialLiked) initialLikes - 1 else initialLikes + 1, updatedTarget.likesCount)
    }

    @Test
    fun add_comment_updates_comments_count_on_post() = runBlocking {
        repository.seedInitialDataIfNeeded()

        val initialPosts = repository.allPosts.first()
        val target = initialPosts.first()
        val initialCommentsCount = target.commentsCount

        repository.addComment(
            postId = target.id,
            text = "Awesome photo!",
            username = "test_user"
        )

        val comments = repository.getCommentsForPost(target.id).first()
        assertTrue(comments.any { it.text == "Awesome photo!" })

        val updatedPosts = repository.allPosts.first()
        val updatedPost = updatedPosts.first { it.id == target.id }
        assertEquals(initialCommentsCount + 1, updatedPost.commentsCount)
    }

    @Test
    fun follow_and_unfollow_user_and_verify_feed_filtering() = runBlocking {
        repository.seedInitialDataIfNeeded()

        val currentUsername = "sarah.creatives"
        val targetUsername = "marcus_lens"

        // Initially check following
        val initialFollowing = repository.isFollowing(currentUsername, targetUsername).first()
        assertEquals(false, initialFollowing)

        // Follow user
        repository.toggleFollow(currentUsername, targetUsername)
        val afterFollow = repository.isFollowing(currentUsername, targetUsername).first()
        assertEquals(true, afterFollow)

        // Verify feed now includes posts from target user
        val feed = repository.getFeedPosts(currentUsername).first()
        assertTrue(feed.any { it.authorUsername == targetUsername })

        // Unfollow user
        repository.toggleFollow(currentUsername, targetUsername)
        val afterUnfollow = repository.isFollowing(currentUsername, targetUsername).first()
        assertEquals(false, afterUnfollow)

        // Feed no longer includes posts from target user
        val feedAfterUnfollow = repository.getFeedPosts(currentUsername).first()
        assertTrue(feedAfterUnfollow.none { it.authorUsername == targetUsername })
    }

    @Test
    fun update_profile_updates_username_bio_and_avatar() = runBlocking {
        repository.seedInitialDataIfNeeded()

        repository.updateUserProfile(
            oldUsername = "sarah.creatives",
            newUsername = "sarah.lens",
            displayName = "Sarah Lens",
            bio = "Visual Architect & Storyteller",
            avatarResName = "img_feed_arch",
            websiteUrl = "https://sarahlens.design"
        )

        val updatedUser = repository.currentUser.first()
        assertEquals("sarah.lens", updatedUser?.username)
        assertEquals("Sarah Lens", updatedUser?.displayName)
        assertEquals("Visual Architect & Storyteller", updatedUser?.bio)
        assertEquals("img_feed_arch", updatedUser?.avatarResName)
        assertEquals("https://sarahlens.design", updatedUser?.websiteUrl)

        // Verify posts authored by old username got migrated to new username
        val posts = repository.allPosts.first()
        assertTrue(posts.any { it.authorUsername == "sarah.lens" })
        assertTrue(posts.none { it.authorUsername == "sarah.creatives" })
    }

    @Test
    fun feed_pagination_loads_requested_limit() = runBlocking {
        repository.seedInitialDataIfNeeded()

        val currentUsername = "sarah.creatives"
        val page1 = repository.getFeedPosts(currentUsername, limit = 2).first()
        val page2 = repository.getFeedPosts(currentUsername, limit = 4).first()

        assertEquals(2, page1.size)
        assertTrue(page2.size > page1.size)
    }
}
