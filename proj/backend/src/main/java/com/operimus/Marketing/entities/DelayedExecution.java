package com.operimus.Marketing.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "delayed_executions")
public class DelayedExecution {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "workflow_instance_id", nullable = false)
    private Long workflowInstanceId;
    
    @Column(name = "node_id", nullable = false)
    private Long nodeId;
    
    @Column(name = "execution_time", nullable = false)
    private LocalDateTime executionTime;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private DelayedExecutionStatus status = DelayedExecutionStatus.PENDING;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "executed_at")
    private LocalDateTime executedAt;
    
    public enum DelayedExecutionStatus {
        PENDING,
        EXECUTED,
        FAILED,
        CANCELLED
    }
}