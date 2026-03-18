package com.operimus.Marketing.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.operimus.Marketing.entities.Post;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // Fetch posts whose scheduled date <= now and have at least one PENDING platform
    @Query("SELECT DISTINCT p FROM Post p JOIN p.platforms pp " +
           "WHERE p.scheduled_date <= :now AND pp.status = 'SCHEDULED'")
    List<Post> findScheduledPostsReadyToPublish(@Param("now") LocalDateTime now);

    boolean existsByName(String name);
}

    
