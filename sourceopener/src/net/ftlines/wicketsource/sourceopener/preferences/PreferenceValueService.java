package net.ftlines.wicketsource.sourceopener.preferences;

import java.util.logging.Logger;

import net.ftlines.wicketsource.sourceopener.Activator;

/**
 * Wraps the preferences settings for this plugin.
 * @author Jenny Brown
 *
 */
public class PreferenceValueService {

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

	public static int getKeepCount()
	{
		return Activator.getDefault().getPreferenceStore().getInt(PreferenceConstants.P_KEEP_COUNT);
	}

	public static boolean isStartListenerOnStartup()
	{
		return Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_START_ON_STARTUP);
	}

}
