package com.operimus.Marketing.steps;

import com.operimus.Marketing.SharedDriver;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.io.File;
import static org.assertj.core.api.Assertions.assertThat;

public class Feature31Steps {

    private final SharedDriver shared = SharedDriver.getInstance();
    private final WebDriver driver = shared.getDriver();
    private final WebDriverWait wait = shared.getWait();
    private String createdPostName;



    @Given("the content creator navigates to the Social Posts page")
    public void the_content_creator_navigates_to_the_social_posts_page() {
        try {
            WebElement formsLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(.,'Social Posts')]")
            ));
            formsLink.click();
            
        } catch (Exception e) {
            if (!driver.getCurrentUrl().contains("/app/social-posts")) {
                driver.get("http://localhost:3000/app/social-posts");
            }
        }
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h1[contains(text(), 'Social Media Posts')]")));
        assertThat(title.isDisplayed()).isTrue();
    }

    @When("the content creator clicks the \"Create Post\" button")
    public void the_content_creator_clicks_the_create_post_button() {
        WebElement createButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(., 'Create Post')]")));
        createButton.click();
        wait.until(ExpectedConditions.urlContains("/app/social-posts/create"));
    }

    @When("fills in the post form with:")
    public void fills_in_the_post_form_with(io.cucumber.datatable.DataTable dataTable) {
        var map = dataTable.asMap(String.class, String.class);
        createdPostName = map.get("name");

        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("input[placeholder='Enter post name']")));
        nameInput.clear();
        nameInput.sendKeys(createdPostName);

        WebElement descInput = driver.findElement(By.cssSelector("textarea[placeholder*='Describe']"));
        descInput.clear();
        descInput.sendKeys(map.get("description"));

        WebElement platformCheckbox = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//label[contains(., '" + map.get("platform") + "')]//input[@type='checkbox']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", platformCheckbox);
    }

    @When("uploads an image for the post")
    public void uploads_an_image_for_the_post() {
        WebElement fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[type='file']")));
        File imageFile = new File("src/test/resources/testdata/test-image.png");
        assertThat(imageFile.exists())
            .withFailMessage("Test image not found at src/test/resources/testdata/test-image.png")
            .isTrue();
        fileInput.sendKeys(imageFile.getAbsolutePath());
    }

    @When("selects \"Publish Now\"")
    public void selects_publish_now() {
        WebElement publishNowRadio = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[value='now']")));
        publishNowRadio.click();
    }

    @When("submits the social post form")
    public void submits_the_social_post_form() {
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(., 'Publish Now') or contains(., 'Schedule Post')]")));
        submitBtn.click();

        try {
            WebElement confirmModal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(), 'Confirm Post Creation')]")));
            assertThat(confirmModal.isDisplayed()).isTrue();

            WebElement yesButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(., 'Yes, create')]")));
            yesButton.click();
        } catch (TimeoutException ignored) {
        }

        wait.until(ExpectedConditions.urlContains("/app/social-posts"));
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), 'Post created')]")),
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), 'success')]"))
        ));
    }

    @When("the content creator deletes the newly created post")
    public void the_content_creator_deletes_the_newly_created_post() {
        
        WebElement deleteBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//h3[contains(text(), '" + createdPostName + "')]/ancestor::div[contains(@class,'bg-white')]//button[@title='Delete Post']")));
        deleteBtn.click();

        WebElement confirm = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(., 'Delete Post')]")));
        confirm.click();
    }
}