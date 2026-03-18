package com.operimus.Marketing.steps;

import com.operimus.Marketing.SharedDriver;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.assertj.core.api.Assertions.assertThat;

public class Feature22Steps {

    private final SharedDriver shared = SharedDriver.getInstance();
    private final WebDriver driver = shared.getDriver();
    private final WebDriverWait wait = shared.getWait();

    @And("the content creator deletes the email template named {string}")
    public void the_content_creator_deletes_the_email_template_named(String templateName) {
        wait.until(ExpectedConditions.urlContains("/app/email-templates"));
        WebElement templateCard = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//h3[contains(text(),'" + templateName + "')]")));

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", templateCard);
        WebElement deleteButton = templateCard.findElement(
                By.xpath("./ancestor::div[contains(@class,'rounded-2xl')]//button[@title='Delete Template']"));
        wait.until(ExpectedConditions.elementToBeClickable(deleteButton));
        deleteButton.click();
        WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(.,'Confirm') or contains(.,'Delete')]")));
        confirmButton.click();
    }

    @Then("a success message should appear confirming the template deletion")
    public void a_success_message_should_appear_confirming_the_template_deletion() {
        String expectedSnippet = "deleted successfully!";

        WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(., '" + expectedSnippet + "')]")
        ));
        
        assertThat(successMsg.getText()).contains(expectedSnippet);
    }
}
