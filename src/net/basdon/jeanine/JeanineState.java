package net.basdon.jeanine;

import java.awt.Point;
import java.util.List;

public class JeanineState
{
	public List<CodeGroup> codegroups;
	public CodeGroup activeGroup;
	public Point location;
	public LineSelectionListener lineSelectionListener;
	public Runnable postStateLeaveListener;
}
