package net.simsa.sourceopener.views;

import java.io.IOException;

import net.simsa.sourceopener.Activator;
import net.simsa.sourceopener.IOpenEventListener;
import net.simsa.sourceopener.OpenEvent;

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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * This class provides a view tab showing files recently requested to be opened.
 * Double-clicking a recent file should re-open it to the specified location.
 * 
 * From the Eclipse plug-in wizard: The view shows data obtained from the model.
 * The sample creates a dummy model on the fly, but a real implementation would
 * connect to the model available either in this or another plug-in (e.g. the
 * workspace). The view is connected to the model using a content provider.
 * 
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 */

public class RecentFilesView extends ViewPart implements IOpenEventListener {
	// Logger log = Logger.getLogger("RecentFilesView"); // import
	// java.util.logging.Logger;

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "net.simsa.sourceopener.views.RecentFilesView";

	TableViewer viewer;
	private Action startSocketServer;
	private Action stopSocketServer;
	private Action doubleClickAction;

	/**
	 * Open the specified file in an editor and go to the line number. Then
	 * notify the viewer that the data in the model has changed and it should
	 * update. Parts of this must occur in the UI thread hence the use of
	 * syncExec.
	 */
	@Override
	public void onOpenEvent(OpenEvent event)
	{
		try {
			// Do the long running search process in the background, first. If
			// it succeeds, open the file in an editor.
			IPath file = UIOpenFileMacro.searchForFile(event);
			Display.getDefault().syncExec(new UIOpenFileMacro(this, file, event.getLineNumber(), event));
		} catch (OpenFileException ofe) {
			event.setResultOfOpen(ofe);
		}
		// And whether success or failure, always update the user's view so they
		// see any changes in error messages on the open event.
		Display.getDefault().syncExec(new Runnable() {
			public void run()
			{
				refreshView();
			}
		});
	}

	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */
	class ViewContentProvider implements IStructuredContentProvider {
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
	 * The constructor.
	 */
	public RecentFilesView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent)
	{
		// Single-select rows (not multi), scroll horizontal and vertical as needed, and highlight full row not just first column.
		viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION); 
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);

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

		TableViewerColumn[] columns = new TableViewerColumn[4];
		for (int i = 0; i < columnHeads.length; i++) {
			TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
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
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
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

		viewer.setContentProvider(new ViewContentProvider());
		viewer.setInput(getViewSite());

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "net.simsa.sourceopener.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		registerListener();
	}

	private void refreshView()
	{
		// rebuild the ui contents from the model (TODO: is there a better way?)
		viewer.setInput(getViewSite());
		// redraw the ui display
		viewer.refresh(false);
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
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars()
	{
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager)
	{
		manager.add(startSocketServer);
		manager.add(new Separator());
		manager.add(stopSocketServer);
	}

	private void fillContextMenu(IMenuManager manager)
	{
		manager.add(startSocketServer);
		manager.add(stopSocketServer);
		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager)
	{
		manager.add(startSocketServer);
		manager.add(stopSocketServer);
	}

	/**
	 * This provides the start/stop buttons for the listener, both as toolbar
	 * items and as right-click menu items.
	 */
	private void makeActions()
	{
		final ImageDescriptor IMAGE_START_ENABLED = Activator.getImageDescriptor("icons/greenplay.gif");
		final ImageDescriptor IMAGE_START_DISABLED = Activator.getImageDescriptor("icons/greenplay_disabled.gif");
		final ImageDescriptor IMAGE_STOP_ENABLED = PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_ELCL_STOP);
		final ImageDescriptor IMAGE_STOP_DISABLED = PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_ELCL_STOP_DISABLED);

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
		stopSocketServer.setEnabled(false);

		doubleClickAction = new Action() {
			public void run()
			{
				// Reopen the file when the line is double clicked.
				onOpenEvent((OpenEvent) (((IStructuredSelection) viewer.getSelection()).getFirstElement()));
			}
		};
	}

	private void hookDoubleClickAction()
	{
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event)
			{
				doubleClickAction.run();
			}
		});
	}

	void showMessage(String message)
	{
		MessageDialog.openInformation(viewer.getControl().getShell(), "Source Opener", message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus()
	{
		viewer.getControl().setFocus();
	}

}