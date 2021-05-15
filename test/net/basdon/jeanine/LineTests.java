package net.basdon.jeanine;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class LineTests
{
	public static class Construct
	{
		@Test
		public void construct()
		{
			Line line = new Line("abcd");

			assertEquals(1, line.segments.size());
			assertEquals(4, line.segments.get(0).visualLength);
			assertEquals("abcd", line.segments.get(0).text.toString());
		}

		@Test
		public void construct_with_ending_tab()
		{
			Line line = new Line("abcd\t");

			assertEquals(2, line.segments.size());
			assertTrue(!line.segments.get(0).isTab());
			assertTrue(line.segments.get(1).isTab());
			assertEquals(4, line.segments.get(0).visualLength);
			assertEquals(4, line.segments.get(1).visualLength);
			assertEquals("abcd", line.segments.get(0).text.toString());
		}
	}

	public static class Insert
	{
		@Test
		public void new_text()
		{
			Line line = new Line();

			line.insert(0, "abc");

			assertEquals(1, line.segments.size());
			assertEquals(3, line.segments.get(0).visualLength);
			assertEquals("abc", line.segments.get(0).text.toString());
		}

		@Test
		public void new_text_starting_tab_aligned()
		{
			Line line = new Line();

			line.insert(0, "\tabcdefgh");

			assertEquals(2, line.segments.size());
			assertTrue(line.segments.get(0).isTab());
			assertTrue(!line.segments.get(1).isTab());
			assertEquals(8, line.segments.get(0).visualLength);
			assertEquals(8, line.segments.get(1).visualLength);
			assertEquals("abcdefgh", line.segments.get(1).text.toString());
		}

		@Test
		public void new_text_ending_tab_aligned()
		{
			Line line = new Line();

			line.insert(0, "abcdefgh\t");

			assertEquals(2, line.segments.size());
			assertTrue(!line.segments.get(0).isTab());
			assertTrue(line.segments.get(1).isTab());
			assertEquals(8, line.segments.get(0).visualLength);
			assertEquals(8, line.segments.get(1).visualLength);
			assertEquals("abcdefgh", line.segments.get(0).text.toString());
		}

		@Test
		public void new_text_ending_tab_misaligned()
		{
			Line line = new Line();

			line.insert(0, "abcdef\t");

			assertEquals(2, line.segments.size());
			assertTrue(!line.segments.get(0).isTab());
			assertTrue(line.segments.get(1).isTab());
			assertEquals(6, line.segments.get(0).visualLength);
			assertEquals(2, line.segments.get(1).visualLength);
			assertEquals("abcdef", line.segments.get(0).text.toString());
		}

		@Test
		public void new_text_with_tab_aligned()
		{
			Line line = new Line();

			line.insert(0, "abcdefgh\t01234567");

			assertEquals(3, line.segments.size());
			assertTrue(!line.segments.get(0).isTab());
			assertTrue(line.segments.get(1).isTab());
			assertTrue(!line.segments.get(2).isTab());
			assertEquals(8, line.segments.get(0).visualLength);
			assertEquals(8, line.segments.get(1).visualLength);
			assertEquals(8, line.segments.get(2).visualLength);
			assertEquals("abcdefgh", line.segments.get(0).text.toString());
			assertEquals("01234567", line.segments.get(2).text.toString());
		}

		@Test
		public void new_text_with_tab_misaligned()
		{
			Line line = new Line();

			line.insert(0, "abcdef\t01234567");

			assertEquals(3, line.segments.size());
			assertTrue(!line.segments.get(0).isTab());
			assertTrue(line.segments.get(1).isTab());
			assertTrue(!line.segments.get(2).isTab());
			assertEquals(6, line.segments.get(0).visualLength);
			assertEquals(2, line.segments.get(1).visualLength);
			assertEquals(8, line.segments.get(2).visualLength);
			assertEquals("abcdef", line.segments.get(0).text.toString());
			assertEquals("01234567", line.segments.get(2).text.toString());
		}

		@Test
		public void text_at_beginning()
		{
			Line line = new Line("abcd");

			line.insert(0, "defg");

			assertEquals(1, line.segments.size());
			assertEquals(8, line.segments.get(0).visualLength);
			assertEquals("defgabcd", line.segments.get(0).text.toString());
		}

		@Test
		public void text_at_end()
		{
			Line line = new Line("abcd");

			line.insert(4, "edfg");

			assertEquals(1, line.segments.size());
			assertEquals(8, line.segments.get(0).visualLength);
			assertEquals("abcdedfg", line.segments.get(0).text.toString());
		}

		@Test
		public void text_at_middle()
		{
			Line line = new Line("abcd");

			line.insert(2, "edfg");

			assertEquals(1, line.segments.size());
			assertEquals(8, line.segments.get(0).visualLength);
			assertEquals("abedfgcd", line.segments.get(0).text.toString());
		}

		@Test
		public void text_before_tab_should_change_next_tab()
		{
			Line line = new Line("abcd\t");

			line.insert(4, "ef");

			assertEquals(2, line.segments.size());
			assertTrue(!line.segments.get(0).isTab());
			assertTrue(line.segments.get(1).isTab());
			assertEquals(6, line.segments.get(0).visualLength);
			assertEquals(2, line.segments.get(1).visualLength);
			assertEquals("abcdef", line.segments.get(0).text.toString());
		}

		@Test
		public void text_before_tab_should_change_next_tab_overflow()
		{
			Line line = new Line("abcd\t");

			line.insert(4, "efgh");

			assertEquals(2, line.segments.size());
			assertTrue(!line.segments.get(0).isTab());
			assertTrue(line.segments.get(1).isTab());
			assertEquals(8, line.segments.get(0).visualLength);
			assertEquals(8, line.segments.get(1).visualLength);
			assertEquals("abcdefgh", line.segments.get(0).text.toString());
		}

		@Test
		public void text_before_tab_should_change_next_tab_overflow2()
		{
			Line line = new Line("abcd\t");

			line.insert(4, "efgh012345678");

			assertEquals(2, line.segments.size());
			assertTrue(!line.segments.get(0).isTab());
			assertTrue(line.segments.get(1).isTab());
			assertEquals(17, line.segments.get(0).visualLength);
			assertEquals(7, line.segments.get(1).visualLength);
			assertEquals("abcdefgh012345678", line.segments.get(0).text.toString());
		}

		@Test
		public void text_middle_before_tab_should_change_next_tab()
		{
			Line line = new Line("abcd\t");

			line.insert(2, "ef");

			assertEquals(2, line.segments.size());
			assertTrue(!line.segments.get(0).isTab());
			assertTrue(line.segments.get(1).isTab());
			assertEquals(6, line.segments.get(0).visualLength);
			assertEquals(2, line.segments.get(1).visualLength);
			assertEquals("abefcd", line.segments.get(0).text.toString());
		}

		@Test
		public void tab_at_beginning()
		{
			Line line = new Line("abcd");

			line.insert(0, "\t");

			assertEquals(2, line.segments.size());
			assertTrue(line.segments.get(0).isTab());
			assertTrue(!line.segments.get(1).isTab());
			assertEquals(8, line.segments.get(0).visualLength);
			assertEquals(4, line.segments.get(1).visualLength);
			assertEquals("abcd", line.segments.get(1).text.toString());
		}

		@Test
		public void two_tabs_at_beginning()
		{
			Line line = new Line("abcd");

			line.insert(0, "\t\t");

			assertEquals(3, line.segments.size());
			assertTrue(line.segments.get(0).isTab());
			assertTrue(line.segments.get(1).isTab());
			assertTrue(!line.segments.get(2).isTab());
			assertEquals(8, line.segments.get(0).visualLength);
			assertEquals(8, line.segments.get(1).visualLength);
			assertEquals(4, line.segments.get(2).visualLength);
			assertEquals("abcd", line.segments.get(2).text.toString());
		}

		@Test
		public void tab_at_end_aligned()
		{
			Line line = new Line("abcdefgh");

			line.insert(8, "\t");

			assertEquals(2, line.segments.size());
			assertTrue(!line.segments.get(0).isTab());
			assertTrue(line.segments.get(1).isTab());
			assertEquals(8, line.segments.get(0).visualLength);
			assertEquals(8, line.segments.get(1).visualLength);
			assertEquals("abcdefgh", line.segments.get(0).text.toString());
		}

		@Test
		public void two_tabs_at_end_aligned()
		{
			Line line = new Line("abcdefgh");

			line.insert(8, "\t\t");

			assertEquals(3, line.segments.size());
			assertTrue(!line.segments.get(0).isTab());
			assertTrue(line.segments.get(1).isTab());
			assertTrue(line.segments.get(2).isTab());
			assertEquals(8, line.segments.get(0).visualLength);
			assertEquals(8, line.segments.get(1).visualLength);
			assertEquals(8, line.segments.get(2).visualLength);
			assertEquals("abcdefgh", line.segments.get(0).text.toString());
		}

		@Test
		public void tab_at_end_misaligned()
		{
			Line line = new Line("abc");

			line.insert(3, "\t");

			assertEquals(2, line.segments.size());
			assertTrue(!line.segments.get(0).isTab());
			assertTrue(line.segments.get(1).isTab());
			assertEquals(3, line.segments.get(0).visualLength);
			assertEquals(5, line.segments.get(1).visualLength);
			assertEquals("abc", line.segments.get(0).text.toString());
		}

		@Test
		public void two_tabs_at_end_misaligned()
		{
			Line line = new Line("abc");

			line.insert(3, "\t\t");

			assertEquals(3, line.segments.size());
			assertTrue(!line.segments.get(0).isTab());
			assertTrue(line.segments.get(1).isTab());
			assertTrue(line.segments.get(2).isTab());
			assertEquals(3, line.segments.get(0).visualLength);
			assertEquals(5, line.segments.get(1).visualLength);
			assertEquals(8, line.segments.get(2).visualLength);
			assertEquals("abc", line.segments.get(0).text.toString());
		}

		@Test
		public void tab_inside_text_aligend()
		{
			Line line = new Line("abcdefgh01234567");
			assertEquals(1, line.segments.size());

			line.insert(8, "\t");

			assertEquals(3, line.segments.size());
			assertTrue(!line.segments.get(0).isTab());
			assertTrue(line.segments.get(1).isTab());
			assertTrue(!line.segments.get(2).isTab());
			assertEquals(8, line.segments.get(0).visualLength);
			assertEquals(8, line.segments.get(1).visualLength);
			assertEquals(8, line.segments.get(2).visualLength);
			assertEquals("abcdefgh", line.segments.get(0).text.toString());
			assertEquals("01234567", line.segments.get(2).text.toString());
		}

		@Test
		public void two_tabs_inside_text_aligend()
		{
			Line line = new Line("abcdefgh01234567");
			assertEquals(1, line.segments.size());

			line.insert(8, "\t\t");

			assertEquals(4, line.segments.size());
			assertTrue(!line.segments.get(0).isTab());
			assertTrue(line.segments.get(1).isTab());
			assertTrue(line.segments.get(2).isTab());
			assertTrue(!line.segments.get(3).isTab());
			assertEquals(8, line.segments.get(0).visualLength);
			assertEquals(8, line.segments.get(1).visualLength);
			assertEquals(8, line.segments.get(2).visualLength);
			assertEquals(8, line.segments.get(3).visualLength);
			assertEquals("abcdefgh", line.segments.get(0).text.toString());
			assertEquals("01234567", line.segments.get(3).text.toString());
		}

		@Test
		public void tab_inside_text_misaligend()
		{
			Line line = new Line("abcd");

			line.insert(1, "\t");

			assertEquals(3, line.segments.size());
			assertTrue(!line.segments.get(0).isTab());
			assertTrue(line.segments.get(1).isTab());
			assertTrue(!line.segments.get(2).isTab());
			assertEquals(1, line.segments.get(0).visualLength);
			assertEquals(7, line.segments.get(1).visualLength);
			assertEquals(3, line.segments.get(2).visualLength);
			assertEquals("a", line.segments.get(0).text.toString());
			assertEquals("bcd", line.segments.get(2).text.toString());
		}

		@Test
		public void two_tabs_inside_text_misaligned()
		{
			Line line = new Line("abcd");

			line.insert(1, "\t\t");

			assertEquals(4, line.segments.size());
			assertTrue(!line.segments.get(0).isTab());
			assertTrue(line.segments.get(1).isTab());
			assertTrue(line.segments.get(2).isTab());
			assertTrue(!line.segments.get(3).isTab());
			assertEquals(1, line.segments.get(0).visualLength);
			assertEquals(7, line.segments.get(1).visualLength);
			assertEquals(8, line.segments.get(2).visualLength);
			assertEquals(3, line.segments.get(3).visualLength);
			assertEquals("a", line.segments.get(0).text.toString());
			assertEquals("bcd", line.segments.get(3).text.toString());
		}

		@Test
		public void tab_between_text_aligend()
		{
			Line line = new Line("abcdefgh\t01234567");
			assertEquals(3, line.segments.size());
			assertTrue(!line.segments.get(0).isTab());
			assertTrue(line.segments.get(1).isTab());
			assertTrue(!line.segments.get(2).isTab());
			assertEquals(8, line.segments.get(0).visualLength);
			assertEquals(8, line.segments.get(1).visualLength);
			assertEquals(8, line.segments.get(2).visualLength);
			assertEquals("abcdefgh", line.segments.get(0).text.toString());
			assertEquals("01234567", line.segments.get(2).text.toString());

			line.insert(8, "\t");

			assertEquals(4, line.segments.size());
			assertTrue(!line.segments.get(0).isTab());
			assertTrue(line.segments.get(1).isTab());
			assertTrue(line.segments.get(2).isTab());
			assertTrue(!line.segments.get(3).isTab());
			assertEquals(8, line.segments.get(0).visualLength);
			assertEquals(8, line.segments.get(1).visualLength);
			assertEquals(8, line.segments.get(2).visualLength);
			assertEquals(8, line.segments.get(3).visualLength);
			assertEquals("abcdefgh", line.segments.get(0).text.toString());
			assertEquals("01234567", line.segments.get(3).text.toString());
		}

		@Test
		public void two_tabs_between_text_aligned()
		{
			Line line = new Line("abcdefgh\t01234567");
			assertEquals(3, line.segments.size());
			assertTrue(!line.segments.get(0).isTab());
			assertTrue(line.segments.get(1).isTab());
			assertTrue(!line.segments.get(2).isTab());
			assertEquals(8, line.segments.get(0).visualLength);
			assertEquals(8, line.segments.get(1).visualLength);
			assertEquals(8, line.segments.get(2).visualLength);
			assertEquals("abcdefgh", line.segments.get(0).text.toString());
			assertEquals("01234567", line.segments.get(2).text.toString());

			line.insert(8, "\t\t");

			assertEquals(5, line.segments.size());
			assertTrue(!line.segments.get(0).isTab());
			assertTrue(line.segments.get(1).isTab());
			assertTrue(line.segments.get(2).isTab());
			assertTrue(line.segments.get(3).isTab());
			assertTrue(!line.segments.get(4).isTab());
			assertEquals(8, line.segments.get(0).visualLength);
			assertEquals(8, line.segments.get(1).visualLength);
			assertEquals(8, line.segments.get(2).visualLength);
			assertEquals(8, line.segments.get(3).visualLength);
			assertEquals(8, line.segments.get(4).visualLength);
			assertEquals("abcdefgh", line.segments.get(0).text.toString());
			assertEquals("01234567", line.segments.get(4).text.toString());
		}
	}

	public static class Delete
	{
		@Test
		public void at_end()
		{
			Line line = new Line("abcd");

			line.delete(2, 4);

			assertEquals(1, line.segments.size());
			assertTrue(!line.segments.get(0).isTab());
			assertEquals("ab", line.segments.get(0).text.toString());
		}

		@Test
		public void tab_at_end()
		{
			Line line = new Line("abcd\t");

			line.delete(4, 5);

			assertEquals(1, line.segments.size());
			assertTrue(!line.segments.get(0).isTab());
			assertEquals("abcd", line.segments.get(0).text.toString());
		}

		@Test
		public void span_multiple()
		{
			Line line = new Line("abcd\t\tefgh\t_\t");

			line.delete(2, 12);

			assertEquals(2, line.segments.size());
			assertTrue(!line.segments.get(0).isTab());
			assertTrue(line.segments.get(1).isTab());
			assertEquals("ab", line.segments.get(0).text.toString());
			assertEquals(6, line.segments.get(1).visualLength);
		}

		@Test
		public void two_tabs()
		{
			Line line = new Line("abcd\t\tefgh");

			line.delete(4, 6);

			assertEquals(1, line.segments.size());
			assertTrue(!line.segments.get(0).isTab());
			assertEquals("abcdefgh", line.segments.get(0).text.toString());
		}
	}
}
