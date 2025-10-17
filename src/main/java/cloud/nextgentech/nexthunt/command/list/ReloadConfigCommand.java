package cloud.nextgentech.nexthunt.command.list;

import cloud.nextgentech.nexthunt.Config;
import cloud.nextgentech.nexthunt.util.Placeholder;
import org.bukkit.command.CommandSender;
import ru.amixoldev.api.commandmanagerapi.command.SubCommand;

import java.util.List;

public class ReloadConfigCommand implements SubCommand {

    private final Config config;

    public ReloadConfigCommand(Config config) {
        this.config = config;
    }

    @Override
    public void onExecute(CommandSender commandSender, String[] strings) {
        config.reloadConfig();

        commandSender.sendMessage(Placeholder.replacePrefix(config.getConfigReload(), config));
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] strings) {
        return List.of();
    }
}
