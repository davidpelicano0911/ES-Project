package com.operimus.Marketing.repositories;

import com.operimus.Marketing.entities.Workflow;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
    boolean existsByName(String name);

    @Query("""
        SELECT DISTINCT w
        FROM Workflow w
        LEFT JOIN FETCH w.nodes n
        LEFT JOIN FETCH n.outgoingNodes
        WHERE w.id = :id
    """)
    Optional<Workflow> findByIdWithNodes(@Param("id") Long id);
}
