package net.ftlines.wicketsource.sourceopener;

import java.util.logging.Logger;

import net.ftlines.wicketsource.sourceopener.preferences.PreferenceValueService;

import org.eclipse.ui.IStartup;

public class Startup implements IStartup {
	Logger log = Logger.getLogger("Startup");
	
	@Override
	public void earlyStartup()
	{
		if (PreferenceValueService.isStartListenerOnStartup()) {
			// don't have to do anything, just be registered.
		}
	}

}
