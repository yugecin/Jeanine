package net.basdon.jeanine;

import java.util.ArrayList;

public class BufferLines
{
	public final ArrayList<SB> lines;
	public final CodeGroup group;

	public BufferLines(CodeGroup group)
	{
		this.lines = new ArrayList<>();
		this.group = group;
	}

	public SB get(int idx)
	{
		return this.lines.get(idx);
	}

	/**
	 * Should not be used while editing, only on init
	 */
	public void clear()
	{
		this.lines.clear();
	}

	public void add(SB line)
	{
		this.lines.add(line);
		if (this.group != null) {
			this.group.beforeLineAdded(this.lines.size() - 1);
		}
	}

	public void add(int idx, SB line)
	{
		this.lines.add(idx, line);
		if (this.group != null) {
			this.group.beforeLineAdded(idx);
		}
	}

	public SB remove(int idx)
	{
		if (this.group != null) {
			this.group.beforeLineRemoved(idx);
		}
		return this.lines.remove(idx);
	}

	public int size()
	{
		return this.lines.size();
	}

	public boolean isEmpty()
	{
		return this.lines.isEmpty();
	}
}
