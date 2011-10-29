package net.simsa.sourceopener;

import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;

/**
 * Interruptable background process that searches for the specified file, while
 * enabling progress bar display on the associated View.
 * @author Jenny Brown
 *
 */
public final class SearchWithProgress extends Job {

	private static final String TASK_SEARCHING_FOR_FILE = "SourceOpener searching for file... ";
	private String packageName;
	private String fileName;
	private final PackageFileSearchRequester callback;


	public SearchWithProgress(PackageFileSearchRequester callback, String packageName, String fileName) {
		super(SearchWithProgress.TASK_SEARCHING_FOR_FILE);
		this.callback = callback;
		this.packageName = packageName;
		this.fileName = fileName;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) 
	{
		Logger log = Logger.getLogger("SearchWithProgress");
		log.info("Began search task's run method. ");
		try {
			monitor.beginTask(SearchWithProgress.TASK_SEARCHING_FOR_FILE,  IProgressMonitor.UNKNOWN);
			
			// Create search pattern
			String name = "";
			if ((this.packageName != null) && !("".equals(this.packageName.trim()))) {
				name += this.packageName + ".";
			}
			name += this.fileName;
			
			SearchPattern pattern = SearchPattern.createPattern(name, IJavaSearchConstants.TYPE,
					IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
			IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
			try {
				new SearchEngine().search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope, callback, monitor);
			} catch (CoreException ce) {
				callback.onError(ce);
			}
			return Status.OK_STATUS;
		} finally {
			monitor.done();
		}
	}
	
}