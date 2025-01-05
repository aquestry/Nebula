package de.voasis.nebula.model;

import java.util.ArrayList;
import java.util.List;

public class Group {

    private final String name;
    private final String prefix;
    private final int level;
    private final List<String> permissions = new ArrayList<>();
    private final List<String> members = new ArrayList<>();

    public Group(String name, String prefix, int level) {
        this.name = name;
        this.prefix = prefix;
        this.level = level;
    }

    public String getPrefix() { return prefix; }
    public String getName() { return name; }
    public int getLevel() { return level; }
    public List<String> getPermissions() { return permissions; }
    public List<String> getMembers() { return members; }
    public void addPermission(String permission) { if (!permissions.contains(permission)) permissions.add(permission); }
    public void removePermission(String permission) { permissions.remove(permission); }
    public boolean hasPermission(String permission) { return permissions.contains(permission); }
    public void addMember(String uuid) { if (!members.contains(uuid)) members.add(uuid); }
    public void removeMember(String uuid) { members.remove(uuid); }
    public boolean hasMember(String uuid) { return members.contains(uuid); }
    public void clearMembers() { members.clear(); }
}