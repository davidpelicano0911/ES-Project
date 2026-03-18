package com.operimus.Marketing.services;

import com.operimus.Marketing.entities.Post;
import com.operimus.Marketing.repositories.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostScheduler {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostService postService;

    @Scheduled(fixedRate = 30000) // every 60 seconds
    @Transactional
    public void publishScheduledPosts() {
        LocalDateTime now = LocalDateTime.now();
        List<Post> posts = postRepository.findScheduledPostsReadyToPublish(now);
        for (Post post : posts) {
            postService.publish(post);
            postRepository.save(post);
        }
    }
}
