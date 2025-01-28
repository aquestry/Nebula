package dev.aquestry.nebula.file;

import com.velocitypowered.api.permission.PermissionFunction;
import com.velocitypowered.api.permission.PermissionProvider;
import com.velocitypowered.api.permission.PermissionSubject;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import dev.aquestry.nebula.Nebula;
import dev.aquestry.nebula.data.Config;
import dev.aquestry.nebula.model.Group;

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
            Nebula.server.getPlayer(uuid).ifPresent(p -> Nebula.util.sendInfotoBackend(p));
        }
        return group;
    }

    public String getGroupData(Group group) {
        return group.getName()
               + "?" + group.getPrefix().replace(" ", "<space>")
               + "?" + group.getLevel()
               + "?" + String.join(",:", Nebula.permissionFile.getGroupMembers(group.getName())
               + "°" + String.join(",", group.getPermissions()));
    }

    public String getAllGroups() {
        return String.join("~", Nebula.permissionFile.runtimeGroups.stream().map(g -> g.getName() + "?"
                        + g.getPrefix().replace(" ", "<space>")
                        + "?" + g.getLevel()
                        + "?" + String.join(",", Nebula.permissionFile.getGroupMembers(g.getName())
                        + "°" + String.join(",", g.getPermissions()))).toList());
    }

    public void assignGroup(String uuid, Group group) {
        Nebula.permissionFile.addMemberToGroup(group, uuid);
        for(Group g : Nebula.permissionFile.runtimeGroups) {
            if(g.hasMember(uuid) && !g.equals(group)) {
                Nebula.permissionFile.removeMemberFromGroup(g, uuid);
            }
        }
        Nebula.util.log("'{}' is now in group {} sending info to backend.", uuid, group.getName());
        Nebula.server.getAllPlayers().stream().forEach(p -> {
          if(p.getUniqueId().toString().equals(uuid)) {
              Nebula.util.sendInfotoBackend(p);
          }
        });
    }
}