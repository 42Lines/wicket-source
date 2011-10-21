package net.simsa.sourceopener;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Limits the number of recent click events that are tracked. Provides limited access to the
 * underlying list. 
 * @author Jenny Brown
 *
 */
public class RecentEventsCache {
	private List<OpenEvent> recentEvents = Collections.synchronizedList(new LinkedList<OpenEvent>());
	private int maxSize;

	public RecentEventsCache()
	{
		maxSize = 6;
	}

	public int size()
	{
		return recentEvents.size();
	}

	public boolean isEmpty()
	{
		return recentEvents.isEmpty();
	}

	public Iterator<OpenEvent> iterator()
	{
		return recentEvents.iterator();
	}

	public Object[] toArray()
	{
		System.out.println("----");
		for (OpenEvent oe : recentEvents) {
			System.out.println(oe);
		}
		System.out.println("----");
		return recentEvents.toArray();
	}

	public void add(OpenEvent e)
	{
		recentEvents.add(e);
		if (recentEvents.size() > maxSize) {
			recentEvents.remove(0);
		}
	}

	public void clear()
	{
		recentEvents.clear();
	}

	public OpenEvent get(int index)
	{
		return recentEvents.get(index);
	}

	public void setMaxSize(int maxSize)
	{
		this.maxSize = maxSize;
	}

}
