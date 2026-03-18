package com.operimus.Marketing.steps;

import com.operimus.Marketing.SharedDriver;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.assertj.core.api.Assertions.assertThat;

public class ContentCommonSteps {

    private final SharedDriver shared = SharedDriver.getInstance();
    private final WebDriver driver = shared.getDriver();
    private final WebDriverWait wait = shared.getWait();

    @Given("the content creator is on the login page")
    public void the_content_creator_is_on_the_login_page() {
        driver.get("http://localhost:3000/");
        wait.until(ExpectedConditions.urlContains("realms/marketing-realm"));
    }

    @When("the content creator enters valid credentials")
    public void the_content_creator_enters_valid_credentials() {
        WebElement usernameInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        WebElement passwordInput = driver.findElement(By.id("password"));
        usernameInput.sendKeys("rafaela");
        passwordInput.sendKeys("12345678");
    }

    @When("the content creator clicks the login button")
    public void the_content_creator_clicks_the_login_button() {
        driver.findElement(By.id("kc-login")).click();
    }

    @Then("the content creator should be redirected to the Campaigns page")
    public void the_content_creator_should_be_redirected_to_the_campaigns_page() {
        wait.until(ExpectedConditions.urlContains("/app/campaigns"));
        assertThat(driver.getCurrentUrl()).contains("/app/campaigns");
    }

    @Then("the Landing Pages page should be displayed")
    public void the_landing_pages_page_should_be_displayed() {
        wait.until(ExpectedConditions.urlContains("/app/landing-pages"));
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h1[contains(text(), 'Landing Pages')]")));
        assertThat(title.isDisplayed()).isTrue();
    }


    @Given("the content marketer is on the Landing Pages list with at least one item")
    public void the_user_is_on_the_landing_pages_list_with_at_least_one_item() {
        if (!driver.getCurrentUrl().contains("/app/landing-pages")) {
            driver.get("http://localhost:3000/app/landing-pages");
        }

        WebDriverWait longWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(15));
        WebElement title = longWait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h1[contains(text(), 'Landing Pages')]")));
        assertThat(title.isDisplayed()).isTrue();

        int pageCount = driver.findElements(By.xpath("//h3")).size();
        assertThat(pageCount)
            .withFailMessage("No landing pages found. At least one must exist.")
            .isGreaterThan(0);
    }

    @When("the content marketer navigates to the Landing Pages section")
    public void the_content_marketer_navigates_to_the_landing_pages_section() {
        WebElement landingLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(., 'Landing Pages')]")));
        landingLink.click();
        
        wait.until(ExpectedConditions.urlContains("/app/landing-pages"));
    }
}
