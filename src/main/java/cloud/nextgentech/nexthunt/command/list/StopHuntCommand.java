package cloud.nextgentech.nexthunt.command.list;

import cloud.nextgentech.nexthunt.manager.HuntManager;
import org.bukkit.command.CommandSender;
import ru.amixoldev.api.commandmanagerapi.command.SubCommand;

import java.util.Collections;
import java.util.List;

public class StopHuntCommand implements SubCommand {
    private final HuntManager huntManager;

    public StopHuntCommand(HuntManager huntManager) {
        this.huntManager = huntManager;
    }

    @Override
    public void onExecute(CommandSender commandSender, String[] strings) {
        huntManager.stop();
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, String[] strings) {
        return Collections.emptyList();
    }
}
