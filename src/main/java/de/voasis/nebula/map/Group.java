package de.voasis.nebula.map;

import java.util.ArrayList;
import java.util.List;

public class Group {

    private final String name;
    private final String prefix;
    private final int level;
    private static List<String> permissions = new ArrayList<>();

    public Group(String name, String prefix, int level) {
        this.name = name;
        this.prefix = prefix;
        this.level = level;
    }

    public String getPrefix() { return prefix; }
    public String getName() { return name; }
    public int getLevel() { return level; }
    public void addPermission(String permission) { permissions.add(permission); }
    public boolean hasPermission(String permission) { return permissions.contains(permission); }
}