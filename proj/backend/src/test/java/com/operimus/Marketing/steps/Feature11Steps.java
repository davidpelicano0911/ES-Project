package com.operimus.Marketing.steps;

import com.operimus.Marketing.SharedDriver;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.assertj.core.api.Assertions.assertThat;

public class Feature11Steps {

    private final SharedDriver shared = SharedDriver.getInstance();
    private final WebDriver driver = shared.getDriver();
    private final WebDriverWait wait = shared.getWait();

    @When("the manager clicks the Create Campaign button")
    public void the_manager_clicks_the_create_campaign_button() {
        WebElement createButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(., 'Create Campaign')]"))
        );
        createButton.click();

        wait.until(ExpectedConditions.urlContains("/app/campaigns/create"));
        assertThat(driver.getCurrentUrl()).contains("/app/campaigns/create");
    }

    @And("the manager fills in the campaign details with a name, description, and deadline")
    public void the_manager_fills_in_the_campaign_details_with_a_name_description_and_deadline() {
        WebElement nameInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='text']")));
        WebElement descriptionInput = driver.findElement(By.cssSelector("textarea"));
        WebElement dateInput = driver.findElement(By.cssSelector("input[type='date']"));

        nameInput.sendKeys("Test Campaign");
        descriptionInput.sendKeys("This is an automated test campaign.");
        dateInput.sendKeys("2026-12-11");

        try {
            WebElement firstSegment = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='checkbox']"))
            );
            firstSegment.click();
        } catch (Exception e) {
            System.out.println("No segment found to select. Ignoring if not required.");
        }
    }

    @And("the manager saves the new campaign")
    public void the_manager_saves_the_new_campaign() {
        WebElement addButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(., 'Add Campaign')]"))
        );
        addButton.click();

        try {
            WebElement confirmButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(., 'Yes, create')]"))
            );
            confirmButton.click();
        } catch (Exception e) {
            System.out.println("No confirmation modal appeared (possibly created directly).");
        }
        wait.until(ExpectedConditions.urlContains("/app/campaigns"));
    }

    @Then("the campaign should be created and visible in the campaign list")
    public void the_campaign_should_be_created_and_visible_in_the_campaign_list() {
        wait.until(ExpectedConditions.urlContains("/app/campaigns"));
        WebElement campaignCard = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), 'Test Campaign')]"))
        );
        assertThat(campaignCard.isDisplayed()).isTrue();
    }

    @When("the manager clicks the delete button for the created campaign")
    public void the_manager_clicks_the_delete_button_for_the_created_campaign() {
        WebElement campaignCard = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[contains(@class, 'bg-white')][.//*[contains(text(), 'Test Campaign')]]")
        ));

        WebElement deleteButton = campaignCard.findElement(
            By.xpath(".//button[@title='Delete Campaign']")
        );

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", deleteButton);
        wait.until(ExpectedConditions.elementToBeClickable(deleteButton)).click();
    }


    @And("the manager confirms the campaign deletion")
    public void the_manager_confirms_the_campaign_deletion() {
        WebElement confirmButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Delete Campaign')]"))
        );
        confirmButton.click();

        wait.until(ExpectedConditions.urlContains("/app/campaigns"));
    }

    @Given("a campaign named {string} exists in the campaign list")
    public void a_campaign_named_exists_in_the_campaign_list(String campaignName) {
        WebElement campaignCard = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), '" + campaignName + "')]"))
        );
        assertThat(campaignCard.isDisplayed()).isTrue();
    }

    @Then("a success message should be displayed indicating the campaign was deleted")
    public void a_success_message_should_be_displayed_indicating_the_campaign_was_deleted() {
        String expectedMessage = "Campaign \"Test Campaign\" deleted successfully!";

        WebElement successToast = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(., 'deleted successfully!')]")
            )
        );
        
        assertThat(successToast.getText()).contains(expectedMessage);
    }
}
