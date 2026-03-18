package com.operimus.Marketing;

import io.cucumber.java.After;
import io.cucumber.java.Before;

public class Hooks {

    @Before
    public void setup() {
        SharedDriver.getInstance().getDriver().manage().deleteAllCookies();
    }

    @After
    public void tearDown() {
        SharedDriver.getInstance().quit();
    }
}
