package net.simsa.sourceopener.views;

public class UIRefreshViewMacro implements Runnable {
	
	private final RecentFilesView recentFilesView;
	
	
	/**
	 * This is the class to use when searching for the file failed, and we only want to refresh display.
	 * @param recentFilesView
	 * @param event
	 */
	public UIRefreshViewMacro(RecentFilesView recentFilesView)
	{
		this.recentFilesView = recentFilesView;
	}
	
	
	/**
	 * Does the UI work of opening the editor (if requested) and refreshing the most-recent-files list.
	 */
	public void run()
	{
		recentFilesView.viewer.refresh(false);
	}

	
}
