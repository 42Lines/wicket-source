package net.simsa.sourceopener;

import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;

public final class FileSearchProgressMonitor implements IProgressMonitor {
	Logger log = Logger.getLogger("FileSearchProgressMonitor");
	
	boolean cancelled = false;
	double sum;
	
	@Override
	public void beginTask(String taskName, int completionNumber)
	{
		log.info("Begin task : " + taskName + " " + completionNumber);
		cancelled = false;
	}

	@Override
	public void done()
	{
		log.info("Done");
	}

	@Override
	public void internalWorked(double amountWorked)
	{
		log.info("internalWorked: " + amountWorked);
		sum += amountWorked;
	}

	@Override
	public boolean isCanceled()
	{
		log.info("Checked whether it's been cancelled ");
		return cancelled;
	}

	@Override
	public void setCanceled(boolean cancelled)
	{
		log.info("setCancelled " + cancelled);
		this.cancelled = cancelled;
	}

	@Override
	public void setTaskName(String taskName)
	{
		log.info("Task name " + taskName);
	}

	@Override
	public void subTask(String subtaskName)
	{
		log.info("Subtask " + subtaskName);
	}

	@Override
	public void worked(int amountWorked)
	{
		log.info("Worked " + amountWorked);
	}
}