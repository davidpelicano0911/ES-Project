package com.operimus.Marketing.services;

import com.operimus.Marketing.entities.Campaign;
import com.operimus.Marketing.entities.CampaignStatus;
import com.operimus.Marketing.entities.Dashboard;
import com.operimus.Marketing.entities.Workflow;
import com.operimus.Marketing.repositories.CampaignRepository;
import com.operimus.Marketing.repositories.DashboardRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CampaignServiceTest {
    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private DashboardRepository dashboardRepository;

    @InjectMocks
    private CampaignService campaignService;

    private Campaign campaign1;
    private Campaign campaign2;

    @BeforeEach
    void setUp() {
        campaign1 = new Campaign();
        campaign1.setId(1L);
        campaign1.setName("Campaign 1");
        campaign1.setDescription("Description 1");
        campaign1.setCreatedAt(new Date());
        campaign1.setDueDate(new Date());
        campaign1.setStatus(CampaignStatus.ACTIVE);

        campaign2 = new Campaign();
        campaign2.setId(2L);
        campaign2.setName("Campaign 2");
        campaign2.setDescription("Description 2");
        campaign2.setCreatedAt(new Date());
        campaign2.setDueDate(new Date());
        campaign2.setStatus(CampaignStatus.FINISHED);

        List<Campaign> campaigns = Arrays.asList(campaign1, campaign2);

        when(campaignRepository.findAll()).thenReturn(campaigns);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign1));
        when(campaignRepository.findById(2L)).thenReturn(Optional.of(campaign2));
        when(campaignRepository.findById(99L)).thenReturn(Optional.empty());
        when(campaignRepository.findByStatus(CampaignStatus.ACTIVE)).thenReturn(List.of(campaign1));
        when(campaignRepository.findByStatus(CampaignStatus.FINISHED)).thenReturn(List.of(campaign2));
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void whenCreateCampaign_thenReturnSavedCampaign() {
        Campaign newCampaign = new Campaign();
        newCampaign.setName("New Campaign");
        newCampaign.setDescription("New Description");
        newCampaign.setStatus(CampaignStatus.IN_PROGRESS);

        when(campaignRepository.save(any(Campaign.class))).thenReturn(newCampaign);

        Campaign saved = campaignService.createCampaign(newCampaign);

        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("New Campaign");
        verify(campaignRepository, times(1)).save(newCampaign);
    }

    @Test
    void whenGetAllCampaigns_thenReturnListOfCampaigns() {
        List<Campaign> result = campaignService.getAllCampaigns();

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).extracting(Campaign::getName).containsExactlyInAnyOrder("Campaign 1", "Campaign 2");

        verify(campaignRepository, times(1)).findAll();
    }

    @Test
    void whenGetCampaignByValidId_thenReturnCampaign() {
        Campaign found = campaignService.getCampaignById(1L);

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Campaign 1");

        verify(campaignRepository, times(1)).findById(1L);
    }

    @Test
    void whenGetCampaignByInvalidId_thenThrowNotFoundException() {
        // When & Then
        assertThatThrownBy(() -> campaignService.getCampaignById(99L))
            .isInstanceOf(ResponseStatusException.class)
            .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND)
            .hasMessageContaining("Campaign not found with id: 99");

        // Verify repository was called
        verify(campaignRepository, times(1)).findById(99L);
    }

    @Test
    void whenUpdateCampaign_thenReturnUpdatedCampaign() {
        Campaign updateData = new Campaign();
        updateData.setName("Updated Campaign");
        updateData.setDescription("Updated Description");
        updateData.setStatus(CampaignStatus.IN_PROGRESS);

        when(campaignRepository.save(argThat(c -> 
            "Updated Campaign".equals(c.getName()) && 
            c.getId() != null && 
            c.getId().equals(1L)
        ))).thenAnswer(invocation -> invocation.getArgument(0));

        Campaign result = campaignService.updateCampaign(1L, updateData);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Updated Campaign");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
        assertThat(result.getStatus()).isEqualTo(CampaignStatus.IN_PROGRESS);

        verify(campaignRepository, times(1)).findById(1L);
        verify(campaignRepository, times(1)).save(argThat(c -> 
            c.getId() != null && 
            c.getId().equals(1L) && 
            "Updated Campaign".equals(c.getName())
        ));
    }

    @Test
    void whenDeleteCampaign_thenRepositoryDeleteByIdIsCalled() {
        doNothing().when(campaignRepository).deleteById(1L);

        campaignService.deleteCampaign(1L);

        verify(campaignRepository, times(1)).deleteById(1L);
    }

    @Test
    void whenGetCampaignsByStatus_thenReturnMatchingCampaigns() {
        List<Campaign> active = campaignService.getCampaignsByStatus(CampaignStatus.ACTIVE);

        assertThat(active).isNotNull();
        assertThat(active.size()).isEqualTo(1);
        assertThat(active.get(0).getStatus()).isEqualTo(CampaignStatus.ACTIVE);

        verify(campaignRepository, times(1)).findByStatus(CampaignStatus.ACTIVE);
    }

    @Test
    void whenGetCampaignsByStatusWithNoMatches_thenReturnEmptyList() {
        when(campaignRepository.findByStatus(CampaignStatus.IN_PROGRESS)).thenReturn(List.of());

        List<Campaign> inProgress = campaignService.getCampaignsByStatus(CampaignStatus.IN_PROGRESS);

        assertThat(inProgress).isNotNull();
        assertThat(inProgress).isEmpty();

        verify(campaignRepository, times(1)).findByStatus(CampaignStatus.IN_PROGRESS);
    }
 
    @Test
    void whenCreateCampaignWithDueDateBeforeCreatedAt_thenThrowException() {
        Campaign newCampaign = new Campaign();
        newCampaign.setName("Invalid Campaign");
        newCampaign.setDescription("This campaign has an invalid due date.");
        newCampaign.setCreatedAt(new Date());
        // Set due date before created date
        newCampaign.setDueDate(new Date(newCampaign.getCreatedAt().getTime() - 86400000)); // 1 day before
        newCampaign.setStatus(CampaignStatus.IN_PROGRESS);

        try {
            campaignService.createCampaign(newCampaign);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Due date cannot be before created date");
        }

        verify(campaignRepository, times(0)).save(any(Campaign.class));
    }

@Test
    void whenGetCampaignDashboard_withValidCampaignId_thenReturnDashboard() {
        Dashboard dashboard = new Dashboard("Sales Dashboard");
        dashboard.setId(10L);
        campaign1.setDashboard(dashboard);
        dashboard.setCampaign(campaign1);

        Dashboard result = campaignService.getCampaignDashboard(1L);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Sales Dashboard");
        assertThat(result.getId()).isEqualTo(10L);
        verify(campaignRepository, times(1)).findById(1L);
    }

    @Test
    void whenGetCampaignDashboard_withInvalidCampaignId_thenReturnNull() {
        Dashboard result = campaignService.getCampaignDashboard(99L);

        assertThat(result).isNull();
        verify(campaignRepository, times(1)).findById(99L);
    }

    @Test
    void whenGetCampaignDashboard_withCampaignWithoutDashboard_thenReturnNull() {
        campaign2.setDashboard(null);

        Dashboard result = campaignService.getCampaignDashboard(2L);

        assertThat(result).isNull();
        verify(campaignRepository, times(1)).findById(2L);
    }

    @Test
    void whenCreateCampaignDashboard_withValidCampaignId_thenDashboardIsLinkedAndSaved() {
        Dashboard newDashboard = new Dashboard("New Analytics Dashboard");

        when(dashboardRepository.save(any(Dashboard.class))).thenAnswer(i -> {
            Dashboard d = i.getArgument(0);
            d.setId(100L);
            return d;
        });

        Dashboard created = campaignService.createCampaignDashboard(1L, newDashboard);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(100L);
        assertThat(created.getTitle()).isEqualTo("New Analytics Dashboard");
        assertThat(created.getCampaign()).isEqualTo(campaign1);
        assertThat(campaign1.getDashboard()).isEqualTo(created);

        verify(campaignRepository, times(1)).findById(1L);
        verify(dashboardRepository, times(1)).save(created);
        verify(campaignRepository, times(1)).save(campaign1);
    }

    @Test
    void whenCreateCampaignDashboard_withInvalidCampaignId_thenReturnNull() {
        Dashboard newDashboard = new Dashboard("Should Not Be Saved");

        Dashboard created = campaignService.createCampaignDashboard(99L, newDashboard);

        assertThat(created).isNull();
        verify(campaignRepository, times(1)).findById(99L);
        verify(dashboardRepository, never()).save(any());
        verify(campaignRepository, never()).save(any());
    }

    @Test
    void whenUpdateCampaignDashboard_withValidCampaignId_thenDashboardIsUpdatedAndSaved() {
        Dashboard existingDashboard = new Dashboard("Old Dashboard");
        existingDashboard.setId(50L); 
        campaign1.setDashboard(existingDashboard);
        existingDashboard.setCampaign(campaign1);

        Dashboard updateData = new Dashboard("Updated Dashboard Title");

        when(dashboardRepository.save(any(Dashboard.class))).thenAnswer(i -> {
            Dashboard d = i.getArgument(0);
            if (d.getId() == null) d.setId(100L);
            return d;
        });

        Dashboard updated = campaignService.updateCampaignDashboard(1L, updateData);

        assertThat(updated).isNotNull();
        
        assertThat(updated.getId()).isEqualTo(50L); 
        
        assertThat(updated.getTitle()).isEqualTo("Updated Dashboard Title");
        assertThat(updated.getCampaign()).isEqualTo(campaign1);
        assertThat(campaign1.getDashboard()).isEqualTo(updated);

        verify(campaignRepository, times(1)).findById(1L);
        verify(dashboardRepository, times(1)).save(updated);
    }

    @Test
    void whenUpdateCampaignDashboard_withInvalidCampaignId_thenThrowNotFound() {
        Dashboard updateData = new Dashboard("Ignored");

        org.junit.jupiter.api.Assertions.assertThrows(ResponseStatusException.class, () -> {
            campaignService.updateCampaignDashboard(99L, updateData);
        });

        verify(campaignRepository, times(1)).findById(99L);
        verify(dashboardRepository, never()).save(any());
        verify(campaignRepository, never()).save(any());
    }

    @Test
    void whenUpdateCampaignDashboard_withNullDashboard_thenThrowIllegalArgumentException() {
        assertThatThrownBy(() -> campaignService.updateCampaignDashboard(1L, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Dashboard data cannot be null");

        verify(campaignRepository, never()).findById(any());
        verify(dashboardRepository, never()).save(any());
        verify(campaignRepository, never()).save(any());
    }

    @Test
    void whenGetWorkflowByCampaignId_withValidCampaignId_thenReturnWorkflow() {
        Workflow workflow = new Workflow();
        workflow.setId(200L);
        workflow.setName("Sales Workflow");

        campaign1.setWorkflow(workflow);

        Workflow result = campaignService.getWorkflowByCampaignId(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(200L);
        assertThat(result.getName()).isEqualTo("Sales Workflow");
        verify(campaignRepository, times(1)).findById(1L);
    }

    @Test
    void whenGetWorkflowByCampaignId_withInvalidCampaignId_thenReturnNull() {
        Workflow result = campaignService.getWorkflowByCampaignId(99L);

        assertThat(result).isNull();
        verify(campaignRepository, times(1)).findById(99L);
    }

    @Test
    void whenGetWorkflowByCampaignId_withCampaignWithoutWorkflow_thenReturnNull() {
        campaign2.setWorkflow(null);

        Workflow result = campaignService.getWorkflowByCampaignId(2L);

        assertThat(result).isNull();
        verify(campaignRepository, times(1)).findById(2L);
    }
}
