package net.ftlines.wicketsource.sourceopener.views;

import java.io.IOException;
import java.util.logging.Logger;

import net.ftlines.wicketsource.sourceopener.Activator;
import net.ftlines.wicketsource.sourceopener.IOpenEventListener;
import net.ftlines.wicketsource.sourceopener.OpenEvent;
import net.ftlines.wicketsource.sourceopener.PackageFileSearchRequester;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.part.ViewPart;

/**
 * This class provides a view tab showing files recently requested to be opened.
 * Double-clicking a recent file should re-open it to the specified location and
 * refresh any error messages based on success/fail.
 * 
 * In case multiple files match the package+class, it will prompt the user with
 * a forced-choice dialog to choose just one match (or if they cancel, it will
 * mark the event with an error message accordingly).
 * 
 * Start/Stop buttons are provided for the http socket listener, and a clear all
 * events button for cleaning up the display.
 * 
 * General information from the Eclipse plug-in wizard: The view shows data
 * obtained from the model. The sample creates a dummy model on the fly, but a
 * real implementation would connect to the model available either in this or
 * another plug-in (e.g. the workspace). The view is connected to the model
 * using a content provider.
 * 
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * 
 * Tutorial on plugins here:
 * http://www.vogella.de/articles/EclipsePlugIn/article.html
 */

public class RecentFilesView extends ViewPart implements IOpenEventListener {
	Logger log = Logger.getLogger("RecentFilesView");

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "net.ftlines.wicketsource.sourceopener.views.RecentFilesView";

	private TableViewer eventTableViewer;
	private Action startSocketServer;
	private Action stopSocketServer;
	private Action doubleClickAction;
	private Action clearEventsAction;

	// ---- begin things that participate in dispose ----
	final private Image openEventImage = PlatformUI.getWorkbench().getSharedImages()
			.getImage(ISharedImages.IMG_OBJ_FILE);
	// ---- end things that participate in dispose ----

	// ---- begin indirect references
	// ---- these are references that can be used to create graphics, but since
	// ---- I'm not the one calling for construction, I shouldn't dispose them.
	final private ImageDescriptor IMAGE_CLEAR = Activator.getImageDescriptor("icons/clear.gif");
	final private ImageDescriptor IMAGE_STOP_ENABLED = PlatformUI.getWorkbench().getSharedImages()
			.getImageDescriptor(ISharedImages.IMG_ELCL_STOP);
	final private ImageDescriptor IMAGE_STOP_DISABLED = PlatformUI.getWorkbench().getSharedImages()
			.getImageDescriptor(ISharedImages.IMG_ELCL_STOP_DISABLED);
	final private ImageDescriptor IMAGE_START_ENABLED = Activator.getImageDescriptor("icons/greenplay.gif");
	final private ImageDescriptor IMAGE_START_DISABLED = Activator.getImageDescriptor("icons/greenplay_disabled.gif");

	// ---- end indirect references

	@Override
	public void dispose()
	{
		super.dispose();
		openEventImage.dispose();
	}

	/**
	 * The constructor.
	 */
	public RecentFilesView() {
	}

	/**
	 * Open the specified file in an editor and go to the line number. Then
	 * notify the eventTableViewer that the data in the model has changed and it
	 * should update. This must occur in the UI thread hence the use of
	 * syncExec.
	 * 
	 * If more than one file matches during search, a UI dialog will appear
	 * asking the user to choose one of the matches. If they cancel, the file
	 * will not open and the event will received an error message saying that
	 * the user cancelled open. If they choose a file and press Ok, the file
	 * should open in an editor.
	 * 
	 * Regardless of success/fail on search, always redisplay the view so that
	 * the user can see any updated error messages in the event table.
	 * 
	 */
	@Override
	public void onOpenEvent(final OpenEvent event)
	{
		event.reset();
		final RecentFilesView view = this;

		Display.getDefault().syncExec(new Runnable() {
			public void run()
			{
				view.identifyFileAndDisplay(event);
				view.refreshOpenEventTable();
			}
		});
	}

	/**
	 * This expects to be run from the Display thread.
	 * 
	 * @param event
	 */
	private void identifyFileAndDisplay(final OpenEvent event)
	{
		try {
			// Look for the file requested by open event.
			IPath[] matches = searchForFile(event);
			if (matches.length == 0) {
				event.setResultOfOpen(new OpenFileException(OpenFileException.Reason.FILE_NOT_FOUND));
				return;
			}
			if (matches.length == 1) {
				event.setFile(matches[0]);
				new UIEditorFileOpener(this, event).run();
				return;
			}

			// Multiple choices, so display a dialog and have the user pick one.
			ElementListSelectionDialog dialog = new ElementListSelectionDialog(this.getViewSite().getShell(),
					new LabelProvider());
			dialog.setTitle("File Selection - SourceOpener");
			dialog.setMessage("Multiple matches. Choose One (* = any string, ? = any char):");
			dialog.setElements(matches);
			dialog.setBlockOnOpen(true);
			dialog.open();
			Object[] choices = dialog.getResult();

			if ((dialog.getReturnCode() == Window.CANCEL) || (choices.length < 1)) {
				event.setResultOfOpen("Multiple matches - user cancelled selection");
				event.setFile(null);
				return;
			}
			event.setFile((IPath) choices[0]);
			event.setResultOfOpenOk();
			new UIEditorFileOpener(this, event).run();
			
		} catch (OpenFileException ofe) {
			event.setResultOfOpen(ofe);
		}
	}

	/**
	 * This expects to be run from the display thread.
	 */
	private void refreshOpenEventTable()
	{
		// rebuild the ui contents from the model (TODO: is there a better way?)
		eventTableViewer.setInput(getViewSite());
		// redraw the ui display
		eventTableViewer.refresh(false);
	}

	private IPath[] searchForFile(OpenEvent event) throws OpenFileException
	{
		PackageFileSearchRequester searchFacade = new PackageFileSearchRequester(event.getPackageName(), event.getFileName().replace(".java", ""));
		searchFacade.searchAndWait(this);
		return searchFacade.allMatches();
	}

	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */
	private class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput)
		{
		}

		public void dispose()
		{
		}

		public Object[] getElements(Object parent)
		{
			return Activator.getDefault().getHttpService().getEventCache().toArray();
		}
	}

	/**
	 * This is a callback that will allow us to create the eventTableViewer and
	 * initialize it.
	 */
	public void createPartControl(Composite parent)
	{
		// Logger log = Logger.getLogger("RecentFilesView");

		// Tips on JFace and tables:
		// http://www.vogella.de/articles/EclipseJFaceTable/article.html
		// http://www.vogella.de/articles/EclipseJFaceTableAdvanced/article.html

		// Single-select rows (not multi), scroll horizontal and vertical as
		// needed, and highlight full row not just first column.
		eventTableViewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		eventTableViewer.getTable().setHeaderVisible(true);
		createEventTableColumns();
		eventTableViewer.setContentProvider(new ViewContentProvider());
		eventTableViewer.setInput(getViewSite());

		// Create the help context id for the eventTableViewer's control
		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(eventTableViewer.getControl(), "net.ftlines.wicketsource.sourceopener.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		registerListener();
	}

	private void createEventTableColumns()
	{
		int[] columnWidth = new int[4];
		String[] columnHeads = new String[4];
		columnHeads[0] = "Class";
		columnWidth[0] = 190;
		columnHeads[1] = "Line";
		columnWidth[1] = 60;
		columnHeads[2] = "Package";
		columnWidth[2] = 190;
		columnHeads[3] = "Messages";
		columnWidth[3] = 300;

		// Presuming this takes care of the table, its columns, and cells.
		// "When a Composite, is disposed, the composite and all of its child
		// widgets are recursively disposed. In this case, you do not need to
		// dispose of the widgets themselves."

		TableViewerColumn[] columns = new TableViewerColumn[4];
		for (int i = 0; i < columnHeads.length; i++) {
			TableViewerColumn viewerColumn = new TableViewerColumn(eventTableViewer, SWT.NONE);
			TableColumn tableColumn = viewerColumn.getColumn();
			tableColumn.setText(columnHeads[i]);
			tableColumn.setResizable(true);
			tableColumn.setMoveable(true);
			tableColumn.pack();
			tableColumn.setWidth(columnWidth[i]);
			columns[i] = viewerColumn;
		}

		columns[0].setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element)
			{
				OpenEvent event = (OpenEvent) element;
				return event.getFileName();
			}

			@Override
			public Image getImage(Object element)
			{

				return openEventImage;
			}

			@Override
			public void update(ViewerCell cell)
			{
				super.update(cell);
			}

		});
		columns[1].setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element)
			{
				OpenEvent event = (OpenEvent) element;
				return event.getLineNumber() + "";
			}
		});
		columns[2].setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element)
			{
				OpenEvent event = (OpenEvent) element;
				return event.getPackageName();
			}
		});
		columns[3].setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element)
			{
				OpenEvent event = (OpenEvent) element;
				return event.getResultOfOpen();
			}

			@Override
			public void update(ViewerCell cell)
			{
				cell.setText(((OpenEvent) cell.getElement()).getResultOfOpen());
			}
		});
	}

	private void registerListener()
	{
		Activator.getDefault().getHttpService().registerListener(this);
	}

	private void hookContextMenu()
	{
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager)
			{
				RecentFilesView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(eventTableViewer.getControl());
		eventTableViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, eventTableViewer);
	}

	private void contributeToActionBars()
	{
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager)
	{
		manager.add(clearEventsAction);
		manager.add(startSocketServer);
		manager.add(stopSocketServer);
		manager.add(new Separator());
	}

	private void fillContextMenu(IMenuManager manager)
	{
		manager.add(clearEventsAction);
		manager.add(startSocketServer);
		manager.add(stopSocketServer);
		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager)
	{
		manager.add(clearEventsAction);
		manager.add(startSocketServer);
		manager.add(stopSocketServer);
	}

	/**
	 * This provides the start/stop buttons for the listener, both as toolbar
	 * items and as right-click menu items.
	 */
	private void makeActions()
	{
		createActionStartSocketServer();
		createActionStopSocketServer();
		createActionDoubleClickReopens();
		createActionClearEvents();
	}

	private void createActionClearEvents()
	{
		clearEventsAction = new Action() {
			public void run()
			{
				Activator.getDefault().getHttpService().getEventCache().clear();
				refreshOpenEventTable();
			}
		};
		clearEventsAction.setText("Clear History");
		clearEventsAction.setToolTipText("Clears the list of recently opened files");
		clearEventsAction.setImageDescriptor(IMAGE_CLEAR);
	}

	private void createActionDoubleClickReopens()
	{
		doubleClickAction = new Action() {
			public void run()
			{
				// Reopen the file when the line is double clicked.
				onOpenEvent((OpenEvent) (((IStructuredSelection) eventTableViewer.getSelection()).getFirstElement()));
			}
		};
	}

	private void createActionStopSocketServer()
	{
		stopSocketServer = new Action() {
			public void run()
			{
				Activator.getDefault().getHttpService().stop();
				this.setEnabled(false);
				startSocketServer.setEnabled(true);
			}
		};
		stopSocketServer.setText("Stop Listener");
		stopSocketServer.setToolTipText("Stops the listener that receives browser file-open clicks");
		stopSocketServer.setImageDescriptor(IMAGE_STOP_ENABLED);
		stopSocketServer.setDisabledImageDescriptor(IMAGE_STOP_DISABLED);
		stopSocketServer.setEnabled(Activator.getDefault().getHttpService().isRunning());
	}

	private void createActionStartSocketServer()
	{
		startSocketServer = new Action() {
			public void run()
			{
				try {
					Activator.getDefault().getHttpService().start();
					this.setEnabled(false);
					stopSocketServer.setEnabled(true);
				} catch (IOException io) {
					showMessage("Start listener: Exception while starting: " + io.toString());
				}
			}
		};
		startSocketServer.setText("Start Listener");
		startSocketServer.setToolTipText("Starts the listener to receive browser file-open clicks");
		startSocketServer.setImageDescriptor(IMAGE_START_ENABLED);
		startSocketServer.setDisabledImageDescriptor(IMAGE_START_DISABLED);
		startSocketServer.setEnabled(!Activator.getDefault().getHttpService().isRunning());
	}

	private void hookDoubleClickAction()
	{
		eventTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event)
			{
				doubleClickAction.run();
			}
		});
	}

	void showMessage(String message)
	{
		MessageDialog.openInformation(eventTableViewer.getControl().getShell(), "Source Opener", message);
	}

	/**
	 * Passing the focus request to the eventTableViewer's control.
	 */
	public void setFocus()
	{
		eventTableViewer.getControl().setFocus();
	}

}