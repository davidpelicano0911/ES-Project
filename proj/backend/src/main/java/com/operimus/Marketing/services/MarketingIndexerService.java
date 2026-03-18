package com.operimus.Marketing.services;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.operimus.Marketing.entities.EmailTemplate;
import com.operimus.Marketing.entities.LandingPage;
import com.operimus.Marketing.entities.Campaign;
import com.operimus.Marketing.entities.Dashboard;
import com.operimus.Marketing.entities.FormTemplate;
import com.operimus.Marketing.entities.Lead;
import com.operimus.Marketing.entities.Post;
import com.operimus.Marketing.entities.Segments;
import com.operimus.Marketing.entities.Workflow;
import com.operimus.Marketing.entities.WorkflowTemplate;

import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.Filter.Expression;
import java.util.Arrays;
import org.springframework.ai.document.Document;



@Service
public class MarketingIndexerService {

    private final VectorStore vectorStore;

    public MarketingIndexerService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Transactional
    public void indexEmailTemplate(EmailTemplate template) {
        String content = template.getSubject() + "\n\n" + stripHtml(template.getBody());

        deleteFromIndex(template.getId(), "EMAIL_TEMPLATE");

        Document doc = new Document(
            content,
            Map.of(
                "type", "EMAIL_TEMPLATE",
                "id", template.getId().toString(),
                "name", template.getName()
            )
        );
        vectorStore.add(List.of(doc));
    }

    @Transactional
    public void indexLandingPage(LandingPage page) {
        String content = page.getName() + "\n\n" + stripHtml(page.getBody());

        deleteFromIndex(page.getId(), "LANDING_PAGE");

        Document doc = new Document(
            content,
            Map.of(
                "type", "LANDING_PAGE",
                "id", page.getId().toString(),
                "name", page.getName()
            )
        );
        vectorStore.add(List.of(doc));
    }

    @Transactional
    public void indexCampaign(Campaign campaign) {
        StringBuilder sb = new StringBuilder();
        sb.append("Campaign: ").append(campaign.getName()).append("\n");
        if (campaign.getDescription() != null) {
            sb.append(campaign.getDescription()).append("\n");
        }

        deleteFromIndex(campaign.getId(), "CAMPAIGN");

        Document doc = new Document(
            sb.toString(),
            Map.of(
                "type", "CAMPAIGN",
                "id", campaign.getId().toString(),
                "status", String.valueOf(campaign.getStatus())
            )
        );
        vectorStore.add(List.of(doc));
    }

    @Transactional
    public void indexDashboard(Dashboard dashboard) {
        String content = dashboard.getTitle() + "\n\n" + dashboard.getCampaign().getName();

        deleteFromIndex(dashboard.getId(), "DASHBOARD");

        Document doc = new Document(
            content,
            Map.of(
                "type", "DASHBOARD",
                "id", dashboard.getId().toString(),
                "title", dashboard.getTitle()
            )
        );
        vectorStore.add(List.of(doc));
    }

    @Transactional
    public void indexFormTemplate(FormTemplate form) {
        String content = form.getName() + "\n\n" + form.getDescription() + "\n\n" + form.getFormJson() + "\n\n" + "Creation Date: " + form.getCreatedAt() + "\nLast Updated: " + form.getUpdatedAt() + "\nPublished: " + form.getIsPublished();
        
        deleteFromIndex(form.getId(), "FORM_TEMPLATE");

        Document doc = new Document(
            content,
            Map.of(
                "type", "FORM_TEMPLATE",
                "id", form.getId().toString(),
                "name", form.getName()
            )
        );
        vectorStore.add(List.of(doc));
    }

    @Transactional
    public void indexLead(Lead lead) {
        String content = lead.getFirstName() + " " + lead.getLastName() + "\n\n" + lead.getEmail() + "\n" + lead.getPhoneNumber() + "\n\n" + "Country: " + lead.getCountry() + "\nSubscribed: " + lead.getIsSubscribed() + "\nCreated At: " + lead.getCreatedAt();
        
        deleteFromIndex(lead.getId(), "LEAD");

        Document doc = new Document(
            content,
            Map.of(
                "type", "LEAD",
                "id", lead.getId().toString(),
                "email", lead.getEmail()
            )
        );
        vectorStore.add(List.of(doc));
    }

    @Transactional
    public void indexPost(Post post) {
        String content = post.getName() + "\n\n" + post.getDescription() + "\n\nScheduled Date: " + post.getScheduled_date() + "\nFile Path: " + post.getFile_path();
        
        deleteFromIndex(post.getId(), "POST");
        
        Document doc = new Document(
            content,
            Map.of(
                "type", "POST",
                "id", post.getId().toString(),
                "title", post.getName()
            )
        );
        vectorStore.add(List.of(doc));
    }

    @Transactional
    public void indexSegment(Segments segment) {
        String campaignNames = segment.getCampaigns().stream()
            .map(c -> c.getName())
            .collect(Collectors.joining("\n")); 

        String content = segment.getName() + "\n\nCampaigns:\n" + campaignNames;

        deleteFromIndex(segment.getId(), "SEGMENT");

        Document doc = new Document(
            content,
            Map.of(
                "type", "SEGMENT",
                "id", segment.getId().toString(),
                "name", segment.getName()
            )
        );
        vectorStore.add(List.of(doc));
    }

    @Transactional
    public void indexWorkflow(Workflow workflow) {
        String content = workflow.getName() + "\n\n" + workflow.getDescription() + "\n\nCreated At: " + workflow.getCreatedAt() + "\nModified At: " + workflow.getModifiedAt() + "\nCampaign: " + (workflow.getCampaign() != null ? workflow.getCampaign().getName() : "None");
        
        deleteFromIndex(workflow.getId(), "WORKFLOW");
        
        Document doc = new Document(
            content,
            Map.of(
                "type", "WORKFLOW",
                "id", workflow.getId().toString(),
                "name", workflow.getName()
            )
        );
        vectorStore.add(List.of(doc));
    }

    @Transactional
    public void indexWorkflowTemplate(WorkflowTemplate template) {
        String content = template.getName() + "\n\n" + template.getDescription() + "\n\nCreated At: " + template.getCreatedAt() + "\nData: " + template.getTemplateData();
        
        deleteFromIndex(template.getId(), "WORKFLOW_TEMPLATE");
        
        Document doc = new Document(
            content,
            Map.of(
                "type", "WORKFLOW_TEMPLATE",
                "id", template.getId().toString(),
                "name", template.getName()
            )
        );
        vectorStore.add(List.of(doc));
    }

    @Transactional
    public void deleteFromIndex(Long templateId, String type) {
        Filter.Expression typeFilter = new Filter.Expression(
            Filter.ExpressionType.EQ,
            new Filter.Key("type"),
            new Filter.Value(type)
        );

        Filter.Expression idFilter = new Filter.Expression(
            Filter.ExpressionType.EQ,
            new Filter.Key("id"),
            new Filter.Value(templateId.toString())
        );

        Filter.Expression filteredExpression = new Filter.Expression(
            Filter.ExpressionType.AND,
            typeFilter,
            idFilter
        );
        
        vectorStore.delete(filteredExpression);
    }

    private String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", " ");
    }
}
