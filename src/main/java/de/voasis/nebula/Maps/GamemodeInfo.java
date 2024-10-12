package de.voasis.nebula.Maps;

public class GamemodeInfo {

    private final String name;
    private final int neededPlayers;
    private final String templateName;

    public GamemodeInfo(String name, int neededPlayers, String templateName) {
        this.name = name;
        this.neededPlayers = neededPlayers;
        this.templateName = templateName;
    }
    public int getNeededPlayers() { return neededPlayers; }
    public String getTemplateName() { return templateName; }
    public String getName() { return name; }
}
