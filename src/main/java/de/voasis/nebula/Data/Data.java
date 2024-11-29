package de.voasis.nebula.Data;

import de.voasis.nebula.Map.BackendServer;
import de.voasis.nebula.Map.GamemodeQueue;
import de.voasis.nebula.Map.HoldServer;
import java.util.ArrayList;
import java.util.List;

public class Data {
    public static String vsecret;
    public static String envVars;
    public static boolean pullStart;
    public static String defaultServerTemplate;
    public static int defaultmax;
    public static int defaultmin;
    public static List<String> adminUUIDs = new ArrayList<>();
    public static List<HoldServer> holdServerMap = new ArrayList<>();
    public static List<BackendServer> backendInfoMap = new ArrayList<>();
    public static List<GamemodeQueue> gamemodeQueueMap = new ArrayList<>();
    public static List<String> alltemplates = new ArrayList<>();
    public static String Icon = """
                \n
        ███╗░░██╗███████╗██████╗░██╗░░░██╗██╗░░░░░░█████╗░
        ████╗░██║██╔════╝██╔══██╗██║░░░██║██║░░░░░██╔══██╗
        ██╔██╗██║█████╗░░██████╦╝██║░░░██║██║░░░░░███████║
        ██║╚████║██╔══╝░░██╔══██╗██║░░░██║██║░░░░░██╔══██║
        ██║░╚███║███████╗██████╦╝╚██████╔╝███████╗██║░░██║
        ╚═╝░░╚══╝╚══════╝╚═════╝░░╚═════╝░╚══════╝╚═╝░░╚═╝
               \s""";
}
