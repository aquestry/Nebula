package de.voasis.nebula.data;

import de.voasis.nebula.map.BackendServer;
import de.voasis.nebula.map.GamemodeQueue;
import de.voasis.nebula.map.HoldServer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Data {
    public static String vsecret;
    public static String envVars;
    public static boolean pullStart;
    public static String defaultServerTemplate;
    public static int defaultmax;
    public static int defaultmin;
    public static String defaultGroupName;
    public static List<HoldServer> holdServerMap = new ArrayList<>();
    public static List<BackendServer> backendInfoMap = new ArrayList<>();
    public static List<GamemodeQueue> gamemodeQueueMap = new ArrayList<>();
    public static final Map<String, Long> cooldownsPluginMessage = new ConcurrentHashMap<>();
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