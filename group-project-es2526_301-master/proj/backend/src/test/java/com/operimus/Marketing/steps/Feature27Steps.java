package com.operimus.Marketing.steps;

import com.operimus.Marketing.SharedDriver;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.assertj.core.api.Assertions.assertThat;

public class Feature27Steps {

    private final SharedDriver shared = SharedDriver.getInstance();
    private final WebDriver driver = shared.getDriver();
    private final WebDriverWait wait = shared.getWait();

    private String landingPageName = "Winter Sale 2025";

    @When("the content marketer selects it for deletion")
    public void the_content_marketer_selects_it_for_deletion() {
        WebElement card = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//h3[contains(text(),'" + landingPageName + "')]")));

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", card);

        WebElement deleteButton = card.findElement(
                By.xpath("./ancestor::div[contains(@class,'rounded-2xl')]//button[@title='Delete Landing Page']"));
        wait.until(ExpectedConditions.elementToBeClickable(deleteButton)).click();

    }

    @Then("the system asks for confirmation")
    public void the_system_asks_for_confirmation() {
        WebElement confirmModal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'Are you sure') or contains(text(),'Confirm')]")));
        assertThat(confirmModal.isDisplayed()).isTrue();
    }

    @And("confirms the deletion")
    public void confirms_the_deletion() {
        WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(.,'Delete') or contains(.,'Confirm')]")));
        confirmButton.click();
    }

    @Then("a success message should appear confirming the landing page deletion")
    public void a_success_message_should_appear_confirming_the_landing_page_deletion() {
        WebDriverWait longWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(20));
        WebElement successMsg = longWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'deleted')]")));
        assertThat(successMsg.isDisplayed()).isTrue();
    }
}