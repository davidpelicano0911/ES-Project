package com.operimus.Marketing.steps;

import com.operimus.Marketing.SharedDriver;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Feature21Steps {

    private final SharedDriver shared = SharedDriver.getInstance();
    private final WebDriverWait wait = shared.getWait();

    @And("the content creator edits the subject to {string}")
    public void the_content_creator_edits_the_subject_to(String newSubject) {
        WebElement subjectInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[placeholder*='Welcome to']")));

        subjectInput.clear();
        subjectInput.sendKeys(newSubject);
    }

}
