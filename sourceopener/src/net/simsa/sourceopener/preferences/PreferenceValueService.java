package net.simsa.sourceopener.preferences;

import java.util.logging.Logger;

import net.simsa.sourceopener.Activator;

/**
 * Wraps the preferences settings for this plugin.
 * @author Jenny Brown
 *
 */
public class PreferenceValueService {
	static Logger log = Logger.getLogger("PreferenceValueService");

	public static int getPort()
	{
		return Activator.getDefault().getSecurePreferenceStore().getInt(PreferenceConstants.P_PORT);
	}

	public static String getPassword()
	{
		return Activator.getDefault().getSecurePreferenceStore().getString(PreferenceConstants.P_PASSWORD);
	}

	public static boolean isUsePassword()
	{
		return Activator.getDefault().getSecurePreferenceStore().getBoolean(PreferenceConstants.P_USEPASSWORD);
	}

	
}
