package de.voasis.nebula.Helper;

import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Nebula;

public class AutoDeleter {
    public void process() {
        for(BackendServer backendServer : Nebula.dataHolder.backendInfoMap) {
            if(backendServer.getPendingPlayerConnections().isEmpty() && backendServer.getAutoDelete()) {
                Nebula.serverManager.delete(backendServer, null);
            }
        }
    }
}
