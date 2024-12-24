package de.voasis.nebula.helper;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.permission.PermissionFunction;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.permission.PermissionProvider;
import com.velocitypowered.api.permission.PermissionSubject;
import de.voasis.nebula.map.Group;
import java.util.HashMap;
import java.util.Map;

public class PermissionManager implements PermissionProvider {

    private final Group adminGroup = new Group("admin", "<dark_gray>[<red>Admin<dark_gray>] <white>", 2);
    private final Group defaultGroup = new Group("default", "<dark_gray>[<white>Player<dark_gray>] <white>", 1);
    private final Map<Player, Group> playerGroups = new HashMap<>();

    public PermissionManager() {
        adminGroup.addPermission("velocity.admin");
    }

    public Group getGroup(Player player) {
        return playerGroups.getOrDefault(player, defaultGroup);
    }

    public boolean hasPermission(Player player, String permission) {
        return getGroup(player).hasPermission(permission);
    }

    public void updateGroup(Player player, String groupName) {
        if ("admin".equalsIgnoreCase(groupName)) {
            playerGroups.put(player, adminGroup);
        } else {
            playerGroups.put(player, defaultGroup);
        }
    }

    @Subscribe
    public PermissionFunction createFunction(PermissionSubject subject) {
        return permission -> Tristate.fromBoolean(
                subject instanceof Player && hasPermission((Player) subject, permission)
        );
    }

    public String getGroupInfo(Player player) {
        Group group = playerGroups.getOrDefault(player, defaultGroup);
        return player.getUsername() + ":" + group.getName() + "#" + group.getLevel() + "#" + group.getPrefix();
    }
}