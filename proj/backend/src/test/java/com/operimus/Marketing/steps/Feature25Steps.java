package com.operimus.Marketing.steps;

import com.operimus.Marketing.SharedDriver;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.assertj.core.api.Assertions.assertThat;

public class Feature25Steps {

    private final SharedDriver shared = SharedDriver.getInstance();
    private final WebDriver driver = shared.getDriver();

    @When("the content marketer clicks the Create Landing Page button")
    public void the_content_marketer_clicks_the_create_landing_page_button() {
        WebDriverWait longWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(20));
        WebElement button = longWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(., 'Create Landing Page')]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", button);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
    }

    @Then("the Create Landing Page form should open")
    public void the_create_landing_page_form_should_open() {
        WebDriverWait longWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(30));
        WebElement header = longWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(text(), 'Create Landing Page') or contains(text(),'Edit Landing Page')]")));
        assertThat(header.isDisplayed()).isTrue();
    }

    @When("the content marketer fills in the landing page name and description")
    public void the_content_marketer_fills_in_the_landing_page_name_and_description() {
        WebDriverWait longWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(20));
        WebElement nameInput = longWait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//input[@placeholder='Example: Summer Campaign Landing Page']")));
        WebElement descInput = driver.findElement(
                By.xpath("//textarea[@placeholder='Briefly describe the purpose of this landing page...']"));

        nameInput.sendKeys("Spring Collection Launch");
        descInput.sendKeys("Promotional page showcasing our 2025 spring fashion collection.");
    }

    @When("the content marketer clicks the Preview Landing Page button")
    public void the_content_marketer_clicks_the_preview_landing_page_button() throws InterruptedException {
        WebDriverWait longWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(20));

        WebElement previewButton = longWait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//button[contains(., 'Preview Landing Page')]")));

        longWait.until(d -> previewButton.isEnabled());

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", previewButton);
        Thread.sleep(800);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", previewButton);
    }

    @Then("a preview modal should appear showing the landing page preview")
    public void a_preview_modal_should_appear_showing_the_landing_page_preview() {
        System.out.println("Waiting for preview modal to appear...");
        WebDriverWait previewWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(40));

        // Espera tolerante: procura por h2 OU h3 com esse texto
        WebElement modalTitle = previewWait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[self::h2 or self::h3][contains(normalize-space(.), 'Landing Page Preview')]")
        ));
        assertThat(modalTitle.isDisplayed())
            .as("Modal title 'Landing Page Preview' should be visible")
            .isTrue();

        WebElement saveButton = previewWait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(., 'Save Landing Page')]")));
        assertThat(saveButton.isDisplayed()).isTrue();
    }


    @When("the content marketer saves the new landing page")
    public void the_content_marketer_saves_the_new_landing_page() throws InterruptedException {
        WebDriverWait longWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(20));
        WebElement saveButton = longWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(., 'Save Landing Page')]")));

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", saveButton);
        Thread.sleep(1000);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveButton);
    }

    @Then("a success message should confirm the landing page creation")
    public void a_success_message_should_confirm_the_landing_page_creation() {
        WebDriverWait longWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(10));
        WebElement successMsg = longWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'Landing page') and (contains(text(), 'created') or contains(text(), 'successfully'))]")));
        assertThat(successMsg.isDisplayed()).isTrue();
    }

}
