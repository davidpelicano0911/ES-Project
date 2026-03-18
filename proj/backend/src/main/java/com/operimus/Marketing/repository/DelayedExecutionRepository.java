package com.operimus.Marketing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.operimus.Marketing.entities.DelayedExecution;
import com.operimus.Marketing.entities.DelayedExecution.DelayedExecutionStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DelayedExecutionRepository extends JpaRepository<DelayedExecution, Long> {
    
    @Query("SELECT de FROM DelayedExecution de WHERE de.executionTime <= :currentTime AND de.status = :status")
    List<DelayedExecution> findExecutionsReadyToRun(
        @Param("currentTime") LocalDateTime currentTime, 
        @Param("status") DelayedExecutionStatus status
    );
    
    List<DelayedExecution> findByWorkflowInstanceIdAndStatus(
        Long workflowInstanceId, 
        DelayedExecutionStatus status
    );
    
    List<DelayedExecution> findByWorkflowInstanceId(Long workflowInstanceId);
}