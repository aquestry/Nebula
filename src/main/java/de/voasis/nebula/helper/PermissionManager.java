package de.voasis.nebula.helper;

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

    private final Map<Player, Set<String>> playerPermissions = new HashMap<>();

    public void addPermission(Player player, String permission) {
        playerPermissions.computeIfAbsent(player, k -> new HashSet<>()).add(permission);
    }

    public boolean hasPermission(Player player, String permission) {
        Set<String> permissions = playerPermissions.get(player);
        if (permissions == null) {
            return false;
        }
        if (permissions.contains(permission)) {
            return true;
        }
        String[] parts = permission.split("\\.");
        StringBuilder wildcardBuilder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            wildcardBuilder.append(parts[i]);
            if (permissions.contains(wildcardBuilder + ".*")) {
                return true;
            }
            if (i < parts.length - 1) {
                wildcardBuilder.append(".");
            }
        }
        return false;
    }

    @Subscribe
    public boolean hasPermission(PermissionSubject subject, String permission) {
        if (subject instanceof Player) {
            return hasPermission((Player) subject, permission);
        }
        return subject.getPermissionValue(permission).asBoolean();
    }

    @Subscribe
    public PermissionFunction createFunction(PermissionSubject subject) {
        return permission -> {
            if (subject instanceof Player) {
                return Tristate.fromBoolean(hasPermission((Player) subject, permission));
            } else {
                return Tristate.fromBoolean(true);
            }
        };
    }
}
