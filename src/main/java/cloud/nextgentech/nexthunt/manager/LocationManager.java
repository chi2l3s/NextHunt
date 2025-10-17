package cloud.nextgentech.nexthunt.manager;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Random;

public class LocationManager {

    public static Random random = new Random();

    public Location generateRandomLocation(Player player, World world) {
        Location location = generateRandomSquareLocation(player, world);

        if (location == null) {
            return generateRandomLocation(player, world);
        }

        return  location;
    }

    public Location generateRandomSquareLocation(Player player, World world) {
        int minX = 400;
        int maxX = 2000;
        int minZ = 400;
        int maxZ = 2000;
        int x, z = 0;

        x = random.nextInt((maxX - minX) + 1) + minX;
        z = random.nextInt((maxZ - minZ) + 1) + minZ;

        int y = findSafeSurfacePoint(world, x, z);
        if (y < 0) {
            return null;
        }

        Location playerLocation = player.getLocation();
        Location location = new Location(world, x + 0.5D, y, z + 0.5D, playerLocation.getYaw(), playerLocation.getPitch());

        location.setY(y + 1D);
        return location;
    }

    private int findSafeSurfacePoint(World world, int x, int z) {
        return world.getHighestBlockYAt(x, z);
    }
}
