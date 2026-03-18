package com.operimus.Marketing.steps;

import com.operimus.Marketing.SharedDriver;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.assertj.core.api.Assertions.assertThat;

public class Feature06Steps {

    private final SharedDriver shared = SharedDriver.getInstance();
    private final WebDriver driver = shared.getDriver();
    private final WebDriverWait wait = shared.getWait();

    @And("the manager selects the campaign named {string}")
    public void the_manager_selects_the_campaign_named(String campaignName) {
        String campaignCardXpath = String.format(
            "//h3[normalize-space(text())='%s']/ancestor::div[contains(@class,'grid-cols-3') or contains(@class, 'CampaignCard') or contains(@class,'bg-white')]",
            campaignName
        );
        WebElement campaignCard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(campaignCardXpath)));
        System.out.println("Campaign card found: " + campaignCard);

        String viewDetailsButtonXpath = String.format(
            "//h3[normalize-space(text())='%s']/ancestor::div[contains(@class,'bg-white')]//button[contains(.,'View Details')]",
            campaignName
        );
        WebElement campaignButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(viewDetailsButtonXpath)));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", campaignButton);
        wait.until(ExpectedConditions.elementToBeClickable(campaignButton)).click();
    }

    @Then("the manager should be redirected to the Campaign Details page")
    public void the_manager_should_be_redirected_to_the_campaign_details_page() {
        wait.until(ExpectedConditions.urlContains("/app/campaigns/"));
        assertThat(driver.getCurrentUrl()).contains("/app/campaigns/");
    }

    @And("the manager clicks the Create New Workflow button")
    public void the_manager_clicks_the_create_new_workflow_button() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h1[contains(@class,'text-xl') and contains(text(),'Winter Clearance')]")));

        WebElement createWorkflowBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(.,'Create New Workflow')]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", createWorkflowBtn);
        createWorkflowBtn.click();
    }

    @Then("the manager should be redirected to the Create Workflow page")
    public void the_manager_should_be_redirected_to_the_create_workflow_page() {
        wait.until(ExpectedConditions.urlContains("/app/workflows/create"));
        assertThat(driver.getCurrentUrl()).contains("/app/workflows/create");
    }

    @And("the manager fills in the workflow details with a name and description")
    public void the_manager_fills_in_the_workflow_details_with_a_name_and_description() {
        WebElement nameInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder='Enter workflow name']")));
        WebElement descInput = driver.findElement(By.cssSelector("textarea[placeholder*='Describe']"));
        nameInput.sendKeys("Workflow Test");
        descInput.sendKeys("Automated workflow test description");
    }

    @And("the manager confirms the workflow creation")
    public void the_manager_confirms_the_workflow_creation() {
        WebElement createBtn = driver.findElement(By.cssSelector("button.bg-\\[\\#2563EB\\]"));
        createBtn.click();
        WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(.,'Yes, create')]")));
        confirmBtn.click();
    }

    @Then("the workflow should be successfully created")
    public void the_workflow_should_be_successfully_created() {
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'Workflow created')]")));
        assertThat(successMessage.isDisplayed()).isTrue();
    }

    @And("the manager should be redirected back to the Campaign Details page")
    public void the_manager_should_be_redirected_back_to_the_campaign_details_page() {
        wait.withTimeout(java.time.Duration.ofSeconds(30))
            .pollingEvery(java.time.Duration.ofMillis(500))
            .until(driver -> driver.getCurrentUrl().matches(".*/app/campaigns/\\d+$"));

        assertThat(driver.getCurrentUrl()).matches(".*/app/campaigns/\\d+$");
    }

    @And("the manager clicks the View Workflow button")
    public void the_manager_clicks_the_view_workflow_button() {
        By viewBtnLocator = By.xpath("//button[contains(.,'View Workflow')]");
        
        WebElement viewBtn = new WebDriverWait(driver, java.time.Duration.ofSeconds(20))
            .until(ExpectedConditions.presenceOfElementLocated(viewBtnLocator));

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", viewBtn);
        
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", viewBtn);
    }

    @Then("the manager should be redirected to the Workflow Builder page")
    public void the_manager_should_be_redirected_to_the_workflow_builder_page() {
        wait.until(ExpectedConditions.urlContains("/app/workflows/"));
        assertThat(driver.getCurrentUrl()).contains("/app/workflows/");
    }

    @And("the manager adds nodes to the workflow")
    public void the_manager_adds_nodes_to_the_workflow() {
        WebDriverWait longWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(40));
        longWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".react-flow__pane")));
        longWait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".bg-white.border-t.border-gray-200")));

        // Click Triggers tab using JavaScript
        WebElement triggersTab = longWait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(.,'Triggers')]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", triggersTab);

        // Wait for tab content to render
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Triggers tab clicked, looking for Form block...");
        
        // Find the Form block - it's a button element, not a div
        WebElement formBlock = longWait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[.//p[contains(text(),'Form')]]")));
        
        System.out.println("Found Form block button");
        
        // Scroll into view and wait
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", formBlock);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Click the button
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", formBlock);
        
        System.out.println("Clicked Form block, waiting for new node...");
        
        // Wait for the Form node to be added (should have 2 nodes: START + FORM)
        longWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
            By.cssSelector(".react-flow__node"), 1));
        
        int nodeCount = driver.findElements(By.cssSelector(".react-flow__node")).size();
        System.out.println("Node count after adding Form: " + nodeCount);
    }

    @And("the manager connects nodes in the workflow")
    public void the_manager_connects_nodes_in_the_workflow() {
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.cssSelector(".react-flow__node"), 1));
        var nodes = driver.findElements(By.cssSelector(".react-flow__node"));
        assertThat(nodes.size()).isGreaterThanOrEqualTo(2);
    }

    @And("the manager clicks the Save button")
    public void the_manager_clicks_the_save_button() {
        WebDriverWait shortWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(30));
        WebElement saveBtn = shortWait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("button.flex.items-center.gap-2.bg-\\[\\#2563EB\\]")));

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", saveBtn);
        wait.until(ExpectedConditions.elementToBeClickable(saveBtn));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveBtn);

    }

    @Then("the workflow should be saved successfully and visible in the builder")
    public void the_workflow_should_be_saved_successfully_and_visible_in_the_builder() {
        WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'Workflow saved')]")));
        assertThat(successMsg.isDisplayed()).isTrue();
    }
}
