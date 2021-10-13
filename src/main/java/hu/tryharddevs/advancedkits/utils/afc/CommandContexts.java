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

import hu.tryharddevs.advancedkits.utils.afc.annotation.Single;
import hu.tryharddevs.advancedkits.utils.afc.annotation.Split;
import hu.tryharddevs.advancedkits.utils.afc.annotation.Values;
import hu.tryharddevs.advancedkits.utils.afc.contexts.ContextResolver;
import hu.tryharddevs.advancedkits.utils.afc.contexts.SenderAwareContextResolver;
import com.google.common.collect.Maps;
import org.bukkit.configuration.InvalidConfigurationException;

import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class CommandContexts {
    private final Map<Class<?>, ContextResolver<?>> contextMap = Maps.newHashMap();

    CommandContexts() {
        registerContext(Integer.class, (c) -> {
            try {
                return ACFUtil.parseNumber(c.popFirstArg(), c.hasFlag("suffixes")).intValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument("Must be a number");
            }
        });
        registerContext(Long.class, (c) -> {
            try {
                return ACFUtil.parseNumber(c.popFirstArg(), c.hasFlag("suffixes")).longValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument("Must be a number");
            }

        });
        registerContext(Float.class, (c) -> {
            try {
                return ACFUtil.parseNumber(c.popFirstArg(), c.hasFlag("suffixes")).floatValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument("Must be a number");
            }
        });
        registerContext(Double.class, (c) -> {
            try {
                return ACFUtil.parseNumber(c.popFirstArg(), c.hasFlag("suffixes")).doubleValue();
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument("Must be a number");
            }
        });
        registerContext(Number.class, (c) -> {
            try {
                return ACFUtil.parseNumber(c.popFirstArg(), c.hasFlag("suffixes"));
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument("Must be a number");
            }
        });
        registerContext(Boolean.class, (c) -> ACFUtil.isTruthy(c.popFirstArg()));
        registerContext(String.class, (c) -> {
            final Values values = c.getParam().getAnnotation(Values.class);
            if (values != null) {
                return c.popFirstArg();
            }
            String ret = (c.isLastArg() && c.getParam().getAnnotation(Single.class) == null) ?
                ACFUtil.join(c.getArgs())
                :
                c.popFirstArg();

            Integer minLen = c.getFlagValue("minlen", (Integer) null);
            Integer maxLen = c.getFlagValue("maxlen", (Integer) null);
            if (minLen != null) {
                if (ret.length() < minLen) {
                    throw new InvalidCommandArgument("Must be at least " + minLen + " characters long");
                }
            }
            if (maxLen != null) {
                if (ret.length() > maxLen) {
                    throw new InvalidCommandArgument("Must be less " + maxLen + " characters long");
                }
            }

            return ret;
        });
        registerContext(String[].class, (c) -> {
            String val;
            if (c.isLastArg() && c.getParam().getAnnotation(Single.class) == null) {
                val = ACFUtil.join(c.getArgs());
            } else {
                val = c.popFirstArg();
            }
            Split split = c.getParam().getAnnotation(Split.class);
            if (split != null) {
                if (val.isEmpty()) {
                    throw new InvalidCommandArgument();
                }
                return ACFPatterns.getPattern(split.value()).split(val);
            } else if (!c.isLastArg()) {
                ACFUtil.sneaky(new InvalidConfigurationException("Weird Command signature... String[] should be last or @Split"));
            }

            String[] result = c.getArgs().toArray(new String[c.getArgs().size()]);
            c.getArgs().clear();
            return result;
        });

        registerContext(Enum.class, (c) -> {
            final String first = c.popFirstArg();
            Class<? extends Enum<?>> enumCls = (Class<? extends Enum<?>>) c.getParam().getType();
            Enum<?> match = ACFUtil.simpleMatch(enumCls, first);
            if (match == null) {
                List<String> names = ACFUtil.enumNames(enumCls);
                throw new InvalidCommandArgument("Please specify one of: " + ACFUtil.join(names));
            }
            return match;
        });
    }

    public <T> void registerSenderAwareContext(Class<T> context, SenderAwareContextResolver<T> supplier) {
        contextMap.put(context, supplier);
    }
    public <T> void registerContext(Class<T> context, ContextResolver<T> supplier) {
        contextMap.put(context, supplier);
    }

    public ContextResolver<?> getResolver(Class<?> type) {
        Class<?> rootType = type;
        do {
            if (type == Object.class) {
                break;
            }

            final ContextResolver<?> resolver = contextMap.get(type);
            if (resolver != null) {
                return resolver;
            }
        } while ((type = type.getSuperclass()) != null);

        ACFLog.exception(new InvalidConfigurationException("No context resolver defined for " + rootType.getName()));
        return null;
    }
}
