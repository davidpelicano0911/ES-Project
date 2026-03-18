package com.operimus.Marketing.steps;

import com.operimus.Marketing.SharedDriver;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import static org.assertj.core.api.Assertions.assertThat;

public class Feature01Steps {

    private final SharedDriver shared = SharedDriver.getInstance();
    private final WebDriver driver = shared.getDriver();
    private final WebDriverWait wait = shared.getWait();

    @Then("the username and role should be visible in the navbar")
    public void the_username_and_role_should_be_visible_in_the_navbar() {
        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//span[@class='text-sm font-medium text-gray-800']")
        ));
        WebElement role = driver.findElement(By.xpath("//span[@class='text-xs text-gray-500 capitalize']"));
        WebElement signOut = driver.findElement(By.xpath("//button[contains(text(),'Sign Out')]"));

        assertThat(username.getText()).isNotEmpty();
        assertThat(role.getText()).isNotEmpty();
        assertThat(signOut.isDisplayed()).isTrue();
    }
}
