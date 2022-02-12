package net.basdon.jeanine;

import java.util.ArrayList;

import static net.basdon.jeanine.TestRunner.*;

/**
 * Test both directions, so {@link RawToGroupConverter} and {@link GroupToRawConverter}.
 */
public class TestRawGroupConversion
{
	public static JeanineFrame jf;

	private ArrayList<SB> lines = new ArrayList<>();
	private RawToGroupConverter parser;
	private CodePanel p0, p1, p2, p3;

	public TestRawGroupConversion()
	{
		this.lines.add(new SB("hi/*jeanine:r:i:1;:s:a:r;i:2;:s:a:r;i:3;*/"));
		this.lines.add(new SB("/*jeanine:s:a:b;i:2;:s:a:t;i:1;*/"));
		this.lines.add(new SB("/*jeanine:p:i:1;p:0;a:r;*/"));
		this.lines.add(new SB("hi2"));
		this.lines.add(new SB("/*jeanine:p:i:2;p:1;a:b;x:-3.02;y:8.55;*/"));
		this.lines.add(new SB("hi3/*jeanine:s:a:r;i:1;*/"));
		this.lines.add(new SB("/*jeanine:p:i:3;p:2;a:b;x:4.11;y:3.00;*/"));
		this.lines.add(new SB("hi4"));
		this.lines.add(new SB("/*jeanine:s:a:b;i:2;*/"));
		this.parser = new RawToGroupConverter(jf.j, new CodeGroup(jf));
		this.parser.interpretSource(lines.iterator());
		this.p0 = this.parser.panels.get(Integer.valueOf(0));
		this.p1 = this.parser.panels.get(Integer.valueOf(1));
		this.p2 = this.parser.panels.get(Integer.valueOf(2));
		this.p3 = this.parser.panels.get(Integer.valueOf(3));
	}

	public void amount_of_panels()
	{
		assertEquals("wrong amount of panels", 4, parser.panels.size());
	}

	public void amount_of_lines_in_buffer()
	{
		assertEquals("wrong amount of lines in buffer", 4, parser.lines.size());
	}

	public void buffer_line_contents()
	{
		assertEquals("bad line 1", "hi", parser.lines.get(0).toString());
		assertEquals("bad line 2", "hi2", parser.lines.get(1).toString());
		assertEquals("bad line 3", "hi3", parser.lines.get(2).toString());
		assertEquals("bad line 4", "hi4", parser.lines.get(3).toString());
	}

	public void panel_ids()
	{
		// since p0 p1 p2 p3 are set by getting by id, testing for nullability is enough
		assertTrue("no panel with id 0", p0 != null);
		assertTrue("no panel with id 1", p1 != null);
		assertTrue("no panel with id 2", p2 != null);
		assertTrue("no panel with id 3", p3 != null);
	}

	public void p0_line_range()
	{
		assertEquals("bad firstline", 0, p0.firstline);
		assertEquals("bad lastline", 1, p0.lastline);
	}

	public void p1_line_range()
	{
		assertEquals("bad firstline", 1, p1.firstline);
		assertEquals("bad lastline", 2, p1.lastline);
	}

	public void p2_line_range()
	{
		assertEquals("bad firstline", 2, p2.firstline);
		assertEquals("bad lastline", 3, p2.lastline);
	}

	public void p3_line_range()
	{
		assertEquals("bad firstline", 3, p3.firstline);
		assertEquals("bad lastline", 4, p3.lastline);
	}

	public void p0_primary_link()
	{
		assertEquals("bad parent", null, p0.parent);
	}

	public void p1_primary_link()
	{
		assertEquals("bad parent", p0, p1.parent);
		assertEquals("bad anchor", PanelLink.createRightLink(0), p1.link);
		assertEquals("bad x", 0, p1.location.x, 0.01f);
		assertEquals("bad y", 0, p1.location.y, 0.01f);
	}

	public void p2_primary_link()
	{
		assertEquals("bad parent", p1, p2.parent);
		assertEquals("bad anchor", PanelLink.BOTTOM, p2.link);
		assertEquals("bad x", -3.01f, p2.location.x, 0.01f);
		assertEquals("bad y", 8.55f, p2.location.y, 0.01f);
	}

	public void p3_primary_link()
	{
		assertEquals("bad parent", p2, p3.parent);
		assertEquals("bad anchor", PanelLink.BOTTOM, p3.link);
		assertEquals("bad x", 4.11f, p3.location.x, 0.01f);
		assertEquals("bad y", 3.00f, p3.location.y, 0.01f);
	}

	public void p0_secondary_links()
	{
		assertEquals("bad amount", 4, p0.secondaryLinks.size());

		// the other of the links in the list shouldn't matter
		SecondaryLink s;

		s = p0.secondaryLinks.get(0);
		assertEquals("bad anchor", 'r', PanelLink.getAnchor(s.link));
		assertEquals("bad line", 0, PanelLink.getLine(s.link));
		assertEquals("bad child", p2.id, s.child.id);

		s = p0.secondaryLinks.get(1);
		assertEquals("bad anchor", 'r', PanelLink.getAnchor(s.link));
		assertEquals("bad line", 0, PanelLink.getLine(s.link));
		assertEquals("bad child", p3.id, s.child.id);

		s = p0.secondaryLinks.get(2);
		assertEquals("bad anchor", PanelLink.BOTTOM, s.link);
		assertEquals("bad child", p2.id, s.child.id);

		s = p0.secondaryLinks.get(3);
		assertEquals("bad anchor", PanelLink.TOP, s.link);
		assertEquals("bad child", p1.id, s.child.id);
	}

	public void p1_secondary_links()
	{
		assertEquals("bad amount", 0, p1.secondaryLinks.size());
	}

	public void p2_secondary_links()
	{
		assertEquals("bad amount", 1, p2.secondaryLinks.size());
		SecondaryLink s = p2.secondaryLinks.get(0);
		assertEquals("bad anchor", 'r', PanelLink.getAnchor(s.link));
		assertEquals("bad line", 2, PanelLink.getLine(s.link));
		assertEquals("bad child", p1.id, s.child.id);
	}

	public void p3_secondary_links()
	{
		assertEquals("bad amount", 1, p3.secondaryLinks.size());
		SecondaryLink s = p3.secondaryLinks.get(0);
		assertEquals("bad anchor", PanelLink.BOTTOM, s.link);
		assertEquals("bad child", p2.id, s.child.id);
	}

	public void serialize()
	{
		GroupToRawConverter c = new GroupToRawConverter(parser.lines, parser.panels, 0);
		for (int i = 0; i < lines.size(); i++) {
			assertTrue("missing line #" + i, c.hasNext());
			assertEquals("line #" + i, lines.get(i).toString(), c.next().toString());
		}
		assertTrue("more lines than expected", !c.hasNext());
	}

	public void serialize_parsed_carety()
	{
		GroupToRawConverter c = new GroupToRawConverter(parser.lines, parser.panels, 0);
		assertTrue("should have another line", c.hasNext());
		c.next();
		assertEquals("wrong parsed carety", 1, c.currentParsedCarety);
		assertTrue("should have another line", c.hasNext());
		c.next();
		assertEquals("wrong parsed carety", 1, c.currentParsedCarety);
		assertTrue("should have another line", c.hasNext());
		c.next();
		assertEquals("wrong parsed carety", 1, c.currentParsedCarety);
		assertTrue("should have another line", c.hasNext());
		c.next();
		assertEquals("wrong parsed carety", 2, c.currentParsedCarety);
		assertTrue("should have another line", c.hasNext());
		c.next();
		assertEquals("wrong parsed carety", 2, c.currentParsedCarety);
		assertTrue("should have another line", c.hasNext());
		c.next();
		assertEquals("wrong parsed carety", 3, c.currentParsedCarety);
		assertTrue("should have another line", c.hasNext());
		c.next();
		assertEquals("wrong parsed carety", 3, c.currentParsedCarety);
		assertTrue("should have another line", c.hasNext());
		c.next();
		assertEquals("wrong parsed carety", 4, c.currentParsedCarety);
		assertTrue("should have another line", c.hasNext());
		c.next();
		assertEquals("wrong parsed carety", 4, c.currentParsedCarety);
	}
}
