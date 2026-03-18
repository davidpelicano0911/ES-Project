package com.operimus.Marketing.repositories;

import com.operimus.Marketing.entities.LandingPage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LandingPageRepository extends JpaRepository<LandingPage, Long> {
    boolean existsByName(String name);
}
