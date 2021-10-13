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

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class BukkitCommandManager implements CommandManager {

    @SuppressWarnings("WeakerAccess")
    protected final Plugin plugin;
    private final CommandMap commandMap;
    protected Map<String, Command> knownCommands = new HashMap<>();
    protected Map<String, BaseCommand> registeredCommands = new HashMap<>();
    protected CommandContexts contexts;
    protected CommandCompletions completions;

    public BukkitCommandManager(Plugin plugin) {
        this.plugin = plugin;
        CommandMap commandMap = null;
        try {
            Server server = Bukkit.getServer();
            Method getCommandMap = server.getClass().getDeclaredMethod("getCommandMap");
            getCommandMap.setAccessible(true);
            commandMap = (CommandMap) getCommandMap.invoke(server);
            Field knownCommands = commandMap.getClass().getDeclaredField("knownCommands");
            knownCommands.setAccessible(true);
            //noinspection unchecked
            this.knownCommands = (Map<String, Command>) knownCommands.get(commandMap);
        } catch (Exception e) {
            ACFLog.severe("Failed to get Command Map. ACF will not function.");
            ACFUtil.sneaky(e);
        }
        this.commandMap = commandMap;
        Bukkit.getPluginManager().registerEvents(new ACFBukkitListener(plugin), plugin);
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }

    public CommandMap getCommandMap() {
        return commandMap;
    }

    @Override
    public CommandContexts getCommandContexts() {
        if (this.contexts == null) {
            this.contexts = new BukkitCommandContexts();
        }
        return contexts;
    }

    @Override
    public CommandCompletions getCommandCompletions() {
        if (this.completions == null) {
            this.completions = new BukkitCommandCompletions();
        }
        return completions;
    }

    @Override
    public boolean registerCommand(BaseCommand command) {
        final String plugin = this.plugin.getName().toLowerCase();
        command.onRegister(this);
        boolean allSuccess = true;
        for (Map.Entry<String, Command> entry : command.registeredCommands.entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (!(commandMap.register(key, plugin, entry.getValue()))) {
                allSuccess = false;
            }
            registeredCommands.put(key, command);
        }

        return allSuccess;
    }

    public void unregisterCommand(BaseCommand command) {
        final String plugin = this.plugin.getName().toLowerCase();
        command.registeredCommands.entrySet().removeIf(entry -> {
            Command cmd = entry.getValue();
            cmd.unregister(commandMap);
            String key = entry.getKey();
            Command registered = knownCommands.get(key);
            if (registered == command) {
                knownCommands.remove(key);
            }
            knownCommands.remove(plugin + ":" + key);
            return true;
        });
    }

    public void unregisterCommands() {
        for (Map.Entry<String, BaseCommand> entry : registeredCommands.entrySet()) {
            unregisterCommand(entry.getValue());
        }
    }

    private class ACFBukkitListener implements Listener {
        private final Plugin plugin;

        public ACFBukkitListener(Plugin plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onPluginDisable(PluginDisableEvent event) {
            if (!(plugin.getName().equalsIgnoreCase(event.getPlugin().getName()))) {
                return;
            }
            unregisterCommands();
        }
    }
}
