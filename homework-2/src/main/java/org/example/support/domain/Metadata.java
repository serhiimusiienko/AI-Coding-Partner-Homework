package org.example.support.domain;

public class Metadata {
    private Source source;
    private String browser;
    private DeviceType deviceType;

    public enum Source {
        WEB_FORM, EMAIL, API, CHAT, PHONE
    }

    public enum DeviceType {
        DESKTOP, MOBILE, TABLET
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }
}
