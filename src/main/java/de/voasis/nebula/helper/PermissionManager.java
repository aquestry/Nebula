package de.voasis.nebula.helper;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.permission.PermissionFunction;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.permission.PermissionProvider;
import com.velocitypowered.api.permission.PermissionSubject;
import de.voasis.nebula.data.Data;
import java.util.*;

public class PermissionManager implements PermissionProvider {

    private final Map<Player, Set<String>> playerPermissions = new HashMap<>();

    public String getRank(Player player) {
        if (Data.adminUUIDs.contains(player.getUniqueId().toString())) {
            return "admin#1#<red>Admin <white>";
        }
        return "default#2#<blue>Player <white>";
    }

    public void addPermission(Player player, String permission) {
        playerPermissions.computeIfAbsent(player, k -> new HashSet<>()).add(permission);
    }

    public boolean hasPermission(Player player, String permission) {
        Set<String> permissions = playerPermissions.getOrDefault(player, Collections.emptySet());
        if (permissions.contains(permission)) return true;
        String[] parts = permission.split("\\.");
        StringBuilder wildcard = new StringBuilder();
        for (String part : parts) {
            wildcard.append(part).append(".");
            if (permissions.contains(wildcard + "*")) return true;
        }
        return false;
    }

    @Subscribe
    public PermissionFunction createFunction(PermissionSubject subject) {
        return permission -> Tristate.fromBoolean(
                subject instanceof Player && hasPermission((Player) subject, permission)
        );
    }
}
