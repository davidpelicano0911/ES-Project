package com.operimus.Marketing.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.operimus.Marketing.entities.Lead;
import java.util.Optional;
import java.util.Date;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<Lead> findByEmail(String email);
    
    Optional<Lead> findByPhoneNumber(String phoneNumber);

    Optional<Lead> findByCrmId(String crmId);

    @Query("SELECT MAX(l.lastSyncedAt) FROM Lead l")
    Date findLatestSyncDate();
}
