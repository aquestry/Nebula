package de.voasis.serverHandlerProxy.Maps;

public class GamemodeInfo {

    private String name;
    private int neededPlayers;
    private String templateName;

    public GamemodeInfo(String name, int neededPlayers, String templateName) {
        this.name = name;
        this.neededPlayers = neededPlayers;
        this.templateName = templateName;
    }
    public int getNeededPlayers() {
        return neededPlayers;
    }
    public String getTemplateName() {
        return templateName;
    }
    public String getName() {
        return name;
    }
}
