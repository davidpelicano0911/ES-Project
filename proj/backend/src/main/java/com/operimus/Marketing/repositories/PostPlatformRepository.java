package com.operimus.Marketing.repositories;

import com.operimus.Marketing.entities.PostPlatform;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostPlatformRepository extends JpaRepository<PostPlatform, Long> {

    Optional<PostPlatform> findByPostId(Long postId);
}