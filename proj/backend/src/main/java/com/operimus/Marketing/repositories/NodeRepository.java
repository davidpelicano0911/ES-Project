package com.operimus.Marketing.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.operimus.Marketing.entities.Node;
import com.operimus.Marketing.entities.EventType;
import com.operimus.Marketing.entities.Workflow;
import java.util.List;

@Repository
public interface NodeRepository extends JpaRepository<Node, Long> {

    List<Node> findByEventType(EventType eventType);

    List<Node> findByWorkflowAndEventType(Workflow workflow, EventType eventType);
}
