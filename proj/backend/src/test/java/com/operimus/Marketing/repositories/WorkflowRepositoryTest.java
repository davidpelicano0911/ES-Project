package com.operimus.Marketing.repositories;

import com.operimus.Marketing.entities.Node;
import com.operimus.Marketing.entities.NodeType;
import com.operimus.Marketing.entities.Workflow;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class WorkflowRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private WorkflowRepository workflowRepository;

    @BeforeEach
    void cleanUp() {
        workflowRepository.deleteAll();
    }

    @Test
    void whenFindById_thenReturnWorkflow() {
        Workflow workflow = new Workflow();
        workflow.setName("Test Workflow");
        workflow.setDescription("A test workflow");
        entityManager.persist(workflow);
        entityManager.flush();

        Workflow found = workflowRepository.findById(workflow.getId()).orElse(null);

        assertNotNull(found);
        assertEquals("Test Workflow", found.getName());
        assertEquals("A test workflow", found.getDescription());
    }

    @Test
    void whenSave_thenWorkflowIsPersisted() {
        Workflow workflow = new Workflow();
        workflow.setName("New Workflow");
        workflow.setDescription("Persisted workflow");

        Workflow saved = workflowRepository.save(workflow);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertEquals("New Workflow", saved.getName());
        assertEquals("Persisted workflow", saved.getDescription());
    }

    @Test
    void whenDelete_thenWorkflowIsRemoved() {
        Workflow workflow = new Workflow();
        workflow.setName("To Be Deleted");
        entityManager.persist(workflow);
        entityManager.flush();

        workflowRepository.delete(workflow);

        Workflow deleted = workflowRepository.findById(workflow.getId()).orElse(null);
        assertNull(deleted);
    }

    @Test
    void whenExistsByName_thenReturnTrue() {
        Workflow workflow = new Workflow();
        workflow.setName("Existing Workflow");
        entityManager.persist(workflow);
        entityManager.flush();

        boolean exists = workflowRepository.existsByName("Existing Workflow");

        assertTrue(exists);
    }

    @Test
    void whenNotExistsByName_thenReturnFalse() {
        boolean exists = workflowRepository.existsByName("Nonexistent Workflow");

        assertFalse(exists);
    }

    @Test
    void whenFindAll_thenReturnAllWorkflows() {
        Workflow wf1 = new Workflow();
        wf1.setName("Workflow 1");
        entityManager.persist(wf1);

        Workflow wf2 = new Workflow();
        wf2.setName("Workflow 2");
        entityManager.persist(wf2);

        entityManager.flush();

        var workflows = workflowRepository.findAll();

        assertEquals(3, workflows.size());
    }

    @Test
    void whenFindByIdWithNodes_thenReturnWorkflowWithEagerFetchedNodesAndConnections() {
        Workflow workflow = new Workflow();
        workflow.setName("Graph Workflow");
        entityManager.persist(workflow);

        Node start = new Node();
        start.setNodeType(NodeType.START);
        start.setIsStartNode(true);
        start.setPositionX(100);
        start.setPositionY(100);

        Node action = new Node();
        action.setNodeType(NodeType.EMAIL);
        action.setPositionX(300);
        action.setPositionY(100);

        Node end = new Node();
        end.setNodeType(NodeType.END);
        end.setIsEndNode(true);
        end.setPositionX(500);
        end.setPositionY(100);

        workflow.addNode(start);
        workflow.addNode(action);
        workflow.addNode(end);

        start.addOutgoingNode(action);
        action.addOutgoingNode(end);

        entityManager.persist(start);
        entityManager.persist(action);
        entityManager.persist(end);
        entityManager.flush();

        entityManager.clear();

        Optional<Workflow> result = workflowRepository.findByIdWithNodes(workflow.getId());

        assertTrue(result.isPresent());
        Workflow fetched = result.get();

        assertEquals("Graph Workflow", fetched.getName());
        assertEquals(3, fetched.getNodes().size());

        Node fetchedStart = fetched.getNodes().stream()
                .filter(n -> n.getNodeType() == NodeType.START)
                .findFirst().orElse(null);
        assertNotNull(fetchedStart);
        assertTrue(fetchedStart.getIsStartNode());

        assertEquals(1, fetchedStart.getOutgoingNodes().size());
        Node connectedAction = fetchedStart.getOutgoingNodes().iterator().next();
        assertNotNull(connectedAction);
        assertEquals(NodeType.EMAIL, connectedAction.getNodeType());

        assertEquals(1, connectedAction.getOutgoingNodes().size());
        Node connectedEnd = connectedAction.getOutgoingNodes().iterator().next();
        assertNotNull(connectedEnd);
        assertTrue(connectedEnd.getIsEndNode());
    }

    @Test
    void whenFindByIdWithNodes_andWorkflowNotFound_thenReturnEmpty() {
        Optional<Workflow> result = workflowRepository.findByIdWithNodes(999L);

        assertFalse(result.isPresent());
    }
}