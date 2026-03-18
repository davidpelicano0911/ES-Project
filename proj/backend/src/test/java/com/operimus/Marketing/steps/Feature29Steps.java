package com.operimus.Marketing.steps;

import com.operimus.Marketing.SharedDriver;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.assertj.core.api.Assertions.assertThat;
import java.time.Duration;

public class Feature29Steps {
    private final SharedDriver shared = SharedDriver.getInstance();
    private final WebDriver driver = shared.getDriver();
    private final WebDriverWait wait = shared.getWait();

    @Given("a saved form exists with name {string} and description {string}")
    public void a_saved_form_exists_with_name_and_description(String templateName, String expectedDesc) {
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

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".grid")));

        WebElement title = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//h3[contains(text(),'" + templateName + "')]")));
        
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", title);

        WebElement cardContainer = title.findElement(By.xpath("./ancestor::div[contains(@class, 'bg-white')]"));

        WebElement desc = cardContainer.findElement(By.xpath(".//p"));
        assertThat(desc.getText().trim()).contains(expectedDesc);

        WebElement editBtn = cardContainer.findElement(By.xpath(".//button[contains(.,'Edit')]"));
        
        wait.until(ExpectedConditions.elementToBeClickable(editBtn)).click();

        wait.until(ExpectedConditions.urlContains("/app/forms/builder/"));
    }

    @When("the Content Marketer edits the form name to {string} and description to {string}")
    public void the_content_marketer_edits_the_form_name_to_and_description_to(String newName, String newDesc) {
        WebElement nameInput = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//input[@placeholder='Form name']")
        ));
        
        nameInput.clear();
        wait.until(ExpectedConditions.elementToBeClickable(nameInput));
        Actions actions = new Actions(driver);
        actions.click(nameInput).perform();
        for (int i = 0; i < 20; i++) {
            nameInput.sendKeys(Keys.BACK_SPACE);
        }
        nameInput.sendKeys(newName);
        

        WebElement descInput = driver.findElement(By.xpath("//input[@placeholder='Form description']"));
        descInput.clear();
        descInput.sendKeys(newDesc);
    }

    @When("they confirm saving the form")
    public void they_confirm_saving_the_form() {
        WebElement mainButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[.//text()[contains(., 'Save Form')]]")
        ));
        mainButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class, 'fixed') and contains(@class, 'inset-0')]")
        ));

        WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Yes, Save')]")
        ));

        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView(true); arguments[0].click();", confirmButton
        );
    }

    @Then("the form is saved successfully after editing")
    public void the_form_is_saved_successfully_after_editing() {
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement successMessage = shortWait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'Form saved')]")
            )
        );
        assertThat(successMessage.isDisplayed()).isTrue();
        
        wait.until(ExpectedConditions.urlContains("/app/forms"));
        
        WebDriverWait wait10 = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait10.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(), 'Form Templates')]")),
            ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@placeholder='Search form templates...']"))
        ));
    }
}