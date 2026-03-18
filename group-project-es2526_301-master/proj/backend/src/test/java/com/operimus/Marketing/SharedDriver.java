package com.operimus.Marketing;

import java.time.Duration;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

public class SharedDriver {

    private static final ThreadLocal<SharedDriver> threadLocalDriver = ThreadLocal.withInitial(SharedDriver::new);
    private final WebDriver driver;
    private final WebDriverWait wait;

    private SharedDriver() {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1550,830");
        
        // Accept insecure certificates and suppress ALL security warnings
        options.setAcceptInsecureCerts(true);
        options.addPreference("security.insecure_field_warning.contextual.enabled", false);
        options.addPreference("security.insecure_password.ui.enabled", false);
        options.addPreference("security.warn_submit_insecure", false);
        options.addPreference("security.warn_submit_secure_to_insecure", false);
        options.addPreference("security.mixed_content.block_active_content", false);
        options.addPreference("security.mixed_content.block_display_content", false);
        options.addPreference("security.certerrors.mitm.auto_enable_enterprise_roots", true);
        
        // Disable all prompts and alerts
        options.addPreference("browser.helperApps.alwaysAsk.force", false);
        options.addPreference("browser.download.manager.showAlertOnComplete", false);
        options.addPreference("browser.download.manager.closeWhenDone", true);
        options.addPreference("browser.tabs.warnOnClose", false);
        options.addPreference("browser.tabs.warnOnOpen", false);
        options.addPreference("browser.sessionstore.resume_from_crash", false);
        options.addPreference("browser.startup.page", 0);
        
        // Disable modal dialogs
        options.addPreference("prompts.tab_modal.enabled", false);
        options.addPreference("dom.disable_beforeunload", true);

        this.driver = new FirefoxDriver(options);

        // Increased timeouts to allow for slower page loads and element rendering
        this.driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
        this.driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        this.driver.manage().window().setSize(new Dimension(1550, 830));
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    public static SharedDriver getInstance() {
        return threadLocalDriver.get();
    }

    public WebDriver getDriver() {
        return driver;
    }

    public WebDriverWait getWait() {
        return wait;
    }

    public void quit() {
        if (driver != null) {
            driver.quit();
        }
        threadLocalDriver.remove();
    }
}
