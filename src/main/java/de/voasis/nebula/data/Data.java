package de.voasis.nebula.data;

import de.voasis.nebula.map.Container;
import de.voasis.nebula.map.Queue;
import de.voasis.nebula.map.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Data {
    public static boolean quitting;
    public static String envVars;
    public static boolean pullStart;
    public static String defaultServerTemplate;
    public static int defaultmax;
    public static int defaultmin;
    public static String defaultGroupName;
    public static List<Node> nodeMap = new ArrayList<>();
    public static List<Container> backendInfoMap = new ArrayList<>();
    public static List<Queue> queueMap = new ArrayList<>();
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