package chat.util;

import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class Paths {

    private String logo;
    private AppProperty settingsProperties;
    private String chatCss;
    private String confirmCss;
    private String dataCss;
    private String settingsCss;
    private String enabledPin;
    private String disabledPin;
    private String twitchProperties;
    private String soundDirectory;
    private String commandsJson;
    private String directsJson;
    private String ranksJson;
    private String smilesJson;
    private String usersJson;

    public Paths() {
        //do nothing
    }

    public Paths(final String logo, final AppProperty settingsProperties,
                 final String chatCss, final String confirmCss, final String dataCss,
                 final String settingsCss, final String enabledPin, final String disabledPin,
                 final String twitchProperties, final String soundDirectory, final String commandsJson,
                 final String directsJson, final String ranksJson, final String smilesJson,
                 final String usersJson) {
        this.logo = logo;
        this.settingsProperties = settingsProperties;
        this.chatCss = chatCss;
        this.confirmCss = confirmCss;
        this.dataCss = dataCss;
        this.settingsCss = settingsCss;
        this.enabledPin = enabledPin;
        this.disabledPin = disabledPin;
        this.twitchProperties = twitchProperties;
        this.soundDirectory = soundDirectory;
        this.commandsJson = commandsJson;
        this.directsJson = directsJson;
        this.ranksJson = ranksJson;
        this.smilesJson = smilesJson;
        this.usersJson = usersJson;
    }

    public String getLogo() {
        return this.logo;
    }

    public String getChatCSS() {
        final Properties settings = this.settingsProperties.getProperty();
        return "/theme/" + settings.getProperty(Settings.ROOT_THEME) + this.chatCss;
    }

    public String getConfirmCSS() {
        final Properties settings = this.settingsProperties.getProperty();
        return "/theme/" + settings.getProperty(Settings.ROOT_THEME) + this.confirmCss;
    }

    public String getDataCSS() {
        final Properties settings = this.settingsProperties.getProperty();
        return "/theme/" + settings.getProperty(Settings.ROOT_THEME) + this.dataCss;
    }

    public String getSettingsCSS() {
        final Properties settings = this.settingsProperties.getProperty();
        return "/theme/" + settings.getProperty(Settings.ROOT_THEME) + this.settingsCss;
    }

    public String getEnabledPin() {
        return this.enabledPin;
    }

    public String getDisabledPin() {
        return this.disabledPin;
    }

    public String getTwitchProperties() {
        return this.twitchProperties;
    }

    public String getSoundsDirectory() {
        return this.soundDirectory;
    }

    public String getCommandsJson() {
        return this.commandsJson;
    }

    public String getDirectsJson() {
        return this.directsJson;
    }

    public String getRanksJson() {
        return this.ranksJson;
    }

    public String getSmilesJson() {
        return this.smilesJson;
    }

    public String getUsersJson() {
        return this.usersJson;
    }
}
