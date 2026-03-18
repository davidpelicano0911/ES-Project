package com.operimus.Marketing.steps;

import com.operimus.Marketing.SharedDriver;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.assertj.core.api.Assertions.assertThat;

public class Feature10Steps {

    private final SharedDriver shared = SharedDriver.getInstance();
    private final WebDriver driver = shared.getDriver();
    private final WebDriverWait wait = shared.getWait();

    @When("the manager adds a Form block to the workflow")
    public void the_manager_adds_a_landing_page_block_to_the_workflow() {
        WebDriverWait longWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(40));
        longWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".react-flow__pane")));
        longWait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".bg-white.border-t.border-gray-200")));
        WebElement triggersTab = longWait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(.,'Triggers')]")));
        triggersTab.click();
        WebElement formBlock = longWait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//p[contains(text(),'Form')]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", formBlock);
        formBlock.click();
        longWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
            By.cssSelector(".react-flow__node"), 1));
    }

    @And("the manager saves the edited workflow")
    public void the_manager_saves_the_edited_workflow() {
        WebDriverWait shortWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(20));
        WebElement saveBtn = shortWait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("button.flex.items-center.gap-2.bg-\\[\\#2563EB\\]")));

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", saveBtn);
        wait.until(ExpectedConditions.elementToBeClickable(saveBtn));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveBtn);
    }

    @Then("the workflow should be updated successfully and visible in the builder")
    public void the_workflow_should_be_updated_successfully_and_visible_in_the_builder() {
        WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(text(),'Workflow saved')]")));
        assertThat(successMsg.isDisplayed()).isTrue();
    }
}
