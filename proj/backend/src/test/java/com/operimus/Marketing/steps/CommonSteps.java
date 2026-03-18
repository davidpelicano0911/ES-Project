package com.operimus.Marketing.steps;

import com.operimus.Marketing.SharedDriver;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.assertj.core.api.Assertions.assertThat;

public class CommonSteps {

    private final SharedDriver shared = SharedDriver.getInstance();
    private final WebDriver driver = shared.getDriver();
    private final WebDriverWait wait = shared.getWait();

    @Given("the user is on the login page")
    public void the_user_is_on_the_login_page() {
        driver.get("http://localhost:3000/");
        wait.until(ExpectedConditions.urlContains("realms/marketing-realm"));
    }

    @When("the user enters valid credentials")
    public void the_user_enters_valid_credentials() {
        WebElement usernameInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        WebElement passwordInput = driver.findElement(By.id("password"));
        usernameInput.sendKeys("joao");
        passwordInput.sendKeys("12345678");
    }

    @When("the user clicks the login button")
    public void the_user_clicks_the_login_button() {
        driver.findElement(By.id("kc-login")).click();
    }

    @Then("the user should be redirected to the Campaigns page")
    public void the_user_should_be_redirected_to_the_campaigns_page() {
        wait.until(ExpectedConditions.urlContains("/app/campaigns"));
        assertThat(driver.getCurrentUrl()).contains("/app/campaigns");
    }
}
