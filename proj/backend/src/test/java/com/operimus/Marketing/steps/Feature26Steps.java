package com.operimus.Marketing.steps;

import com.operimus.Marketing.SharedDriver;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.assertj.core.api.Assertions.assertThat;
import java.time.Duration;

public class Feature26Steps {

    private final SharedDriver shared = SharedDriver.getInstance();
    private final WebDriver driver = shared.getDriver();
    private final WebDriverWait wait = shared.getWait();
    private final WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));

    @When("the content marketer selects an existing landing page to edit")
    public void selects_an_existing_landing_page_to_edit() {
        wait.until(ExpectedConditions.urlContains("/app/landing-pages"));
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".grid")));

        WebElement editButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(., 'Edit')]")
        ));
        
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", editButton);
        editButton.click();
    }

    @Then("the landing page editor should open")
    public void the_landing_page_editor_should_open() {
        wait.until(ExpectedConditions.urlMatches(".*\\/app\\/landing-pages\\/edit\\/\\d+"));
        
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h1[contains(text(),'Edit Landing Page')]")));
        assertThat(header.isDisplayed()).isTrue();
    }

    @When("the content marketer modifies the name and description")
    public void the_content_marketer_modifies_the_name_and_description() {
        WebElement nameInput = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//input[@placeholder='Example: Summer Campaign Landing Page']")));
        WebElement descInput = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//textarea[@placeholder='Briefly describe the purpose of this landing page...']")));
        
        nameInput.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.DELETE); 
        nameInput.clear();
        nameInput.sendKeys("Winter Sale 2025");

        descInput.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.DELETE);
        descInput.clear();
        descInput.sendKeys("Exclusive landing page promoting discounts for Winter 2025.");
    }

    @And("saves the edited landing page")
    public void saves_the_edited_landing_page() throws InterruptedException {
        WebElement previewButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(., 'Preview Landing Page')]")));
        
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", previewButton);
        Thread.sleep(500);
        previewButton.click();

        WebElement modalTitle = longWait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[self::h2 or self::h3][contains(normalize-space(.), 'Landing Page Preview')]")
        ));
        assertThat(modalTitle.isDisplayed()).isTrue();

        WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(., 'Save Landing Page')]")));
        
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", saveButton);
        Thread.sleep(500);
        saveButton.click();
    }

    @Then("a confirmation message should appear before overwriting")
    public void a_confirmation_message_should_appear_before_overwriting() {
        String expectedText = "Landing page updated successfully!";
        WebElement confirmMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(., '" + expectedText + "')]")
        ));
        
        assertThat(confirmMsg.getText()).contains(expectedText);
    }
}