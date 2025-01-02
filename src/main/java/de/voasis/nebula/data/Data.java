package de.voasis.nebula.data;

import de.voasis.nebula.map.Container;
import de.voasis.nebula.map.Proxy;
import de.voasis.nebula.map.Queue;
import de.voasis.nebula.map.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Data {
    public static String envVars;
    public static String HMACSecret;
    public static String defaultGroupName;
    public static String defaultServerTemplate;
    public static boolean quitting;
    public static boolean multiProxyMode;
    public static boolean pullStart;
    public static int multiProxyPort;
    public static int defaultmax;
    public static int defaultmin;
    public static List<Node> nodeMap = new ArrayList<>();
    public static List<Proxy> proxyMap = new ArrayList<>();
    public static List<Container> backendInfoMap = new ArrayList<>();
    public static List<Queue> queueMap = new ArrayList<>();
    public static List<String> alltemplates = new ArrayList<>();
    public static final Map<String, Long> cooldownsPluginMessage = new ConcurrentHashMap<>();
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