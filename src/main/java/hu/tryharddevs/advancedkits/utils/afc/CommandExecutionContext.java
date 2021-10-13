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

import hu.tryharddevs.advancedkits.utils.afc.annotation.Default;
import hu.tryharddevs.advancedkits.utils.afc.annotation.Flags;
import hu.tryharddevs.advancedkits.utils.afc.annotation.Optional;
import hu.tryharddevs.advancedkits.utils.afc.contexts.ContextResolver;
import hu.tryharddevs.advancedkits.utils.afc.contexts.SenderAwareContextResolver;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.bukkit.command.CommandSender;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"WeakerAccess", "unused"})
public class CommandExecutionContext {
    private final RegisteredCommand cmd;
    private final Parameter param;
    private final CommandSender sender;
    private final List<String> args;
    private final int index;
    private final Map<String, Object> passedArgs;
    private final Map<String, String> flags;

    public CommandExecutionContext(RegisteredCommand cmd, Parameter param, CommandSender sender, List<String> args,
                                   int index, Map<String, Object> passedArgs) {
        this.cmd = cmd;
        this.param = param;
        this.sender = sender;
        this.args = args;
        this.index = index;
        this.passedArgs = passedArgs;
        Flags flags = param.getAnnotation(Flags.class);
        if (flags != null) {
            this.flags = Maps.newHashMap();
            for (String s : ACFPatterns.COMMA.split(flags.value())) {
                String[] v = ACFPatterns.EQUALS.split(s, 2);
                this.flags.put(v[0], v.length > 1 ? v[1] : null);
            }
        } else {
            this.flags = ImmutableMap.of();
        }
    }

    public String popFirstArg() {
        return !args.isEmpty() ? args.remove(0) : null;
    }

    public String getFirstArg() {
        return !args.isEmpty() ? args.get(0) : null;
    }

    public boolean isLastArg() {
        return cmd.parameters.length -1 == index;
    }

    public int getNumParams() {
        return cmd.parameters.length;
    }

    public boolean canOverridePlayerContext() {
        int numRequired = getNumParams();
        for (int i = 0; i < cmd.resolvers.length; i++) {
            Parameter parameter = cmd.parameters[i];
            ContextResolver<?> resolver = cmd.resolvers[i];
            if (parameter.getAnnotation(Optional.class) != null || parameter.getAnnotation(Default.class) != null) {
                numRequired--;
            } else if (resolver instanceof SenderAwareContextResolver) {
                numRequired--;
            }
        }

        return numRequired >= args.size();
    }

    public Object getResolvedArg(String arg) {
        return passedArgs.get(arg);
    }

    public Object getResolvedArg(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            for (Object passedArg : passedArgs.values()) {
                if (clazz.isInstance(passedArg)) {
                    return passedArg;
                }
            }
        }

        return null;
    }

    public <T> T getResolvedArg(String key, Class<?>... classes) {
        final Object o = passedArgs.get(key);
        for (Class<?> clazz : classes) {
            if (clazz.isInstance(o)) {
                //noinspection unchecked
                return (T) o;
            }
        }

        return null;
    }

    public boolean isOptional() {
        return param.getAnnotation(Optional.class) != null;
    }
    public boolean hasFlag(String flag) {
        return flags.containsKey(flag);
    }

    public String getFlagValue(String flag, String def) {
        return flags.getOrDefault(flag, def);
    }

    public Integer getFlagValue(String flag, Integer def) {
        return ACFUtil.parseInt(this.flags.get(flag), def);
    }

    public <T extends Annotation> T getAnnotation(Class<T> cls) {
        return param.getAnnotation(cls);
    }

    public <T extends Annotation> boolean hasAnnotation(Class<T> cls) {
        return param.getAnnotation(cls) != null;
    }

    public RegisteredCommand getCmd() {
        return this.cmd;
    }

    public Parameter getParam() {
        return this.param;
    }

    public CommandSender getSender() {
        return this.sender;
    }

    public List<String> getArgs() {
        return this.args;
    }

    public int getIndex() {
        return this.index;
    }

    public Map<String, Object> getPassedArgs() {
        return this.passedArgs;
    }

    public Map<String, String> getFlags() {
        return this.flags;
    }
}
