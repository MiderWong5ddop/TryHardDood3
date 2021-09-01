package hu.tryharddevs.advancedkits.kits.flags;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.contexts.ContextResolver;
import com.google.common.collect.Iterators;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static hu.tryharddevs.advancedkits.utils.localization.I18n.getMessage;

public abstract class Flag<T>
{
	private static final Pattern VALID_NAME = Pattern.compile("^[:A-Za-z0-9\\-]{1,40}$");
	private final String name;

	protected Flag(String name)
	{
		if (name != null && !isValidName(name)) {
			throw new IllegalArgumentException("Invalid flag name used");
		}
		this.name = name;
	}

	public static boolean isValidName(String name)
	{
		checkNotNull(name, "name");
		return VALID_NAME.matcher(name).matches();
	}

	public static ContextResolver<Flag> getContextResolver() {
		return (c) -> {
			String flagName = c.popFirstArg();
			Flag flag = DefaultFlags.fuzzyMatchFlag(flagName);
			if (Objects.isNull(flag)) {
				throw new InvalidCommandArgument(getMessage("flagNotFound"));
			}
			return flag;
		};
	}

	public final String getName()
	{
		return name;
	}

	@Nullable
	public T getDefault()
	{
		return null;
	}

	@Nullable
	public T chooseValue(Collection<T> values)
	{
		return Iterators.getNext(values.iterator(), null);
	}

	public abstract T parseInput(String input) throws InvalidFlagValueException;

	@Nullable
	public T parseItem(Player player) throws InvalidFlagValueException
	{
		return null;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "{" + "name='" + name + '\'' + '}';
	}

	public abstract T unmarshal(@Nullable Object o);

	public abstract Object marshal(T o);
}
