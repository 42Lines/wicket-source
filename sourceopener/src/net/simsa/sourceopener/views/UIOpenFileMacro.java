package net.simsa.sourceopener.views;

import net.simsa.sourceopener.OpenEvent;
import net.simsa.sourceopener.PackageFileSearchRequester;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * Opens a file in the editor, and jumps to the right line of that file. This
 * class is expected to be run by the Display thread.
 * 
 * @author Jenny Brown
 * 
 */
public final class UIOpenFileMacro implements Runnable {
	// Logger log = Logger.getLogger("UIOpenFileMacro");
	// java.util.logging.Logger

	private final RecentFilesView recentFilesView;
	private IPath fileToOpen;
	private int lineNumber;
	private OpenEvent event;

	/**
	 * This is the class to use when the IPath is valid and we expect to be able
	 * to open the file.
	 * 
	 * @param recentFilesView
	 * @param fileToOpen
	 * @param lineNumber
	 * @param event
	 */
	public UIOpenFileMacro(RecentFilesView recentFilesView, IPath fileToOpen, int lineNumber, OpenEvent event) {
		this.recentFilesView = recentFilesView;
		this.fileToOpen = fileToOpen;
		this.lineNumber = lineNumber;
		this.event = event;
	}

	/**
	 * Opens the file editor and jumps to the specified line.
	 */
	public void run()
	{
		try {
			openEditor();
		} catch (OpenFileException ofe) {
			event.setResultOfOpen(ofe);
		}
	}

	private void openEditor() throws OpenFileException
	{
		try {
			IFile ifile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(fileToOpen);
			if (ifile == null) {
				throw new OpenFileException(OpenFileException.Reason.FILE_NOT_FOUND);
			}
			FileEditorInput fileEditorInput = new FileEditorInput(ifile);
			AbstractTextEditor editor = openTextEditor(ifile, fileEditorInput);

			// Now go to the line number we care about.
			IDocument document = editor.getDocumentProvider().getDocument(fileEditorInput);
			if (document == null) {
				throw new OpenFileException(OpenFileException.Reason.EXCEPTION, new Exception(
						"IDocument was null in editor. How strange."));
			}
			// line count internally starts with 0, and not with 1 like in GUI
			IRegion lineInfo = document.getLineInformation(lineNumber - 1);
			if (lineInfo == null) {
				throw new OpenFileException(OpenFileException.Reason.FILE_OK_BUT_LINE_NOT_FOUND);
			}
			editor.selectAndReveal(lineInfo.getOffset(), 0);
			event.setResultOfOpenOk();
		} catch (BadLocationException e) {
			throw new OpenFileException(OpenFileException.Reason.FILE_OK_BUT_LINE_NOT_FOUND);
		} catch (PartInitException pie) {
			throw new OpenFileException(OpenFileException.Reason.EXCEPTION, pie);
		} catch (Throwable t) {
			throw new OpenFileException(OpenFileException.Reason.EXCEPTION, t);
		}
	}

	private AbstractTextEditor openTextEditor(IFile ifile, FileEditorInput fileEditorInput) throws PartInitException,
			OpenFileException
	{
		// First, open the specified file in an appropriate kind of editor.
		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(ifile.getName());
		IEditorPart editorPart = recentFilesView.getSite().getPage().openEditor(fileEditorInput, desc.getId());

		// This forced cast feels like a bit of a hack, but IEditorPart
		// doesn't have the method I need for getDocument.
		if (editorPart instanceof AbstractTextEditor) {
			return (AbstractTextEditor) editorPart;
		} else {
			throw new OpenFileException(OpenFileException.Reason.EXCEPTION, new IllegalArgumentException(
					"Not sure what to do with a non-text file editor."));
		}
	}

	static IPath searchForFile(OpenEvent event) throws OpenFileException
	{
		return searchForFile(event.getPackageName(), event.getFileName().replace(".java", ""));
	}

	static IPath searchForFile(String packageName, String fileName) throws OpenFileException
	{
		PackageFileSearchRequester searchFacade = new PackageFileSearchRequester(packageName, fileName);
		try {
			searchFacade.searchAndWait();
		} catch (CoreException core) {
			throw new OpenFileException(OpenFileException.Reason.EXCEPTION, core);
		}
		if (searchFacade.hasMultipleMatches()) {
			return searchFacade.firstMatch();
			// TODO: There are much nicer ways to handle this than guessing. How
			// about we ask the user?
		} else if (searchFacade.hasSingleMatch()) {
			return searchFacade.singleMatch();
		} else {
			throw new OpenFileException(OpenFileException.Reason.FILE_NOT_FOUND);
		}
	}

}