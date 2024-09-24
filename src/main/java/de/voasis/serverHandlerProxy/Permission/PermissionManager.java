package de.voasis.serverHandlerProxy.Permission;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.permission.PermissionFunction;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.permission.PermissionProvider;
import com.velocitypowered.api.permission.PermissionSubject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PermissionManager implements PermissionProvider {

    private final Map<Player, Set<String>> playerPermissions;

    public PermissionManager() {
        playerPermissions = new HashMap<>();
    }

    public void addPermission(Player player, String permission) {
        playerPermissions.computeIfAbsent(player, k -> new HashSet<>()).add(permission);
    }

    public void removePermission(Player player, String permission) {
        Set<String> permissions = playerPermissions.get(player);
        if (permissions != null) {
            permissions.remove(permission);
        }
    }

    public boolean hasPermission(Player player, String permission) {
        Set<String> permissions = playerPermissions.get(player);
        return permissions != null && permissions.contains(permission);
    }

    @Subscribe
    public boolean hasPermission(PermissionSubject subject, String permission) {
        if (subject instanceof Player) {
            return hasPermission((Player) subject, permission);
        }
        // Console or other non-player subjects can be granted permissions here
        return subject.getPermissionValue(permission).asBoolean(); // Allow console access
    }

    @Subscribe
    public PermissionFunction createFunction(PermissionSubject subject) {
        return permission -> {
            if (subject instanceof Player) {
                return Tristate.fromBoolean(hasPermission((Player) subject, permission));
            } else {
                // For console or other non-player subjects, return true or false based on your needs
                return Tristate.fromBoolean(true); // You can change this to false if you want to restrict access
            }
        };
    }
}