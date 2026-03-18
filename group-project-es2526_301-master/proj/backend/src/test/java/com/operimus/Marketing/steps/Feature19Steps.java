package com.operimus.Marketing.steps;

import com.operimus.Marketing.SharedDriver;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.assertj.core.api.Assertions.assertThat;
import java.lang.Thread;

public class Feature19Steps {

    private final SharedDriver shared = SharedDriver.getInstance();
    private final WebDriver driver = shared.getDriver();
    private final WebDriverWait wait = shared.getWait();

    @When("the content creator navigates to the Email Templates section")
    public void the_content_creator_navigates_to_the_email_templates_section() {
        WebDriverWait longWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(30));
        
        longWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//button[contains(.,'Content')]")));

        WebElement contentButton = driver.findElement(
                By.xpath("//button[contains(.,'Content')]"));

        try {
            if (driver.findElements(By.xpath("//a[contains(.,'Email Templates')]")).isEmpty()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", contentButton);
                Thread.sleep(500);
            }
        } catch (Exception ignored) {}

        WebElement emailTemplatesLink = longWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(.,'Email Templates')]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", emailTemplatesLink);
        emailTemplatesLink.click();
        
        // Wait for navigation to complete
        longWait.until(ExpectedConditions.urlContains("/app/email-templates"));
    }

    @Then("the Email Templates page should be displayed")
    public void the_email_templates_page_should_be_displayed() {
        WebDriverWait longWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(30));
        longWait.until(ExpectedConditions.urlContains("/app/email-templates"));
        WebElement title = longWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(text(),'Email Templates')]")));
        assertThat(title.isDisplayed()).isTrue();
    }

    @When("the content creator clicks the Create Email Template button")
    public void the_content_creator_clicks_the_create_template_button() {
        WebElement createButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(.,'Create Email Template')]")));
        createButton.click();
    }

    @Then("the Create Email Template page should open")
    public void the_create_email_template_page_should_open() {
        wait.until(ExpectedConditions.urlContains("/app/email-templates/create"));
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(text(),'Create Email Template') or contains(text(),'Edit Email Template')]")));
        assertThat(header.isDisplayed()).isTrue();
    }

    @And("the content creator fills in the template name, description, and subject")
    public void the_content_creator_fills_in_the_template_fields() {
        WebElement nameInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[placeholder='Welcome Email Template']")));
        WebElement descInput = driver.findElement(By.cssSelector("textarea[placeholder*='Briefly describe']"));
        WebElement subjectInput = driver.findElement(By.cssSelector("input[placeholder*='Welcome to']"));

        nameInput.sendKeys("Test Email Template");
        descInput.sendKeys("Automated test email template description");
        subjectInput.sendKeys("Welcome to Operimus Marketing!");
    }

    @And("the content creator previews the template")
    public void the_content_creator_previews_the_template() throws InterruptedException {
        WebDriverWait longWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(30));

        WebElement previewButton = longWait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//button[contains(.,'Preview Template')]")));

        longWait.until(d -> previewButton.isEnabled());

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", previewButton);
        Thread.sleep(500);
        previewButton.click();

        WebElement modalHeader = longWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h2[contains(text(),'Email Template Preview')]")));
        assertThat(modalHeader.isDisplayed()).isTrue();
    }


    @And("the content creator saves the new email template")
    public void the_content_creator_saves_the_new_email_template() {
        try {
            WebElement modalHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h2[contains(text(),'Email Template Preview')]")));
            assertThat(modalHeader.isDisplayed()).isTrue();

            WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(.,'Save Template')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", saveButton);
            Thread.sleep(1000);
            saveButton.click();
        } catch (TimeoutException e) {
            throw new RuntimeException("Timed out waiting for Save Template modal or button.", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to click Save Template: " + e.getMessage());
        }
    }

    @Then("a success message should appear confirming the template creation")
    public void a_success_message_should_appear_confirming_the_template_creation() {
        String expectedMessage = "Template created successfully!";

        WebElement successToast = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(., 'Template created successfully!')]")
        ));
        
        assertThat(successToast.getText()).contains(expectedMessage);
    }
}
