package cloud.nextgentech.nexthunt.command.list;

import cloud.nextgentech.nexthunt.manager.HuntManager;
import cloud.nextgentech.nexthunt.util.Color;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import ru.amixoldev.api.commandmanagerapi.command.SubCommand;

import java.util.Collections;
import java.util.List;

public class StartHuntCommand implements SubCommand {
    private final HuntManager huntManager;

    public StartHuntCommand(HuntManager huntManager) {
        this.huntManager = huntManager;
    }

    @Override
    public void onExecute(CommandSender commandSender, String[] strings) {
        if (strings.length < 2) {
            commandSender.sendMessage(Color.format("&cВведите ник игрока!"));
            return;
        }

        Player player = Bukkit.getPlayer(strings[1]);

        if (player == null) {
            commandSender.sendMessage(Color.format("&cИгрок не найден!"));
            return;
        }

        huntManager.startCountdown(player);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] strings) {
        if (strings.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(HumanEntity::getName)
                    .toList();
        }

        return Collections.emptyList();
    }
}
