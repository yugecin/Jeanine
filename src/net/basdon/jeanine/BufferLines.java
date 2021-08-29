package net.basdon.jeanine;

import java.util.ArrayList;

public class BufferLines
{
	public final ArrayList<StringBuilder> lines;
	public final CodeGroup group;

	public BufferLines(CodeGroup group)
	{
		this.lines = new ArrayList<>();
		this.group = group;
	}

	public StringBuilder get(int idx)
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

	public void add(StringBuilder line)
	{
		this.lines.add(line);
		if (this.group != null) {
			this.group.beforeLineAdded(this.lines.size() - 1);
		}
	}

	public void add(int idx, StringBuilder line)
	{
		this.lines.add(idx, line);
		if (this.group != null) {
			this.group.beforeLineAdded(idx);
		}
	}

	public StringBuilder remove(int idx)
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
