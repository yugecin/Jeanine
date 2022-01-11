package net.basdon.jeanine;

import java.awt.Font;
import java.awt.Point;
import java.util.List;

public class JeanineState
{
	public List<CodeGroup> codegroups;
	public CodeGroup activeGroup;
	public Point location;
	public LineSelectionListener lineSelectionListener;
	public Runnable postStateLeaveListener;

	/**
	 * The font that was active when this state was pushed.
	 * To be set by {@link JeanineFrame#pushState}.
	 */
	public Font font;
	/**
	 * Cursor position (as reported by {@link JeanineFrame#findCursorPosition()}) when this
	 * state was pushed.
	 * To be set by {@link JeanineFrame#pushState}.
	 */
	public Point cursorPos;
}
