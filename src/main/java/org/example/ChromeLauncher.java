package org.example;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class ChromeLauncher {

    private ChromeOptions chromeOptions;

    private void setDefaultChromeOptions() {
        chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--start-maximized");
        chromeOptions.addArguments("--headless=new");
//        Set default user agent for the website to work in headless mode
        chromeOptions.addArguments("user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36");
    }

    public WebDriver launch() {
        if (chromeOptions == null) {
            setDefaultChromeOptions();
        }
        WebDriver webDriver = new ChromeDriver(chromeOptions);
        return webDriver;
    }

    public ChromeOptions getChromeOptions() {
        return chromeOptions;
    }

    public void setChromeOptions(ChromeOptions chromeOptions) {
        this.chromeOptions = chromeOptions;
    }

}
