package com.operimus.Marketing.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.operimus.Marketing.entities.Dashboard;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardRepository extends JpaRepository<Dashboard, Long> {
    
}
