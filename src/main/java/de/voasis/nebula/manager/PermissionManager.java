package de.voasis.nebula.manager;

import com.velocitypowered.api.permission.PermissionFunction;
import com.velocitypowered.api.permission.PermissionProvider;
import com.velocitypowered.api.permission.PermissionSubject;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Config;
import de.voasis.nebula.model.Group;
import java.util.Arrays;
import java.util.List;

public class PermissionManager implements PermissionProvider {
    public boolean hasPermission(Player player, String permission) {
        return Nebula.permissionFile.runtimeGroups.stream()
                .anyMatch(group -> group.hasMember(player.getUniqueId().toString()) && group.hasPermission(permission));
    }

    @Override
    public PermissionFunction createFunction(PermissionSubject subject) {
        return permission -> Tristate.fromBoolean(
                subject instanceof Player && hasPermission((Player) subject, permission)
        );
    }

    public Group getGroup(String uuid) {
        Group group = Nebula.permissionFile.runtimeGroups.stream()
                .filter(g -> g.hasMember(uuid))
                .findFirst()
                .orElse(null);
        if(group == null) {
            Nebula.util.log("Giving {} the default group.", uuid);
            group = Nebula.permissionFile.runtimeGroups.stream().filter(g -> g.getName().equals(Config.defaultGroupName)).toList().getFirst();
            assignGroup(uuid, group);
        }
        return group;
    }

    public String getGroupData(Group group) {
        return group.getName()
               + "?" + group.getPrefix().replace(" ", "<space>")
               + "?" + group.getLevel()
               + "?" + String.join(":", Nebula.permissionFile.getGroupMembers(group.getName())
               + "°" + String.join(":", group.getPermissions()));
    }

    public String getAllGroups() {
        return String.join("~", Nebula.permissionFile.runtimeGroups.stream().map(g -> g.getName() + "?"
                        + g.getPrefix().replace(" ", "<space>")
                        + "?" + g.getLevel()
                        + "?" + String.join(":", Nebula.permissionFile.getGroupMembers(g.getName())
                        + "°" + String.join(":", g.getPermissions()))).toList());
    }

    public void assignGroup(String uuid, Group group) {
        Nebula.permissionFile.addMemberToGroup(group, uuid);
        for(Group g : Nebula.permissionFile.runtimeGroups) {
            if(g.hasMember(uuid) && !g.equals(group)) {
                Nebula.permissionFile.removeMemberFromGroup(g, uuid);
            }
        }
        Nebula.server.getPlayer(uuid).ifPresent(value -> Nebula.util.sendInfotoBackend(value));
    }

    public void processGroups(String response) {
        int updated = 0;
        for (String groupData : response.split("~")) {
            try {
                groupData = groupData.trim();
                String[] parts = groupData.split("\\?");
                String groupName = parts[0].trim();
                String prefix = parts[1].replace("<space>", " ");
                int level = Integer.parseInt(parts[2].trim());
                String[] members = new String[0];
                String[] perms = new String[0];
                if (parts.length == 4) {
                    String[] uuidsPerm = parts[3].split("°");
                    if (uuidsPerm.length > 0) {
                        members = uuidsPerm[0].replace("[", "").replace("]", "").split(":");
                    }
                    if (uuidsPerm.length == 2) {
                        perms = uuidsPerm[1].split(":");
                    }
                }
                Group group = Nebula.permissionFile.createGroup(groupName, prefix, level);
                List<String> oldMembers = group.getMembers();
                for (String oldMember : oldMembers) {
                    if (!Arrays.asList(members).contains(oldMember)) {
                        Nebula.util.log("Removed member '{}' from group '{}'.", oldMember, groupName);
                    }
                }
                Nebula.permissionFile.clearMembers(group);
                for (String member : members) {
                    if (!member.isEmpty()) {
                        Nebula.permissionManager.assignGroup(member, group);
                        Nebula.util.log("Added member '{}' to group '{}'.", member, groupName);
                    }
                }
                Nebula.permissionFile.clearPermissions(group);
                for (String perm : perms) {
                    if (!perm.isEmpty()) {
                        Nebula.permissionFile.addPermissionToGroup(group, perm);
                    }
                }
                updated++;
            } catch (NumberFormatException e) {
                Nebula.util.log("Failed to parse level in group data '{}'. Error: {}", groupData, e.getMessage());
            } catch (Exception e) {
                Nebula.util.log("Failed to process group data '{}'. Error: {}", groupData, e.getMessage());
            }
        }
        Nebula.permissionFile.saveConfig();
        Nebula.util.sendAlltoBackend();
        Nebula.util.log("Group processing completed. Total groups updated: {}.", updated);
    }
}