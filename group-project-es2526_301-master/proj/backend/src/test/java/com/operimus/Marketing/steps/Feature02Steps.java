package com.operimus.Marketing.steps;

import com.operimus.Marketing.SharedDriver;
import io.cucumber.java.After;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import static org.assertj.core.api.Assertions.assertThat;

public class Feature02Steps {

    private final SharedDriver shared = SharedDriver.getInstance();
    private final WebDriver driver = shared.getDriver();
    private final WebDriverWait wait = shared.getWait();

    @After
    public void teardown() {
        shared.quit();
    }

    @When("the user clicks the logout option")
    public void the_user_clicks_the_logout_option() {
        wait.until(ExpectedConditions.urlContains("/app/campaigns"));
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Sign Out')]")
        ));
        logoutButton.click();
    }

    @Then("the session should end and redirect to the Keycloak login page")
    public void the_session_should_end_and_redirect_to_the_Keycloak_login_page() {
        wait.until(ExpectedConditions.urlContains("realms/marketing-realm"));
        assertThat(driver.getCurrentUrl()).contains("realms/marketing-realm");
    }
}
