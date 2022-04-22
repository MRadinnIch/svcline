package com.svcline.models;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Props {
    private String buttonColorBg;
    private String buttonColorTxt;
    private String currentlyLoadedConfiguration;
    private String environment;
    private final boolean timekeeping;
    private final boolean clocking;

    public Props() throws IOException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        InputStream resourceStream = loader.getResourceAsStream("application.properties");
        Properties properties = new Properties();
        properties.load(resourceStream);

        this.buttonColorBg = properties.getProperty("button.color.bg");
        this.buttonColorTxt = properties.getProperty("button.color.txt");
        this.currentlyLoadedConfiguration = properties.getProperty("line.configuration");
        this.environment = properties.getProperty("environment");
        this.timekeeping = properties.getProperty("timekeeping").equalsIgnoreCase("true");
        this.clocking = properties.getProperty("clocking").equalsIgnoreCase("true");
    }

    public String getButtonColorBg() {
        return buttonColorBg;
    }

    public void setButtonColorBg(String buttonColorBg) {
        this.buttonColorBg = buttonColorBg;
    }

    public String getButtonColorTxt() {
        return buttonColorTxt;
    }

    public void setButtonColorTxt(String buttonColorTxt) {
        this.buttonColorTxt = buttonColorTxt;
    }

    public String getCurrentlyLoadedConfiguration() {
        return currentlyLoadedConfiguration;
    }

    public void setCurrentlyLoadedConfiguration(String currentlyLoadedConfiguration) {
        this.currentlyLoadedConfiguration = currentlyLoadedConfiguration;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public Boolean isLiveEnv() {
        return environment.equalsIgnoreCase("live");
    }

    public boolean isTimekeeping() {
        return timekeeping;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isClocking() {
        return clocking;
    }

    @Override
    public String toString() {
        return "Props{" +
               "buttonColorBg='" + buttonColorBg + '\'' +
               ", buttonColorTxt='" + buttonColorTxt + '\'' +
               ", currentlyLoadedConfiguration='" + currentlyLoadedConfiguration + '\'' +
               ", environment='" + environment + '\'' +
               ", timekeeping=" + timekeeping +
               '}';
    }
}
