package cloud.nextgentech.nexthunt.util;

import cloud.nextgentech.nexthunt.Config;

public class Placeholder {
    public static String replacePrefix(String s, Config config) {
        return Color.format(s.replaceAll("%prefix%", config.getPrefix()));
    }
}
