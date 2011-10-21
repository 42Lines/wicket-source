package net.simsa.sourceopener.views;

import net.simsa.sourceopener.Activator;
import net.simsa.sourceopener.OpenEvent;
import net.simsa.sourceopener.PackageFileSearchRequester;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * Opens a file in the editor, and jumps to the right line of that file.
 * @author Jenny Brown
 *
 */
public final class UIOpenFileMacro implements Runnable {
	
	private final RecentFilesView recentFilesView;
	private IPath fileToOpen;
	private int lineNumber;
	private OpenEvent event;

	public UIOpenFileMacro(RecentFilesView recentFilesView, IPath fileToOpen, int lineNumber, OpenEvent event) {
		this.recentFilesView = recentFilesView;
		this.fileToOpen = fileToOpen;
		this.lineNumber = lineNumber;
		this.event = event;
	}

	public void run()
	{
		if (fileToOpen != null) { 
			openEditor();
		} else {
			event.setResultOfOpen("Could not find file in workspace.");
		}
		recentFilesView.viewer.refresh(false);
	}

	private void openEditor()
	{
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IFile ifile = workspace.getRoot().getFileForLocation(fileToOpen);
		if (ifile == null) {
			event.setResultOfOpen("Could not find file in workspace.");
			return;
		}
		try {
			// First, open the specified file in an appropriate kind of editor.
			IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(ifile.getName());
			IWorkbenchPage page = recentFilesView.getSite().getPage();
			FileEditorInput fileEditorInput = new FileEditorInput(ifile);
			
			// This forced cast is a bit of a hack, but IEditorPart doesn't have the method I need for getDocument.
			AbstractTextEditor editor = (AbstractTextEditor) page.openEditor(fileEditorInput, desc.getId());
			
			// Now go to the line number we care about. 
			IDocument document = editor.getDocumentProvider().getDocument(fileEditorInput);
			if (document != null) {
				try {
					// line count internaly starts with 0, and not with 1 like in GUI
					IRegion lineInfo = document.getLineInformation(lineNumber - 1);
					if (lineInfo != null) {
						editor.selectAndReveal(lineInfo.getOffset(), 0);
						event.setResultOfOpen("");
					} else {
						event.setResultOfOpen("Bad line number, ignoring. " + lineNumber);
					}
				} catch (BadLocationException e) {
					// ignored because line number may not really exist in document
					event.setResultOfOpen("Bad line number, ignoring. " + lineNumber);
				}
			} else {
				event.setResultOfOpen("Could not find file");
			}
			
		} catch (PartInitException pie) {
			pie.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	static IPath searchForFile(String packageName, String fileName, OpenEvent event)
	{
		PackageFileSearchRequester searchFacade = new PackageFileSearchRequester(packageName, fileName);
		try {
			searchFacade.searchAndWait();
		} catch (CoreException core) {
			event.setResultOfOpen("Exception while searching: " + core.toString());
			return null;
		}
		if (searchFacade.hasMultipleMatches()) {
			return searchFacade.firstMatch();
		} else {
			return searchFacade.singleMatch();
		}
	}

}