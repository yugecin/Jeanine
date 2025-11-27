package net.basdon.jeanine;

import static net.basdon.jeanine.TestRunner.*;

public class TestLine
{
	private SB sb = new SB(100);

	public void visual_to_logical_pos_around_tab_that_moves_5_chars()
	{
		sb.setLength(0).append("\t---\t---");
		assertEquals("bad result", 0, Line.visualToLogicalPos(sb, 0));
		assertEquals("bad result", 1, Line.visualToLogicalPos(sb, 1));
		assertEquals("bad result", 1, Line.visualToLogicalPos(sb, 2));
		assertEquals("bad result", 1, Line.visualToLogicalPos(sb, 3));
		assertEquals("bad result", 1, Line.visualToLogicalPos(sb, 4));
		assertEquals("bad result", 1, Line.visualToLogicalPos(sb, 5));
		assertEquals("bad result", 1, Line.visualToLogicalPos(sb, 6));
		assertEquals("bad result", 1, Line.visualToLogicalPos(sb, 7));
		assertEquals("bad result", 1, Line.visualToLogicalPos(sb, 8));
		assertEquals("bad result", 2, Line.visualToLogicalPos(sb, 9));
		assertEquals("bad result", 3, Line.visualToLogicalPos(sb, 10));
		assertEquals("bad result", 4, Line.visualToLogicalPos(sb, 11));
		assertEquals("bad result", 5, Line.visualToLogicalPos(sb, 12));
		assertEquals("bad result", 5, Line.visualToLogicalPos(sb, 13));
		assertEquals("bad result", 5, Line.visualToLogicalPos(sb, 14));
		assertEquals("bad result", 5, Line.visualToLogicalPos(sb, 15));
		assertEquals("bad result", 5, Line.visualToLogicalPos(sb, 16));
		assertEquals("bad result", 6, Line.visualToLogicalPos(sb, 17));
		assertEquals("bad result", 7, Line.visualToLogicalPos(sb, 18));
	}

	public void visual_to_logical_pos_when_line_only_has_one_tab()
	{
		sb.setLength(0).append("\t");
		assertEquals("bad result", 0, Line.visualToLogicalPos(sb, 0));
		assertEquals("bad result", 0, Line.visualToLogicalPos(sb, 1));
		assertEquals("bad result", 0, Line.visualToLogicalPos(sb, 2));
		assertEquals("bad result", 0, Line.visualToLogicalPos(sb, 3));
		assertEquals("bad result", 0, Line.visualToLogicalPos(sb, 4));
		assertEquals("bad result", 0, Line.visualToLogicalPos(sb, 5));
		assertEquals("bad result", 0, Line.visualToLogicalPos(sb, 6));
		assertEquals("bad result", 0, Line.visualToLogicalPos(sb, 7));
	}

	public void visual_to_logical_pos_when_line_only_has_two_tabs()
	{
		sb.setLength(0).append("\t\t");
		assertEquals("bad result", 0, Line.visualToLogicalPos(sb, 0));
		assertEquals("bad result", 1, Line.visualToLogicalPos(sb, 1));
		assertEquals("bad result", 1, Line.visualToLogicalPos(sb, 2));
		assertEquals("bad result", 1, Line.visualToLogicalPos(sb, 3));
		assertEquals("bad result", 1, Line.visualToLogicalPos(sb, 4));
		assertEquals("bad result", 1, Line.visualToLogicalPos(sb, 5));
		assertEquals("bad result", 1, Line.visualToLogicalPos(sb, 6));
		assertEquals("bad result", 1, Line.visualToLogicalPos(sb, 7));
		assertEquals("bad result", 1, Line.visualToLogicalPos(sb, 8));
		assertEquals("bad result", 1, Line.visualToLogicalPos(sb, 9));
		assertEquals("bad result", 1, Line.visualToLogicalPos(sb, 10));
		assertEquals("bad result", 1, Line.visualToLogicalPos(sb, 11));
		assertEquals("bad result", 1, Line.visualToLogicalPos(sb, 12));
		assertEquals("bad result", 1, Line.visualToLogicalPos(sb, 13));
		assertEquals("bad result", 1, Line.visualToLogicalPos(sb, 14));
		assertEquals("bad result", 1, Line.visualToLogicalPos(sb, 15));
	}
}
