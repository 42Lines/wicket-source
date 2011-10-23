package net.simsa.sourceopener.views;

import net.simsa.sourceopener.Activator;
import net.simsa.sourceopener.preferences.PreferenceConstants;

/**
 * Wraps the preferences settings for this plugin.
 * @author Jenny Brown
 *
 */
public class ConfigurationService {

	public static int getPort()
	{
		return Activator.getDefault().getPreferenceStore().getInt(PreferenceConstants.P_PORT);
	}

	public static String getPassword()
	{
		return Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_PASSWORD);
	}

	public static boolean isUsePassword()
	{
		return Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_USEPASSWORD);
	}

	
}
