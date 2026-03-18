package com.operimus.Marketing.repositories;

import com.operimus.Marketing.entities.*;
import com.operimus.Marketing.services.FacebookApiService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.DiscriminatorValue;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PostRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PostRepository postRepository;

    @Entity
    @DiscriminatorValue("TEST_PLATFORM")
    public static class TestPostPlatform extends PostPlatform {
        
        @Override
        public void publish(FacebookApiService facebookApiService) {
            try {
                setStatus(PostStatus.PUBLISHED);
            } catch (Exception e) {
                setStatus(PostStatus.FAILED);
            }
        }
    
    }

    @BeforeEach
    void cleanUp() {
        postRepository.deleteAll();
    }

    @Test
    void whenSave_thenPostIsPersisted() {
        Post post = new Post();
        post.setName("Test Post");
        post.setDescription("Testing persistence");
        post.setScheduled_date(LocalDateTime.now());
        post.setFile_path("/uploads/test.jpg");

        Post saved = postRepository.save(post);

        assertNotNull(saved.getId());
        assertEquals("Test Post", saved.getName());
        assertEquals("/uploads/test.jpg", saved.getFile_path());
    }

    @Test
    void whenFindById_thenReturnPost() {
        Post post = new Post();
        post.setName("Find Post");
        post.setDescription("Testing retrieval");
        post.setScheduled_date(LocalDateTime.now());
        entityManager.persist(post);
        entityManager.flush();

        Post found = postRepository.findById(post.getId()).orElse(null);

        assertNotNull(found);
        assertEquals("Find Post", found.getName());
    }

    @Test
    void whenDelete_thenPostIsRemoved() {
        Post post = new Post();
        post.setName("To Delete");
        post.setDescription("Testing delete");
        entityManager.persist(post);
        entityManager.flush();

        postRepository.delete(post);
        entityManager.flush();

        Post found = postRepository.findById(post.getId()).orElse(null);
        assertNull(found);
    }

    @Test
    void whenUpdate_thenChangesArePersisted() {
        Post post = new Post();
        post.setName("Old Name");
        post.setDescription("Initial");
        post.setScheduled_date(LocalDateTime.now());
        entityManager.persist(post);
        entityManager.flush();

        post.setName("Updated Name");
        Post updated = postRepository.save(post);

        assertEquals("Updated Name", updated.getName());
    }

    @Test
    void whenFindScheduledPostsReadyToPublish_thenReturnMatchingPosts() {
        LocalDateTime now = LocalDateTime.now();

        // Post 1: scheduled in the past, with SCHEDULED platform (should match)
        Post post1 = new Post();
        post1.setName("Ready Post");
        post1.setDescription("Ready to publish");
        post1.setScheduled_date(now.minusHours(2));

        TestPostPlatform platform1 = new TestPostPlatform();
        platform1.setPost(post1);
        platform1.setStatus(PostStatus.SCHEDULED);
        post1.getPlatforms().add(platform1);
        entityManager.persist(post1);

        // Post 2: scheduled in the past, but platform PUBLISHED (should NOT match)
        Post post2 = new Post();
        post2.setName("Already Published");
        post2.setDescription("Not to be included");
        post2.setScheduled_date(now.minusHours(1));

        TestPostPlatform platform2 = new TestPostPlatform();
        platform2.setPost(post2);
        platform2.setStatus(PostStatus.PUBLISHED);
        post2.getPlatforms().add(platform2);
        entityManager.persist(post2);

        // Post 3: scheduled in the future, SCHEDULED (should NOT match)
        Post post3 = new Post();
        post3.setName("Future Post");
        post3.setDescription("Should not be included");
        post3.setScheduled_date(now.plusHours(3));

        TestPostPlatform platform3 = new TestPostPlatform();
        platform3.setPost(post3);
        platform3.setStatus(PostStatus.SCHEDULED);
        post3.getPlatforms().add(platform3);
        entityManager.persist(post3);

        entityManager.flush();

        List<Post> result = postRepository.findScheduledPostsReadyToPublish(now);

        assertEquals(1, result.size());
        assertEquals("Ready Post", result.get(0).getName());
    }

    @Test
    void whenFindAll_thenReturnAllPosts() {
        Post post1 = new Post();
        post1.setName("Post 1");
        post1.setDescription("Desc 1");
        entityManager.persist(post1);

        Post post2 = new Post();
        post2.setName("Post 2");
        post2.setDescription("Desc 2");
        entityManager.persist(post2);

        entityManager.flush();

        List<Post> posts = postRepository.findAll();

        assertEquals(2, posts.size());
    }
}