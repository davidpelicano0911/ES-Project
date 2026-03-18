package com.operimus.Marketing.tool;


import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.operimus.Marketing.services.EmailTemplateService;
import com.operimus.Marketing.entities.EmailTemplate;
import com.operimus.Marketing.services.PostService;
import com.operimus.Marketing.dto.PostDTO;
import com.operimus.Marketing.services.MarketingIndexerService;
import com.operimus.Marketing.entities.Post;





@Component("marketingTools")
public class MarketingTools {

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Autowired
    private PostService postService;

    @Autowired 
    private MarketingIndexerService marketingIndexerService;
    

   @Tool(
        name = "create_email_template",

        description = """
            Creates a new email marketing template in the system.
            
            CRITICAL INSTRUCTIONS:
            If the user does not provide specific 'subject' or 'body', YOU MUST GENERATE THEM AUTOMATICALLY.
            
            FOR THE 'BODY' (HTML), ACT AS A SENIOR UI/UX DESIGNER:
            - Generate a modern, visually appealing HTML5 layout.
            - Use inline CSS for styling (background-colors, fonts, padding, borders).
            - Include a centered container structure.
            - Add a distinct, styled 'Call-To-Action' (CTA) button.
            - Ensure the tone matches the marketing intent.
            """
    )
    public String createEmailTemplate(EmailTemplate template) {
        
        System.out.println("Executing Tool: Creating Template '" + template.getName() + "'");
        
        try{
            // Try to save
            EmailTemplate savedTemplate = emailTemplateService.createTemplate(template);
            marketingIndexerService.indexEmailTemplate(savedTemplate);
        } catch (Exception e) {
            System.err.println("Tool Error: " + e.getMessage());
            return "Error: Failed to create template. " + e.getMessage();
        }
        return "Success: Template '" + template.getName() + "' created with subject '" + template.getSubject() + "'.";
    }

    @Tool(
        name = "delete_email_template",
        description = "Deletes an email marketing template by its ID."
    )
    public String deleteEmailTemplate(Long templateId) {
        System.out.println("Executing Tool: Deleting Template ID " + templateId);
        try {
            emailTemplateService.deleteTemplate(templateId);
        } catch (Exception e) {
            return "Error: Template with ID " + templateId + " not found.";
        }
        marketingIndexerService.deleteFromIndex(templateId, "EMAIL_TEMPLATE");
        return "Success: Template with ID " + templateId + " deleted.";
    }

    @Tool(
        name = "edit_email_template",
        description = """ 
            Edits an existing email marketing template.
            CRITICAL INSTRUCTIONS:
            User just needs to provide the FIELDS THEY WANT TO UPDATE.

            FOR THE 'BODY' (HTML), ACT AS A SENIOR UI/UX DESIGNER:
            - Generate a modern, visually appealing HTML5 layout.
            - Use inline CSS for styling (background-colors, fonts, padding, borders).
            - Include a centered container structure.
            - Add a distinct, styled 'Call-To-Action' (CTA) button.
            - Ensure the tone matches the marketing intent.
        """
    )
    public String editEmailTemplate(EmailTemplate template) {
        System.out.println("Executing Tool: Editing Template ID " + template.getId());
        
        try {
            // Attempt the update
            emailTemplateService.updateTemplate(template.getId(), template);
            marketingIndexerService.indexEmailTemplate(template);
            
            return "Success: Template ID " + template.getId() + " edited successfully.";
            
        } catch (Exception e) {
            // CATCH the error (like 'value too long') and return it as text
            // This prevents the "Failed to parse JSON" crash
            System.err.println("Tool Error: " + e.getMessage());
            return "ERROR: Failed to update template. Database Error: " + e.getMessage();
        }
    }


    @Tool(
        name = "create_social_media_post",
        description = """
            Creates a new social media post.
            CRITICAL INSTRUCTIONS:
            - If a post with the SAME NAME already exists, RETURN AN ERROR MESSAGE indicating the name
                is taken and ASK THE USER TO PROVIDE A NEW UNIQUE NAME.
            - The only platform available is FACEBOOK, if the user provides any other platform, RETURN AN ERROR MESSAGE
                indicating the platform is unsupported.
            """
    )
    public String createSocialMediaPost(PostDTO postDTO) {
        System.out.println("Executing Tool: Creating Post '" + postDTO.getName() + "'");
        
        try {
            // Try to save
            Post post = postService.createPost(postDTO);
            marketingIndexerService.indexPost(post);
            return "Success: Social media post for '" + postDTO.getName() + "' created.";
            
        } catch (IllegalArgumentException e) {
            System.err.println("Tool Error: " + e.getMessage());
            
            return "ERROR: Failed to create post. " + e.getMessage() + 
                   ". Please generate a NEW, UNIQUE name and try calling this tool again.";
        }
    }

    @Tool(
        name = "delete_social_media_post",
        description = "Deletes a social media post by its ID."
    )
    public String deleteSocialMediaPost(Long postId) {
        System.out.println("Executing Tool: Deleting Post ID " + postId);
        boolean deleted = postService.deletePost(postId);
        if (deleted) {
            marketingIndexerService.deleteFromIndex(postId, "POST");
            return "Success: Post with ID " + postId + " deleted.";
        } else {
            return "Error: Post with ID " + postId + " not found.";
        }
    }
    
        
}
