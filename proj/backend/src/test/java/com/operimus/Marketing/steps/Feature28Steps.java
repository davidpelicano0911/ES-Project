package com.operimus.Marketing.steps;

import com.operimus.Marketing.SharedDriver;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.assertj.core.api.Assertions.assertThat;

public class Feature28Steps {
    private final SharedDriver shared = SharedDriver.getInstance();
    private final WebDriver driver = shared.getDriver();
    private final WebDriverWait wait = shared.getWait();

    @Given("the Content Marketer is on the form builder page")
    public void the_content_marketer_is_on_the_form_builder_page() {

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
        
        WebElement createButton = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//button[contains(., 'Create Template')]")
        ));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView(true);", createButton);
        js.executeScript("arguments[0].click();", createButton);

        wait.until(ExpectedConditions.urlContains("/app/forms/builder"));
        assertThat(driver.getCurrentUrl()).contains("/app/forms/builder");
    }

    @When("they fill in the form name as {string} and the description as {string}")
    public void they_fill_in_the_form_name_as_and_the_description_as(String name, String description) {
        WebElement nameInput = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//input[@placeholder='Form name']")
        ));
        nameInput.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.DELETE);
        nameInput.clear();
        nameInput.sendKeys(name);

        WebElement descriptionInput = driver.findElement(
            By.xpath("//input[@placeholder='Form description']")
        );
        descriptionInput.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.DELETE);
        descriptionInput.clear();
        descriptionInput.sendKeys(description);
    }

    @When("they click on {string}")
    public void they_click_on(String buttonText) {
        WebElement mainButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(., '" + buttonText + "')]")
        ));
        
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", mainButton);

        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h2[contains(text(), 'Confirm Save')]")
        ));

        WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Yes, Save')]")
        ));

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmButton);
    }

    @Then("the form is saved successfully")
    public void the_form_is_saved_successfully() {
        String expectedMessage = "Form saved successfully!";

        WebElement successToast = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(., '" + expectedMessage + "')]")
        ));
        
        assertThat(successToast.getText()).contains(expectedMessage);
    }
}