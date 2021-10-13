/*
 * Copyright (c) 2016-2017 Daniel Ennis (Aikar) - MIT License
 *
 *  Permission is hereby granted, free of charge, to any person obtaining
 *  a copy of this software and associated documentation files (the
 *  "Software"), to deal in the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish,
 *  distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to
 *  the following conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 *  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package hu.tryharddevs.advancedkits.utils.afc;

import hu.tryharddevs.advancedkits.utils.afc.annotation.Optional;
import hu.tryharddevs.advancedkits.utils.afc.contexts.OnlinePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("WeakerAccess")
public class BukkitCommandContexts extends CommandContexts {

    BukkitCommandContexts() {
        super();

        registerContext(OnlinePlayer.class, (c) -> {
            final String playercheck = c.popFirstArg();
            Player player = ACFUtil.findPlayerSmart(c.getSender(), playercheck);
            if (player == null) {
                if (c.hasAnnotation(Optional.class)) {
                    return null;
                }
                ACFUtil.sendMsg(c.getSender(), "&cCould not find a player by the name " + playercheck);
                throw new InvalidCommandArgument(false);
            }
            return new OnlinePlayer(player);
        });
        registerSenderAwareContext(World.class, (c) -> {
            String firstArg = c.getFirstArg();
            World world = firstArg != null ? Bukkit.getWorld(firstArg) : null;
            if (world != null) {
                c.popFirstArg();
            }
            if (world == null && c.getSender() instanceof Player) {
                world = ((Entity) c.getSender()).getWorld();
            }
            if (world == null) {
                throw new InvalidCommandArgument("Invalid World");
            }
            return world;
        });
        registerSenderAwareContext(CommandSender.class, CommandExecutionContext::getSender);
        registerSenderAwareContext(Player.class, (c) -> {
            Player player = c.getSender() instanceof Player ? (Player) c.getSender() : null;
            if (player == null && !c.hasAnnotation(Optional.class)) {
                throw new InvalidCommandArgument("Requires a player to run this command", false);
            }
            PlayerInventory inventory = player != null ? player.getInventory() : null;
            if (inventory != null && c.hasFlag("itemheld") && !ACFUtil.isValidItem(inventory.getItem(inventory.getHeldItemSlot()))) {
                throw new InvalidCommandArgument("You must be holding an item in your main hand.", false);
            }
            return player;
        });
        registerContext(ChatColor.class, c -> {
            String first = c.popFirstArg();
            Stream<ChatColor> colors = Stream.of(ChatColor.values());
            if (c.hasFlag("colorsonly")) {
                colors = colors.filter(color -> color.ordinal() <= 0xF);
            }
            String filter = c.getFlagValue("filter", (String) null);
            if (filter != null) {
                filter = ACFUtil.simplifyString(filter);
                String finalFilter = filter;
                colors = colors.filter(color -> finalFilter.equals(ACFUtil.simplifyString(color.name())));
            }

            ChatColor match = ACFUtil.simpleMatch(ChatColor.class, first);
            if (match == null) {
                String valid = colors
                        .map(color -> ChatColor.YELLOW + ACFUtil.simplifyString(color.name()))
                        .collect(Collectors.joining("&c, "));

                throw new InvalidCommandArgument("Please specify one of: " + valid);
            }
            return match;
        });
    }
}
