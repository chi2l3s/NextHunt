package cloud.nextgentech.nexthunt.util;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundUtil {
    public static void playSound(String input, Player player) {
        String[] parts = input.split(":");

        String soundString = parts[0];
        float pitch = Float.parseFloat(parts[1]);
        float volume = Float.parseFloat(parts[2]);

        Sound sound;
        try {
            sound = Sound.valueOf(soundString);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Неправильный тип звука: " + e.getMessage());
        }

        player.playSound(player.getLocation(), sound, pitch, volume);
    }
}
