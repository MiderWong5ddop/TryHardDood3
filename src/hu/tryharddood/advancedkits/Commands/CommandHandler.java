package hu.tryharddood.advancedkits.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;

/**
 * Class:
 *
 * @author TryHardDood
 */
public class CommandHandler implements CommandExecutor
{
    private static HashMap<List<String>, Subcommand> commands = new HashMap<>();

    public static void addComand(List<String> cmds, Subcommand s)
    {
        commands.put(cmds, s);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (args.length >= 1)
        {
            boolean match = false;

            for (List<String> s : commands.keySet())
            {
                if (s.contains(args[0]))
                {
                    commands.get(s).runCommand(sender, cmd, label, args);
                    match = true;
                }
            }

            if (!match)
            {
                sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Error! " + ChatColor.GRAY + "This command doesn't exist!");
            }
        }
        else
        {
            new MainCommand().runCommand(sender, cmd, label, args);
        }

        return true;
    }

}
