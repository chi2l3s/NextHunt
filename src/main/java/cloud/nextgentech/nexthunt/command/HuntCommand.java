package cloud.nextgentech.nexthunt.command;

import cloud.nextgentech.nexthunt.Config;
import cloud.nextgentech.nexthunt.command.list.ReloadConfigCommand;
import cloud.nextgentech.nexthunt.command.list.StartHuntCommand;
import cloud.nextgentech.nexthunt.command.list.StopHuntCommand;
import cloud.nextgentech.nexthunt.manager.HuntManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.amixoldev.api.commandmanagerapi.command.LongCommandExecutor;

import java.util.List;

public class HuntCommand extends LongCommandExecutor {

    public HuntCommand(HuntManager huntManager, Config config) {
        addSubCommand(new StartHuntCommand(huntManager), new String[]{"start"}, new Permission("nexthunt.start"));
        addSubCommand(new StopHuntCommand(huntManager), new String[]{"stop"}, new Permission("nexthunt.stop"));
        addSubCommand(new ReloadConfigCommand(config), new String[]{"reload"}, new Permission("nexthunt.reload"));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) return false;
        final SubCommandWrapper wrapper = getWrapperFromLabel(args[0]);
        if (wrapper == null) return false;

        if (!sender.hasPermission(wrapper.getPermission())) {
            sender.sendMessage(command.getPermissionMessage());
            return true;
        }

        wrapper.getCommand().onExecute(sender, args);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return getFirstAliases();
        }
        final SubCommandWrapper wrapper = getWrapperFromLabel(args[0]);
        if (wrapper == null) return null;

        return wrapper.getCommand().onTabComplete(sender, args);
    }
}
