package hu.tryharddevs.advancedkits.utils;

import hu.tryharddevs.advancedkits.AdvancedKitsMain;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultUtil
{
	private static AdvancedKitsMain instance = AdvancedKitsMain.advancedKits;
	private static Economy          econ     = null;

	public static void loadVault()
	{
		if (!setupEconomy()) {
			instance.log(ChatColor.RED + String.format("[%s] - Disabled due to no Vault dependency found!", instance.getDescription().getName()));
			instance.getServer().getPluginManager().disablePlugin(instance);
			return;
		}
	}

	private static boolean setupEconomy()
	{
		if (instance.getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = instance.getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	public static Economy getEconomy()
	{
		return econ;
	}
}
