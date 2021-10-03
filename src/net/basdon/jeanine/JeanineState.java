package net.basdon.jeanine;

import java.awt.Point;
import java.util.List;
import java.util.function.Consumer;

public class JeanineState
{
	public List<CodeGroup> codegroups;
	public CodeGroup activeGroup;
	public Point location;
	public Consumer<SB> lineSelectionListener;
}
