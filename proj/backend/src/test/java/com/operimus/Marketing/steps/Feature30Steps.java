package com.operimus.Marketing.steps;

import com.operimus.Marketing.SharedDriver;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.assertj.core.api.Assertions.assertThat;
import java.time.Duration;

public class Feature30Steps {
    private final SharedDriver shared = SharedDriver.getInstance();
    private final WebDriver driver = shared.getDriver();
    private final WebDriverWait wait = shared.getWait();

    @Given("a form exists in the library with name {string}")
    public void a_form_exists_in_the_library_with_name(String formName) {
        try {
            WebElement formsLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(.,'Forms')]")
            ));
            formsLink.click();
            
        } catch (Exception e) {
            if (!driver.getCurrentUrl().contains("/app/forms")) {
                driver.get("http://localhost:3000/app/forms");
            }
        }

        WebElement card = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class, 'bg-white')]//h3[normalize-space()='" + formName + "']/ancestor::div[contains(@class, 'bg-white')]")
        ));

        assertThat(card.isDisplayed()).isTrue();
    }

    @When("the Content Marketer chooses to delete it")
    public void the_content_marketer_chooses_to_delete_it() {
        WebElement card = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class, 'bg-white')]//h3[normalize-space()='Updated Registration Form']/ancestor::div[contains(@class, 'bg-white')]")
        ));

        WebElement deleteBtn = card.findElement(By.xpath(".//button[@title='Delete Form Template']"));

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", deleteBtn);
        wait.until(ExpectedConditions.elementToBeClickable(deleteBtn)).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class, 'fixed') and contains(@class, 'inset-0')]")
        ));
    }

     
    @Then("the system confirms the action before removal")
    public void the_system_confirms_the_action_before_removal() {
        WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Yes, Delete') or contains(text(), 'Delete')]")
        ));

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmBtn);
    }

    @Then("shows a success message once removed")
    public void shows_a_success_message_once_removed() {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement success = shortWait.until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(), 'deleted') or contains(text(), 'removed') or contains(text(), 'Form deleted')]")
                )
            );
            assertThat(success.isDisplayed()).isTrue();
    }
}