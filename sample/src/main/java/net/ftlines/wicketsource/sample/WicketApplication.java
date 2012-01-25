package net.ftlines.wicketsource.sample;

import org.apache.wicket.protocol.http.WebApplication;

import net.ftlines.wicketsource.WicketSource;

/**
 * Application object for your web application. If you want to run this application without deploying, run the Start class.
 * 
 * @see net.ftlines.wicketsource.sample.sample.Start#main(String[])
 */
public class WicketApplication extends WebApplication
{    	
	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<HomePage> getHomePage()
	{
		return HomePage.class;
	}

	/**
	 * @see org.apache.wicket.Application#init()
	 */
	@Override
	public void init()
	{
		super.init();

		WicketSource.configure(this);
	}
}
