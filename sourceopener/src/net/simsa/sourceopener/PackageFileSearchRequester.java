package net.simsa.sourceopener;

import java.util.ArrayList;
import java.util.List;

import net.simsa.sourceopener.views.OpenFileException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

public class PackageFileSearchRequester extends SearchRequestor {
	private String packageName;
	private String fileName;

	List<SearchMatch> list = new ArrayList<SearchMatch>();
	boolean complete = false;

	public PackageFileSearchRequester(String packageName, String fileName) {
		// IWorkspace workspace= ResourcesPlugin.getWorkspace();
		// IPath workspaceRoot = workspace.getRoot().getLocation();
		// IProject[] projects = workspace.getRoot().getProjects();

		this.packageName = packageName;
		this.fileName = fileName;
	}

	public void searchAndWait() throws CoreException
	{
		// Create search pattern
		String name = "";
		if ((packageName != null) && !("".equals(packageName.trim()))) {
			name += packageName + ".";
		}
		name += fileName;
		
		SearchPattern pattern = SearchPattern.createPattern(name, IJavaSearchConstants.TYPE,
				IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		SearchEngine searchEngine = new SearchEngine();
		searchEngine.search(pattern, new SearchParticipant[] { 
				SearchEngine.getDefaultSearchParticipant() 
		}, scope, this, null);
		
		while (!this.complete) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException ie) {
			}
		}
	}

	@Override
	public void acceptSearchMatch(SearchMatch match) throws CoreException
	{
		list.add(match);
	}

	public boolean hasMultipleMatches()
	{
		if (list.isEmpty()) {
			return false;
		}
		return list.size() > 1;
	}

	public boolean hasSingleMatch()
	{
		if (list.isEmpty()) {
			return false;
		}
		return list.size() == 1;
	}

	public IPath singleMatch()
	throws OpenFileException
	{
		if (list.isEmpty()) { 
			throw new OpenFileException(OpenFileException.Reason.FILE_NOT_FOUND);
		}
		if (list.size() > 1) {
			throw new OpenFileException(OpenFileException.Reason.TOO_MANY_MATCHES);
		}
		return firstMatch();
	}

	public IPath firstMatch()
	throws OpenFileException
	{
		if (list.isEmpty()) { 
			throw new OpenFileException(OpenFileException.Reason.FILE_NOT_FOUND);
		}
		SearchMatch match = list.get(0);
		IResource resource = match.getResource();
		return resource.getLocation();
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

}
