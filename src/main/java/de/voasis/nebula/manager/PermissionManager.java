package de.voasis.nebula.manager;

import com.velocitypowered.api.permission.PermissionFunction;
import com.velocitypowered.api.permission.PermissionProvider;
import com.velocitypowered.api.permission.PermissionSubject;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Config;
import de.voasis.nebula.model.Group;

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

    public void assignGroup(String uuid, Group group) {
        for(Group g : Nebula.permissionFile.runtimeGroups) {
            if(g.hasMember(uuid)) {
                Nebula.permissionFile.removeMemberFromGroup(g, uuid);
            }
        }
        Nebula.permissionFile.addMemberToGroup(group, uuid);
    }

    public void sendInfotoBackend(Player player) {
        Group group = getGroup(player.getUniqueId().toString());
        String info = player.getUsername() + ":" + group.getName() + "#" + group.getLevel() + "#" + group.getPrefix();
        player.getCurrentServer().ifPresent(serverConnection -> serverConnection.getServer().sendPluginMessage(Nebula.channelMain, info.getBytes()));
    }
}
