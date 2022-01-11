package net.basdon.jeanine;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test both directions, so {@link RawToGroupConverter} and {@link GroupToRawConverter}.
 */
public class RawGroupConversionTest
{
	private static ArrayList<SB> lines = new ArrayList<>();
	private static RawToGroupConverter parser;
	private static CodePanel p0, p1, p2, p3;
	private static Jeanine j;

	static
	{
		lines.add(new SB("hi/*jeanine:r:i:1;:s:a:r;i:2;:s:a:r;i:3;*/"));
		lines.add(new SB("/*jeanine:s:a:b;i:2;:s:a:t;i:1;*/"));
		lines.add(new SB("/*jeanine:p:i:1;p:0;a:r;m:6;*/"));
		lines.add(new SB("hi2"));
		lines.add(new SB("/*jeanine:p:i:2;p:1;a:b;x:-3;y:8;m:-45;n:19;*/"));
		lines.add(new SB("hi3/*jeanine:s:a:r;i:1;*/"));
		lines.add(new SB("/*jeanine:p:i:3;p:2;a:b;x:4;y:3;m:48;n:11;*/"));
		lines.add(new SB("hi4"));
		lines.add(new SB("/*jeanine:s:a:b;i:2;*/"));
		j = new Jeanine();
		System.clearProperty(Preferences.FILENAME_PROPERTY);
		Preferences.load(j);
		JeanineFrame jf = new JeanineFrame(j);
		// font sizes are not set (because no paint happens?)
		j.fx = 5;
		j.fy = 10;
		parser = new RawToGroupConverter(j, new CodeGroup(jf));
		parser.interpretSource(lines.iterator());
		p0 = parser.panels.get(Integer.valueOf(0));
		p1 = parser.panels.get(Integer.valueOf(1));
		p2 = parser.panels.get(Integer.valueOf(2));
		p3 = parser.panels.get(Integer.valueOf(3));
	}

	@Test
	public void amount_of_panels()
	{
		assertEquals(4, parser.panels.size());
	}

	@Test
	public void amount_of_lines_in_buffer()
	{
		assertEquals(4, parser.lines.size());
	}

	@Test
	public void buffer_line_contents()
	{
		assertEquals("hi", parser.lines.get(0).toString());
		assertEquals("hi2", parser.lines.get(1).toString());
		assertEquals("hi3", parser.lines.get(2).toString());
		assertEquals("hi4", parser.lines.get(3).toString());
	}

	@Test
	public void panel_ids()
	{
		// since p0 p1 p2 p3 are set by getting by id, testing for nullability is enough
		assertNotNull(p0);
		assertNotNull(p1);
		assertNotNull(p2);
		assertNotNull(p3);
	}

	@Test
	public void p0_line_range()
	{
		assertEquals("bad firstline", 0, p0.firstline);
		assertEquals("bad lastline", 1, p0.lastline);
	}

	@Test
	public void p1_line_range()
	{
		assertEquals("bad firstline", 1, p1.firstline);
		assertEquals("bad lastline", 2, p1.lastline);
	}

	@Test
	public void p2_line_range()
	{
		assertEquals("bad firstline", 2, p2.firstline);
		assertEquals("bad lastline", 3, p2.lastline);
	}

	@Test
	public void p3_line_range()
	{
		assertEquals("bad firstline", 3, p3.firstline);
		assertEquals("bad lastline", 4, p3.lastline);
	}

	@Test
	public void p0_primary_link()
	{
		assertEquals("bad parent", null, p0.parent);
	}

	@Test
	public void p1_primary_link()
	{
		assertEquals("bad parent", p0, p1.parent);
		assertEquals("bad anchor", PanelLink.createRightLink(0), p1.link);
		assertEquals("bad x", 0, p1.locationXY.x);
		assertEquals("bad y", 0, p1.locationXY.y);
		assertEquals("bad m", 6, p1.locationMN.x);
		assertEquals("bad n", 0, p1.locationMN.y);
	}

	@Test
	public void p2_primary_link()
	{
		assertEquals("bad parent", p1, p2.parent);
		assertEquals("bad anchor", PanelLink.BOTTOM, p2.link);
		assertEquals("bad x", -3, p2.locationXY.x);
		assertEquals("bad y", 8, p2.locationXY.y);
		assertEquals("bad m", -45, p2.locationMN.x);
		assertEquals("bad n", 19, p2.locationMN.y);
	}

	@Test
	public void p3_primary_link()
	{
		assertEquals("bad parent", p2, p3.parent);
		assertEquals("bad anchor", PanelLink.BOTTOM, p3.link);
		assertEquals("bad x", 4, p3.locationXY.x);
		assertEquals("bad y", 3, p3.locationXY.y);
		assertEquals("bad m", 48, p3.locationMN.x);
		assertEquals("bad n", 11, p3.locationMN.y);
	}

	@Test
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

	@Test
	public void p1_secondary_links()
	{
		assertEquals("bad amount", 0, p1.secondaryLinks.size());
	}

	@Test
	public void p2_secondary_links()
	{
		assertEquals("bad amount", 1, p2.secondaryLinks.size());
		SecondaryLink s = p2.secondaryLinks.get(0);
		assertEquals("bad anchor", 'r', PanelLink.getAnchor(s.link));
		assertEquals("bad line", 2, PanelLink.getLine(s.link));
		assertEquals("bad child", p1.id, s.child.id);
	}

	@Test
	public void p3_secondary_links()
	{
		assertEquals("bad amount", 1, p3.secondaryLinks.size());
		SecondaryLink s = p3.secondaryLinks.get(0);
		assertEquals("bad anchor", PanelLink.BOTTOM, s.link);
		assertEquals("bad child", p2.id, s.child.id);
	}

	@Test
	public void serialize()
	{
		GroupToRawConverter c = new GroupToRawConverter(parser.lines, parser.panels, 0);
		for (int i = 0; i < lines.size(); i++) {
			assertTrue("missing line #" + i, c.hasNext());
			assertEquals("line #" + i, lines.get(i).toString(), c.next().toString());
		}
		assertFalse("more lines than expected", c.hasNext());
	}
}
