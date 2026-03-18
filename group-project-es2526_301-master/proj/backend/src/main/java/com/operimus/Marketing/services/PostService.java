package com.operimus.Marketing.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;


import com.operimus.Marketing.repositories.PostRepository;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Map;

import com.operimus.Marketing.entities.FacebookPlatform;
import com.operimus.Marketing.entities.Post;
import com.operimus.Marketing.entities.PostPlatform;
import java.util.ArrayList;
import com.operimus.Marketing.dto.PostDTO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.nio.file.Path;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import com.operimus.Marketing.repositories.PostPlatformRepository;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private FacebookApiService facebookApiService;

    @Autowired
    private PostPlatformRepository postPlatformRepository;

    // Inject the API version
    @Value("${api.version}")
    private String apiVersion;

    private final Path uploadPath = Paths.get(System.getProperty("upload.dir", "uploads/"));

    public List<Post> getAllPosts() {
        List<Post> posts = postRepository.findAll();

        posts.forEach(post -> {
            if (post.getPlatforms() != null) {
                post.getPlatforms().forEach(platform -> {
                    String url = facebookApiService.getPublicPostUrl(platform.getPlatformPostId());
                    platform.setPostUrl(url);
                });
            }
        });

        return posts;
    }


    public Post createPost(PostDTO postDTO) {

        if (postRepository.existsByName(postDTO.getName())) {
            throw new IllegalArgumentException("Post with the same name already exists");
        }

        if (postDTO.getScheduled_date() != null && postDTO.getScheduled_date().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Scheduled date must be in the future");
        }

        if (postDTO.getPlatforms() == null || postDTO.getPlatforms().isEmpty()) {
            throw new IllegalArgumentException("Cannot schedule a post without platforms");
        }

        Post post = new Post();
        String filename = null; 
        if (postDTO.getImage() != null && !postDTO.getImage().isEmpty()) {
            

            filename = Paths.get(postDTO.getImage().getOriginalFilename()).getFileName().toString();
            try {
                Files.createDirectories(uploadPath);
                postDTO.getImage().transferTo(uploadPath.resolve(filename));
            } catch (IOException e) {
                throw new RuntimeException("Failed to store image file", e);
            }
        }

        post.setName(postDTO.getName());
        post.setDescription(postDTO.getDescription());
        post.setFile_path(filename);
        post.setScheduled_date(
            postDTO.getScheduled_date() != null ? postDTO.getScheduled_date() : LocalDateTime.now().plusSeconds(1)
        );

        List<PostPlatform> platforms = new ArrayList<>();
        for (String platformString : postDTO.getPlatforms()) {
            PostPlatform platform = PostPlatformFactory.create(platformString, post);
            platforms.add(platform);
        }

        post.setPlatforms(new ArrayList<>(platforms));
        return postRepository.save(post);
    }


    public boolean deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            return false;
        }
        postRepository.deleteById(id);
        return true;
    }


    public Post getPostById(Long id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) return null;

        if (post.getPlatforms() != null) {
            post.getPlatforms().forEach(platform -> {
                String url = facebookApiService.getPublicPostUrl(platform.getPlatformPostId());
                platform.setPostUrl(url);
            });
        }

        return post;
    }



    @Transactional
    public Post updatePost(Long id, PostDTO postDTO) {
        Post existingPost = postRepository.findById(id).orElse(null);
        if (existingPost == null) {
            return null;
        }

        // Atualiza apenas campos presentes
        if (postDTO.getName() != null) {
            existingPost.setName(postDTO.getName());
        }

        if (postDTO.getDescription() != null) {
            existingPost.setDescription(postDTO.getDescription());
        }

        if (postDTO.getScheduled_date() != null) {
            if (postDTO.getScheduled_date().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Scheduled date must be in the future");
            }
            existingPost.setScheduled_date(postDTO.getScheduled_date());
        }

        if (postDTO.getPlatforms() != null && !postDTO.getPlatforms().isEmpty()) {
            existingPost.getPlatforms().clear();

            for (String platformString : postDTO.getPlatforms()) {
                PostPlatform platform = PostPlatformFactory.create(platformString, existingPost);
                existingPost.getPlatforms().add(platform);
            }
        }


        if (postDTO.getImage() != null && !postDTO.getImage().isEmpty()) {
            try {
                Files.createDirectories(uploadPath);
                String filename = Paths.get(postDTO.getImage().getOriginalFilename())
                                    .getFileName().toString();
                postDTO.getImage().transferTo(uploadPath.resolve(filename));
                existingPost.setFile_path(filename);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store image file", e);
            }
        }

        return postRepository.save(existingPost);
    }

    public void publish(Post post) {
        for (PostPlatform platform : post.getPlatforms()) {
            if (platform instanceof FacebookPlatform facebookPlatform) {
                facebookPlatform.publish(facebookApiService);
                postPlatformRepository.save(facebookPlatform);
            }
        }

    }

   public Resource getImage(String filename) {
        Path filePath = uploadPath.resolve(filename).normalize();
        if (Files.exists(filePath)) {
            return new FileSystemResource(filePath.toFile());
        }
        return null;
    }

    public Map<String, Integer> getPostStatistics(Long postId) {
        PostPlatform platform = postPlatformRepository.findByPostId(postId)
                .orElseThrow(() -> new RuntimeException("Post platform record not found"));

        if (platform.getPlatformPostId() == null) {
            throw new RuntimeException("This post has not been published on Facebook yet");
        }

        Map<String, Integer> stats = facebookApiService.getPostStatistics(platform.getPlatformPostId());

        Integer reach = facebookApiService.getPostReach(platform.getPlatformPostId());
        stats.put("reach", reach);

        // Atualiza a BD
        platform.setNumberLikes(stats.get("likes"));
        platform.setNumberShares(stats.get("shares"));
        platform.setNumberComments(stats.get("comments"));
        platform.setNumberReachs(reach);


        postPlatformRepository.save(platform);

        return stats;
    }


}