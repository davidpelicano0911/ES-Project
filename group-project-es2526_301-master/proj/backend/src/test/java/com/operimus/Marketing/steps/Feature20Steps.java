package com.operimus.Marketing.steps;

import com.operimus.Marketing.SharedDriver;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.assertj.core.api.Assertions.assertThat;

public class Feature20Steps {

    private final SharedDriver shared = SharedDriver.getInstance();
    private final WebDriver driver = shared.getDriver();
    private final WebDriverWait wait = shared.getWait();

    @And("the content creator opens the template named {string} for editing")
    public void the_content_creator_opens_the_template_named_for_editing(String templateName) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".grid")));

        WebElement title = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//h3[contains(text(),'" + templateName + "')]")));
        
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", title);

        WebElement editBtn = title.findElement(By.xpath("./ancestor::div[contains(@class, 'bg-white')]//button[contains(.,'Edit')]"));
        
        wait.until(ExpectedConditions.elementToBeClickable(editBtn)).click();

        wait.until(ExpectedConditions.urlContains("/app/email-templates/edit"));
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(text(),'Edit Email Template') or contains(text(),'Create Email Template')]")));
        assertThat(header.isDisplayed()).isTrue();
    }

    @And("the content creator edits the name and description to {string}")
    public void the_content_creator_edits_the_name_and_description_to(String newName) {
        WebElement nameInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[placeholder='Welcome Email Template']")));
        WebElement descInput = driver.findElement(
                By.cssSelector("textarea[placeholder*='Briefly describe']"));

        nameInput.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.DELETE); 
        nameInput.clear(); 
        
        descInput.clear();

        nameInput.sendKeys(newName);
        descInput.sendKeys("Updated description for automated edit test.");
    }

    @And("the content creator saves the updated email template")
    public void the_content_creator_saves_the_updated_email_template() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h2[contains(text(),'Email Template Preview')]")));

            WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(.,'Save Template')]")));
            
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", saveBtn);
            Thread.sleep(1000);
            saveBtn.click();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            throw new RuntimeException("Error clicking save button: " + e.getMessage());
        }
    }

    @Then("a success message should appear confirming the template update")
    public void a_success_message_should_appear_confirming_the_template_update() {
        String expectedMessage = "Template updated successfully!";

        WebElement successToast = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(., '" + expectedMessage + "')]")
        ));
        
        assertThat(successToast.getText()).contains(expectedMessage);
    }
}