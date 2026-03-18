package com.operimus.Marketing.controllers;

import com.operimus.Marketing.MarketingApplication;
import com.operimus.Marketing.dto.WorkflowDTO;
import com.operimus.Marketing.entities.Node;
import com.operimus.Marketing.entities.NodeType;
import com.operimus.Marketing.entities.Workflow;
import com.operimus.Marketing.repositories.CampaignRepository;
import com.operimus.Marketing.repositories.WorkflowRepository;
import com.operimus.Marketing.security.TestSecurityConfig;
import com.operimus.Marketing.utils.JsonUtils;
import com.operimus.Marketing.services.HubSpotLeadService;
import com.operimus.Marketing.services.MarketingIndexerService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.operimus.Marketing.services.MarketingIndexerService;

@SpringBootTest(classes = {MarketingApplication.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "api.version=v1")
@Import(TestSecurityConfig.class)
public class WorkflowControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @MockBean
    private MarketingIndexerService marketingIndexerService;

    @MockBean
    private HubSpotLeadService hubSpotLeadService;

    @Value("${api.version}")
    private String apiVersion;

    @BeforeEach
    void resetDb() {
        // Delete campaigns first (they reference workflows)
        campaignRepository.deleteAll();
        workflowRepository.deleteAll();
    }

    @Test
    void givenWorkflowDTO_whenCreateWorkflow_thenStatus200() throws Exception {
        WorkflowDTO dto = new WorkflowDTO();
        dto.setName("New Workflow");
        dto.setDescription("Created via POST");

        mockMvc.perform(post("/api/" + apiVersion + "/workflows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New Workflow")))
                .andExpect(jsonPath("$.description", is("Created via POST")));
    }

    @Test
    void givenWorkflows_whenGetAllWorkflows_thenStatus200() throws Exception {
        Workflow wf1 = new Workflow();
        wf1.setName("Workflow A");
        wf1.setDescription("First");
        workflowRepository.saveAndFlush(wf1);

        Workflow wf2 = new Workflow();
        wf2.setName("Workflow B");
        wf2.setDescription("Second");
        workflowRepository.saveAndFlush(wf2);

        mockMvc.perform(get("/api/" + apiVersion + "/workflows"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Workflow A")))
                .andExpect(jsonPath("$[1].name", is("Workflow B")));
    }

    @Test
    void givenValidWorkflowId_whenGetWorkflow_thenStatus200_andNodesFetched() throws Exception {
        Workflow wf = new Workflow();
        wf.setName("Graph Workflow");

        Node start = new Node();
        start.setNodeType(NodeType.START);
        start.setIsStartNode(true);
        start.setPositionX(100);
        start.setPositionY(100);

        Node end = new Node();
        end.setNodeType(NodeType.END);
        end.setIsEndNode(true);
        end.setPositionX(300);
        end.setPositionY(100);

        start.getOutgoingNodes().add(end);
        wf.getNodes().addAll(java.util.List.of(start, end));

        Workflow saved = workflowRepository.saveAndFlush(wf);

        mockMvc.perform(get("/api/" + apiVersion + "/workflows/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Graph Workflow")))
                .andExpect(jsonPath("$.nodes", hasSize(2)))
                .andExpect(jsonPath("$.nodes[0].nodeType", is("START")))
                .andExpect(jsonPath("$.nodes[1].nodeType", is("END")))
                .andExpect(jsonPath("$.nodes[0].outgoingNodes[0].nodeType", is("END")));
    }

    @Test
    void givenInvalidWorkflowId_whenGetWorkflow_thenStatus404() throws Exception {
        mockMvc.perform(get("/api/" + apiVersion + "/workflows/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenWorkflow_whenUpdateWorkflow_thenStatus200() throws Exception {
        Workflow wf = new Workflow();
        wf.setName("Old Name");
        wf.setDescription("Old Desc");
        Workflow saved = workflowRepository.saveAndFlush(wf);

        Workflow update = new Workflow();
        update.setName("New Name");
        update.setDescription("New Desc");

        mockMvc.perform(put("/api/" + apiVersion + "/workflows/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New Name")))
                .andExpect(jsonPath("$.description", is("New Desc")));
    }

    @Test
    void givenInvalidWorkflowId_whenUpdateWorkflow_thenStatus404() throws Exception {
        Workflow update = new Workflow();
        update.setName("Ignored");

        mockMvc.perform(put("/api/" + apiVersion + "/workflows/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(update)))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenValidWorkflowId_whenDeleteWorkflow_thenStatus204() throws Exception {
        Workflow wf = new Workflow();
        wf.setName("To Delete");
        Workflow saved = workflowRepository.saveAndFlush(wf);

        mockMvc.perform(delete("/api/" + apiVersion + "/workflows/" + saved.getId()))
                .andExpect(status().isNoContent());

        assertThat(workflowRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void givenInvalidWorkflowId_whenDeleteWorkflow_thenStatus404() throws Exception {
        mockMvc.perform(delete("/api/" + apiVersion + "/workflows/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenWorkflow_whenGetNodes_thenStatus200() throws Exception {
        Workflow wf = new Workflow();
        wf.setName("Node List Workflow");
        Node node = new Node();
        node.setNodeType(NodeType.EMAIL);
        wf.getNodes().add(node);
        Workflow saved = workflowRepository.saveAndFlush(wf);

        mockMvc.perform(get("/api/" + apiVersion + "/workflows/" + saved.getId() + "/nodes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nodeType", is("EMAIL")));
    }

    @Test
    void givenInvalidWorkflowId_whenGetNodes_thenStatus404() throws Exception {
        mockMvc.perform(get("/api/" + apiVersion + "/workflows/99999/nodes"))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenValidWorkflow_whenAddNode_thenStatus201() throws Exception {
        Workflow wf = new Workflow();
        wf.setName("Test Add Node");
        Workflow saved = workflowRepository.saveAndFlush(wf);

        Node newNode = new Node();
        newNode.setNodeType(NodeType.EMAIL);
        newNode.setPositionX(200);
        newNode.setPositionY(200);

        mockMvc.perform(post("/api/" + apiVersion + "/workflows/" + saved.getId() + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(newNode)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nodeType", is("EMAIL")))
                .andExpect(jsonPath("$.positionX", is(200)));
    }

    @Test
    void givenInvalidWorkflowId_whenAddNode_thenStatus404() throws Exception {
        Node node = new Node();
        node.setNodeType(NodeType.EMAIL);

        mockMvc.perform(post("/api/" + apiVersion + "/workflows/99999/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(node)))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenValidIds_whenGetNode_thenStatus200() throws Exception {
        Workflow wf = new Workflow();
        wf.setName("Node List Workflow");
        Node node = new Node();
        node.setNodeType(NodeType.START);
        wf.getNodes().add(node);
        Workflow savedWf = workflowRepository.saveAndFlush(wf);
        Long nodeId = savedWf.getNodes().get(0).getId();

        mockMvc.perform(get("/api/" + apiVersion + "/workflows/" + savedWf.getId() + "/nodes/" + nodeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodeType", is("START")));
    }

    @Test
    void givenInvalidNodeId_whenGetNode_thenStatus404() throws Exception {
        Workflow wf = new Workflow();
        wf.setName("Node List Workflow");
        Workflow saved = workflowRepository.saveAndFlush(wf);

        mockMvc.perform(get("/api/" + apiVersion + "/workflows/" + saved.getId() + "/nodes/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenValidIds_whenDeleteNode_thenStatus204() throws Exception {
        Workflow wf = new Workflow();
        wf.setName("Delete Node Workflow");
        Node node = new Node();
        node.setNodeType(NodeType.EMAIL);
        wf.getNodes().add(node);
        Workflow savedWf = workflowRepository.saveAndFlush(wf);
        Long nodeId = savedWf.getNodes().get(0).getId();

        mockMvc.perform(delete("/api/" + apiVersion + "/workflows/" + savedWf.getId() + "/nodes/" + nodeId))
                .andExpect(status().isNoContent());

        Workflow refreshed = workflowRepository.findByIdWithNodes(savedWf.getId()).orElse(null);
        assertThat(refreshed).isNotNull();
        assertThat(refreshed.getNodes()).isEmpty();
    }

    @Test
    void givenInvalidNodeId_whenDeleteNode_thenStatus404() throws Exception {
        Workflow wf = new Workflow();
        wf.setName("Delete Node Workflow");
        Workflow saved = workflowRepository.saveAndFlush(wf);

        mockMvc.perform(delete("/api/" + apiVersion + "/workflows/" + saved.getId() + "/nodes/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenWorkflowWithNodes_whenClearNodes_thenStatus204_andEmpty() throws Exception {
        Workflow wf = new Workflow();
        wf.setName("Clear Nodes Workflow");
        wf.getNodes().add(new Node());
        wf.getNodes().add(new Node());
        Workflow saved = workflowRepository.saveAndFlush(wf);

        mockMvc.perform(delete("/api/" + apiVersion + "/workflows/" + saved.getId() + "/nodes/clear"))
                .andExpect(status().isNoContent());

        Workflow refreshed = workflowRepository.findByIdWithNodes(saved.getId()).orElse(null);
        assertThat(refreshed).isNotNull();
        assertThat(refreshed.getNodes()).isEmpty();
    }

    @Test
    void givenValidIds_whenUpdateNode_thenStatus200() throws Exception {
        Workflow wf = new Workflow();
        wf.setName("Update Node Workflow");
        Node node = new Node();
        node.setNodeType(NodeType.EMAIL);
        node.setPositionX(100);
        node.setPositionY(100);
        wf.getNodes().add(node);
        Workflow savedWf = workflowRepository.saveAndFlush(wf);
        Long nodeId = savedWf.getNodes().get(0).getId();

        Node update = new Node();
        update.setNodeType(NodeType.EMAIL);
        update.setPositionX(500);
        update.setPositionY(500);

        mockMvc.perform(put("/api/" + apiVersion + "/workflows/" + savedWf.getId() + "/nodes/" + nodeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodeType", is("EMAIL")))
                .andExpect(jsonPath("$.positionX", is(500)));
    }

    @Test
    void givenInvalidNodeId_whenUpdateNode_thenStatus404() throws Exception {
        Workflow wf = new Workflow();
        wf.setName("Update Node Workflow");
        Workflow saved = workflowRepository.saveAndFlush(wf);

        Node update = new Node();
        update.setNodeType(NodeType.END);

        mockMvc.perform(put("/api/" + apiVersion + "/workflows/" + saved.getId() + "/nodes/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(update)))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenValidIds_whenAddConnection_thenStatus201() throws Exception {
        Workflow wf = new Workflow();
        wf.setName("Add Connection Workflow");
        Node from = new Node();
        from.setNodeType(NodeType.START);
        Node to = new Node();
        to.setNodeType(NodeType.END);
        wf.getNodes().addAll(java.util.List.of(from, to));
        Workflow savedWf = workflowRepository.saveAndFlush(wf);
        Long fromId = savedWf.getNodes().get(0).getId();
        Long toId = savedWf.getNodes().get(1).getId();

        mockMvc.perform(post("/api/" + apiVersion + "/workflows/" + savedWf.getId() + "/nodes/" + fromId + "/edges/" + toId))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/" + apiVersion + "/workflows/" + savedWf.getId() + "/nodes/" + fromId + "/edges/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nodeType", is("END")));
    }

    @Test
    void givenInvalidFromNodeId_whenAddConnection_thenStatus404() throws Exception {
        Workflow wf = new Workflow();
        wf.setName("Add Connection Workflow");
        Workflow saved = workflowRepository.saveAndFlush(wf);

        mockMvc.perform(post("/api/" + apiVersion + "/workflows/" + saved.getId() + "/nodes/99999/edges/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenValidIds_whenGetConnection_thenStatus200() throws Exception {
        Workflow wf = new Workflow();
        wf.setName("Get Connection Workflow");
        Node from = new Node();
        Node to = new Node();
        from.getOutgoingNodes().add(to);
        wf.getNodes().addAll(java.util.List.of(from, to));
        Workflow saved = workflowRepository.saveAndFlush(wf);
        Long fromId = saved.getNodes().get(0).getId();

        mockMvc.perform(get("/api/" + apiVersion + "/workflows/" + saved.getId() + "/nodes/" + fromId + "/edges/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void givenInvalidFromNodeId_whenGetConnection_thenStatus404() throws Exception {
        Workflow wf = new Workflow();
        wf.setName("Get Connection Workflow");
        Workflow saved = workflowRepository.saveAndFlush(wf);

        mockMvc.perform(get("/api/" + apiVersion + "/workflows/" + saved.getId() + "/nodes/99999/edges/"))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenValidIds_whenDeleteConnection_thenStatus204() throws Exception {
        Workflow wf = new Workflow();
        wf.setName("Delete Connection Workflow");
        Node from = new Node();
        Node to = new Node();
        from.getOutgoingNodes().add(to);
        wf.getNodes().addAll(java.util.List.of(from, to));
        Workflow saved = workflowRepository.saveAndFlush(wf);
        Long fromId = saved.getNodes().get(0).getId();
        Long toId = saved.getNodes().get(1).getId();

        mockMvc.perform(delete("/api/" + apiVersion + "/workflows/" + saved.getId() + "/nodes/" + fromId + "/edges/" + toId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/" + apiVersion + "/workflows/" + saved.getId() + "/nodes/" + fromId + "/edges/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void givenInvalidToNodeId_whenDeleteConnection_thenStatus404() throws Exception {
        Workflow wf = new Workflow();
        wf.setName("Delete Connection Workflow");
        Node from = new Node();
        wf.getNodes().add(from);
        Workflow saved = workflowRepository.saveAndFlush(wf);
        Long fromId = saved.getNodes().get(0).getId();

        mockMvc.perform(delete("/api/" + apiVersion + "/workflows/" + saved.getId() + "/nodes/" + fromId + "/edges/99999"))
                .andExpect(status().isNotFound());
    }
}