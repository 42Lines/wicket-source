package net.ftlines.wicketsource.sourceopener;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.ftlines.wicketsource.sourceopener.views.OpenFileException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.ui.part.ViewPart;

public class PackageFileSearchRequester extends SearchRequestor implements IExceptionCallbackHandler {

	private String packageName;
	private String fileName;

	List<SearchMatch> list = new ArrayList<SearchMatch>();
	boolean complete = false;
	boolean cancelled = false;
	IProgressMonitor progressMonitor;
	Exception exceptionWhileSearching;
	Logger log = Logger.getLogger("PackageFileSearchRequester");

	public PackageFileSearchRequester(String packageName, String fileName) {
		// IWorkspace workspace= ResourcesPlugin.getWorkspace();
		// IPath workspaceRoot = workspace.getRoot().getLocation();
		// IProject[] projects = workspace.getRoot().getProjects();

		this.packageName = packageName;
		this.fileName = fileName;
		progressMonitor = new FileSearchProgressMonitor();
	}

	public void searchAndWait(ViewPart viewPart) throws OpenFileException
	{
		exceptionWhileSearching = null;
		search();
		waitForCompletion();
		if (exceptionWhileSearching != null) {
			throw new OpenFileException(OpenFileException.Reason.EXCEPTION, exceptionWhileSearching);
		}
	}
	private void waitForCompletion()
	{
		// wait for completion kills the display thread, but what else should I do? TODO:FIXME 
		while (!this.complete) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ie) {
			}
			if (cancelled) {
				progressMonitor.setCanceled(true);
				break;
			}
		}
	}

	private void search()
	{
		Job job = new SearchWithProgress(this, packageName, fileName);
		job.setUser(true);
		job.schedule();
	}

	public boolean isCancelled()
	{
		return cancelled;
	}

	public void setCancelled(boolean cancelled)
	{
		this.cancelled = cancelled;
		progressMonitor.setCanceled(cancelled);
	}

	@Override
	public void acceptSearchMatch(SearchMatch match) throws CoreException
	{
		list.add(match);
	}

	public IPath[] allMatches()
	throws OpenFileException
	{
		if (list.isEmpty()) { 
			throw new OpenFileException(OpenFileException.Reason.FILE_NOT_FOUND);
		}
		IPath[] matches = new IPath[list.size()];
		for (int i = 0; i < list.size(); i++) {
			SearchMatch match = list.get(i);
			IResource resource = match.getResource();
			matches[i] = resource.getLocation();		
		}
		return matches;
	}

	@Override
	public void beginReporting()
	{
		complete = false;
		list.clear();
	}

	@Override
	public void endReporting()
	{
		complete = true;
	}

	@Override
	public void onError(Exception e)
	{
		exceptionWhileSearching = e;
		Logger log = Logger.getLogger("SearchWithProgress");
		log.info("Problem with search engine : " + e);
	}
	
}
