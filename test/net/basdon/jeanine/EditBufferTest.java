package net.basdon.jeanine;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Most important test, to see if the editor can actually edit stuff like it's supposed to.
 */
@RunWith(Enclosed.class)
public class EditBufferTest
{
	public static class Move
	{
		@Test
		public void h()
		{
			createBuffer(
				"a<caret>bc"
			).executeSuccess(
				"h"
			).assertBuffer(
				"<caret>abc"
			).executeFail("h");
		}

		@Test
		public void j()
		{
			createBuffer(
				"a<caret>bc",
				"def"
			).executeSuccess(
				"j"
			).assertBuffer(
				"abc",
				"d<caret>ef"
			).executeFail("j");
		}

		@Test
		public void k()
		{
			createBuffer(
				"abc",
				"d<caret>ef"
			).executeSuccess(
				"k"
			).assertBuffer(
				"a<caret>bc",
				"def"
			).executeFail("k");
		}

		@Test
		public void l()
		{
			createBuffer(
				"a<caret>bc"
			).executeSuccess(
				"l"
			).assertBuffer(
				"ab<caret>c"
			).executeFail("l");
		}

		@Test
		public void caret()
		{
			createBuffer(
				"ab<caret>c"
			).executeSuccess(
				"^"
			).assertBuffer(
				"<caret>abc"
			);
		}

		@Test
		public void dollar()
		{
			createBuffer(
				"a<caret>bc"
			).executeSuccess(
				"$"
			).assertBuffer(
				"ab<caret>c" // caret is _on_ the c character
			);
		}

		@Test
		public void gg()
		{
			createBuffer(
				"abc",
				"d<caret>ef"
			).executeSuccess(
				"gg"
			).assertBuffer(
				"a<caret>bc",
				"def"
			);
		}

		@Test
		public void G()
		{
			createBuffer(
				"a<caret>bc",
				"def"
			).executeSuccess(
				"G"
			).assertBuffer(
				"abc",
				"d<caret>ef"
			);
		}

		@Test
		public void j_with_virtual_position()
		{
			createBuffer(
				"abc<caret>def",
				"gh",
				"",
				"ijklmnop"
			).executeSuccess(
				"j"
			).assertBuffer(
				"abcdef",
				"g<caret>h",
				"",
				"ijklmnop"
			).executeSuccess(
				"j"
			).assertBuffer(
				"abcdef",
				"gh",
				"<caret>",
				"ijklmnop"
			).executeSuccess(
				"j"
			).assertBuffer(
				"abcdef",
				"gh",
				"",
				"ijk<caret>lmnop"
			);
		}

		@Test
		public void k_with_virtual_position()
		{
			createBuffer(
				"abcdef",
				"gh",
				"",
				"ijk<caret>lmnop"
			).executeSuccess(
				"k"
			).assertBuffer(
				"abcdef",
				"gh",
				"<caret>",
				"ijklmnop"
			).executeSuccess(
				"k"
			).assertBuffer(
				"abcdef",
				"g<caret>h",
				"",
				"ijklmnop"
			).executeSuccess(
				"k"
			).assertBuffer(
				"abc<caret>def",
				"gh",
				"",
				"ijklmnop"
			);
		}

		@Test
		public void j_with_virtual_position_end()
		{
			createBuffer(
				"x<caret>x",
				"abcdef",
				"gh",
				"",
				"ijklmnop"
			).executeSuccess(
				"$j"
			).assertBuffer(
				"xx",
				"abcde<caret>f",
				"gh",
				"",
				"ijklmnop"
			).executeSuccess(
				"j"
			).assertBuffer(
				"xx",
				"abcdef",
				"g<caret>h",
				"",
				"ijklmnop"
			).executeSuccess(
				"j"
			).assertBuffer(
				"xx",
				"abcdef",
				"gh",
				"<caret>",
				"ijklmnop"
			).executeSuccess(
				"j"
			).assertBuffer(
				"xx",
				"abcdef",
				"gh",
				"",
				"ijklmno<caret>p"
			);
		}

		@Test
		public void k_with_virtual_position_end()
		{
			createBuffer(
				"xx",
				"abcdef",
				"gh",
				"",
				"ijk<caret>lmnop"
			).executeSuccess(
				"$k"
			).assertBuffer(
				"xx",
				"abcdef",
				"gh",
				"<caret>",
				"ijklmnop"
			).executeSuccess(
				"k"
			).assertBuffer(
				"xx",
				"abcdef",
				"g<caret>h",
				"",
				"ijklmnop"
			).executeSuccess(
				"k"
			).assertBuffer(
				"xx",
				"abcde<caret>f",
				"gh",
				"",
				"ijklmnop"
			).executeSuccess(
				"k"
			).assertBuffer(
				"x<caret>x",
				"abcdef",
				"gh",
				"",
				"ijklmnop"
			);
		}

		@Test
		public void w()
		{
			createBuffer(
				"<caret>Welc Jeanine, a 2d ed Vim-like keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"w"
			).assertBuffer(
				"Welc <caret>Jeanine, a 2d ed Vim-like keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"w"
			).assertBuffer(
				"Welc Jeanine<caret>, a 2d ed Vim-like keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"w"
			).assertBuffer(
				"Welc Jeanine, <caret>a 2d ed Vim-like keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"w"
			).assertBuffer(
				"Welc Jeanine, a <caret>2d ed Vim-like keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"w"
			).assertBuffer(
				"Welc Jeanine, a 2d <caret>ed Vim-like keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"w"
			).assertBuffer(
				"Welc Jeanine, a 2d ed <caret>Vim-like keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"w"
			).assertBuffer(
				"Welc Jeanine, a 2d ed Vim<caret>-like keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"w"
			).assertBuffer(
				"Welc Jeanine, a 2d ed Vim-<caret>like keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"w"
			).assertBuffer(
				"Welc Jeanine, a 2d ed Vim-like <caret>keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"w"
			).assertBuffer(
				"Welc Jeanine, a 2d ed Vim-like keybindings",
				"",
				"<caret>Movement: h j"
			).executeSuccess(
				"w"
			).assertBuffer(
				"Welc Jeanine, a 2d ed Vim-like keybindings",
				"",
				"Movement<caret>: h j"
			).executeSuccess(
				"w"
			).assertBuffer(
				"Welc Jeanine, a 2d ed Vim-like keybindings",
				"",
				"Movement: <caret>h j"
			).executeSuccess(
				"w"
			).assertBuffer(
				"Welc Jeanine, a 2d ed Vim-like keybindings",
				"",
				"Movement: h <caret>j"
			).executeFail(
				"w"
			);
		}

		@Test
		public void b()
		{
			createBuffer(
				"Welc Jeanine, a 2d ed Vim-like keybindings",
				"",
				"Movement: h <caret>j"
			).executeSuccess(
				"b"
			).assertBuffer(
				"Welc Jeanine, a 2d ed Vim-like keybindings",
				"",
				"Movement: <caret>h j"
			).executeSuccess(
				"b"
			).assertBuffer(
				"Welc Jeanine, a 2d ed Vim-like keybindings",
				"",
				"Movement<caret>: h j"
			).executeSuccess(
				"b"
			).assertBuffer(
				"Welc Jeanine, a 2d ed Vim-like keybindings",
				"",
				"<caret>Movement: h j"
			).executeSuccess(
				"b"
			).assertBuffer(
				"Welc Jeanine, a 2d ed Vim-like <caret>keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"b"
			).assertBuffer(
				"Welc Jeanine, a 2d ed Vim-<caret>like keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"b"
			).assertBuffer(
				"Welc Jeanine, a 2d ed Vim<caret>-like keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"b"
			).assertBuffer(
				"Welc Jeanine, a 2d ed <caret>Vim-like keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"b"
			).assertBuffer(
				"Welc Jeanine, a 2d <caret>ed Vim-like keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"b"
			).assertBuffer(
				"Welc Jeanine, a <caret>2d ed Vim-like keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"b"
			).assertBuffer(
				"Welc Jeanine, <caret>a 2d ed Vim-like keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"b"
			).assertBuffer(
				"Welc Jeanine<caret>, a 2d ed Vim-like keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"b"
			).assertBuffer(
				"Welc <caret>Jeanine, a 2d ed Vim-like keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"b"
			).assertBuffer(
				"<caret>Welc Jeanine, a 2d ed Vim-like keybindings",
				"",
				"Movement: h j"
			).executeFail(
				"b"
			);
		}

		@Test
		public void e()
		{
			createBuffer(
				"<caret>Welc Jeanine, a 2d ed Vim-like keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"e"
			).assertBuffer(
				"Wel<caret>c Jeanine, a 2d ed Vim-like keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"e"
			).assertBuffer(
				"Welc Jeanin<caret>e, a 2d ed Vim-like keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"e"
			).assertBuffer(
				"Welc Jeanine<caret>, a 2d ed Vim-like keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"e"
			).assertBuffer(
				"Welc Jeanine, <caret>a 2d ed Vim-like keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"e"
			).assertBuffer(
				"Welc Jeanine, a 2<caret>d ed Vim-like keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"e"
			).assertBuffer(
				"Welc Jeanine, a 2d e<caret>d Vim-like keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"e"
			).assertBuffer(
				"Welc Jeanine, a 2d ed Vi<caret>m-like keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"e"
			).assertBuffer(
				"Welc Jeanine, a 2d ed Vim<caret>-like keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"e"
			).assertBuffer(
				"Welc Jeanine, a 2d ed Vim-lik<caret>e keybindings",
				"",
				"Movement: h j"
			).executeSuccess(
				"e"
			).assertBuffer(
				"Welc Jeanine, a 2d ed Vim-like keybinding<caret>s",
				"",
				"Movement: h j"
			).executeSuccess(
				"e"
			).assertBuffer(
				"Welc Jeanine, a 2d ed Vim-like keybindings",
				"",
				"Movemen<caret>t: h j"
			).executeSuccess(
				"e"
			).assertBuffer(
				"Welc Jeanine, a 2d ed Vim-like keybindings",
				"",
				"Movement<caret>: h j"
			).executeSuccess(
				"e"
			).assertBuffer(
				"Welc Jeanine, a 2d ed Vim-like keybindings",
				"",
				"Movement: <caret>h j"
			).executeSuccess(
				"e"
			).assertBuffer(
				"Welc Jeanine, a 2d ed Vim-like keybindings",
				"",
				"Movement: h <caret>j"
			).executeFail(
				"e"
			);
		}
	}

	public static class Insert
	{
		@Test
		public void at_end_of_line()
		{
			createBuffer(
				"ab<caret>c",
				"def"
			).executeSuccess(
				"ix"
			).assertBuffer(
				"abx<caret>c",
				"def"
			).executeSuccess(
				"<esc>"
			).assertBuffer(
				"ab<caret>xc",
				"def"
			).executeSuccess(
				"u"
			).assertBuffer(
				"ab<caret>c",
				"def"
			);
		}

		@Test
		public void at_start_of_line()
		{
			createBuffer(
				"abc",
				"def"
			).executeSuccess(
				"ixx<esc>"
			).assertBuffer(
				"xxabc",
				"def"
			).executeSuccess(
				"u"
			).assertBuffer(
				"abc",
				"def"
			);
		}

		@Test
		public void at_start_of_empty_line()
		{
			createBuffer(
				"abc",
				"<caret>"
			).executeSuccess(
				"ixx"
			).assertBuffer(
				"abc",
				"xx<caret>"
			).executeSuccess(
				"<esc>"
			).assertBuffer(
				"abc",
				"x<caret>x"
			).executeSuccess(
				"u"
			).assertBuffer(
				"abc",
				"<caret>"
			);
		}

		@Test
		public void enter()
		{
			createBuffer(
				"ab<caret>c"
			).executeSuccess(
				"i\nand <esc>"
			).assertBuffer(
				"ab",
				"and<caret> c"
			).executeSuccess(
				"u"
			).assertBuffer(
				"ab<caret>c"
			);
		}

		@Test
		public void bs()
		{
			createBuffer(
				"abc",
				"de<caret>f"
			).executeSuccess(
				"i<bs><bs><bs><esc>"
			).assertBuffer(
				"ab<caret>cf"
			).executeSuccess(
				"u"
			).assertBuffer(
				"abc",
				"de<caret>f"
			);
		}

		@Test
		public void del()
		{
			createBuffer(
				"ab<caret>c",
				"def"
			).executeSuccess(
				"i<del><del><esc>"
			).assertBuffer(
				"a<caret>bdef"
			).executeSuccess(
				"u"
			).assertBuffer(
				"ab<caret>c",
				"def"
			);
		}
	}

	public static class InsertBeginning
	{
		@Test
		public void at_end_of_line()
		{
			createBuffer(
				"ab<caret>c"
			).executeSuccess(
				"Ix<esc>"
			).assertBuffer(
				"<caret>xabc"
			).executeSuccess(
				"u"
			).assertBuffer(
				"ab<caret>c"
			);
		}

		@Test
		public void at_start_of_line()
		{
			createBuffer(
				"abc"
			).executeSuccess(
				"Ixx<esc>"
			).assertBuffer(
				"xxabc"
			).executeSuccess(
				"u"
			).assertBuffer(
				"abc"
			);
		}

		@Test
		public void at_start_of_empty_line()
		{
			createBuffer(
				"<caret>"
			).executeSuccess(
				"Ixx<esc>"
			).assertBuffer(
				"x<caret>x"
			).executeSuccess(
				"u"
			).assertBuffer(
				"<caret>"
			);
		}

		@Test
		public void line_with_leading_spaces()
		{
			createBuffer(
				"   xx<caret>x"
			).executeSuccess(
				"Iy<esc>"
			).assertBuffer(
				"   <caret>yxxx"
			).executeSuccess(
				"u"
			).assertBuffer(
				"   xx<caret>x"
			);
		}

		@Test
		public void line_with_leading_tabs()
		{
			createBuffer(
				"\t\txx<caret>x"
			).executeSuccess(
				"Iy<esc>"
			).assertBuffer(
				"\t\t<caret>yxxx"
			).executeSuccess(
				"u"
			).assertBuffer(
				"\t\txx<caret>x"
			);
		}
	}

	public static class InsertLine
	{
		@Test
		public void middle_in_line()
		{
			createBuffer(
				"the q<caret>uick"
			).executeSuccess(
				"osomething<esc>"
			).assertBuffer(
				"the quick",
				"somethin<caret>g"
			).executeSuccess(
				"u"
			).assertBuffer(
				"the q<caret>uick"
			);
		}

		@Test
		public void with_lines_after()
		{
			createBuffer(
				"the q<caret>uick",
				"abc"
			).executeSuccess(
				"osomething<esc>"
			).assertBuffer(
				"the quick",
				"somethin<caret>g",
				"abc"
			).executeSuccess(
				"u"
			).assertBuffer(
				"the q<caret>uick",
				"abc"
			);
		}
	}

	public static class InsertLineBefore
	{
		@Test
		public void middle_in_line()
		{
			createBuffer(
				"the q<caret>uick"
			).executeSuccess(
				"Osomething<esc>"
			).assertBuffer(
				"somethin<caret>g",
				"the quick"
			).executeSuccess(
				"u"
			).assertBuffer(
				"the q<caret>uick"
			);
		}

		@Test
		public void with_lines_before()
		{
			createBuffer(
				"abc",
				"the q<caret>uick"
			).executeSuccess(
				"Osomething<esc>"
			).assertBuffer(
				"abc",
				"somethin<caret>g",
				"the quick"
			).executeSuccess(
				"u"
			).assertBuffer(
				"abc",
				"the q<caret>uick"
			);
		}
	}

	public static class Append
	{
		@Test
		public void at_end_of_line()
		{
			createBuffer(
				"ab<caret>c",
				"def"
			).executeSuccess(
				"ax<esc>"
			).assertBuffer(
				"abc<caret>x",
				"def"
			).executeSuccess(
				"u"
			).assertBuffer(
				"ab<caret>c",
				"def"
			);
		}

		@Test
		public void at_start_of_line()
		{
			createBuffer(
				"abc",
				"def"
			).executeSuccess(
				"axx<esc>"
			).assertBuffer(
				"axxbc",
				"def"
			).executeSuccess(
				"u"
			).assertBuffer(
				"abc",
				"def"
			);
		}

		@Test
		public void at_start_of_empty_line()
		{
			createBuffer(
				"abc",
				"<caret>"
			).executeSuccess(
				"axx<esc>"
			).assertBuffer(
				"abc",
				"x<caret>x"
			).executeSuccess(
				"u"
			).assertBuffer(
				"abc",
				"<caret>"
			);
		}
	}

	public static class AppendEnd
	{
		@Test
		public void at_end_of_line()
		{
			createBuffer(
				"ab<caret>c"
			).executeSuccess(
				"Ax<esc>"
			).assertBuffer(
				"abc<caret>x"
			).executeSuccess(
				"u"
			).assertBuffer(
				"ab<caret>c"
			);
		}

		@Test
		public void at_start_of_line()
		{
			createBuffer(
				"abc"
			).executeSuccess(
				"Axx<esc>"
			).assertBuffer(
				"abcxx"
			).executeSuccess(
				"u"
			).assertBuffer(
				"abc"
			);
		}

		@Test
		public void at_start_of_empty_line()
		{
			createBuffer(
				"<caret>"
			).executeSuccess(
				"Axx<esc>"
			).assertBuffer(
				"x<caret>x"
			).executeSuccess(
				"u"
			).assertBuffer(
				"<caret>"
			);
		}
	}

	public static class Delete
	{
		@Test
		public void x()
		{
			createBuffer(
				"bl<caret>ack", ""
			).executeSuccess(
				"x"
			).assertBuffer(
				"bl<caret>ck", ""
			).executeSuccess(
				"x"
			).assertBuffer(
				"bl<caret>k", ""
			).executeSuccess(
				"x"
			).assertBuffer(
				"b<caret>l", ""
			).executeSuccess(
				"x"
			).assertBuffer(
				"<caret>b", ""
			).executeSuccess(
				"x"
			).assertBuffer(
				"<caret>", ""
			).executeFail(
				"x"
			).executeSuccess(
				"uuuuu"
			).assertBuffer(
				"bl<caret>ack", ""
			);
		}

		@Test
		public void dw()
		{
			createBuffer(
				"the <caret>quick brown"
			).executeSuccess(
				"dw"
			).assertBuffer(
				"the <caret> brown"
			).executeSuccess(
				"dw"
			).assertBuffer(
				"the<caret> "
			).executeFail(
				"dw" // fail because no next word to jump to (different from vim)
			).executeSuccess(
				"uu"
			).assertBuffer(
				"the <caret>quick brown"
			);
		}

		@Test
		public void dw_one_word()
		{
			createBuffer(
				"br<caret>own"
			).executeSuccess(
				"dw"
			).assertBuffer(
				"b<caret>r"
			).executeSuccess(
				"u"
			).assertBuffer(
				"br<caret>own"
			);
		}

		@Test
		public void dw_end()
		{
			createBuffer(
				"on<caret>e two"
			).executeSuccess(
				"dw"
			).assertBuffer(
				"on<caret>two"
			).executeSuccess(
				"u"
			).assertBuffer(
				"on<caret>e two"
			);
		}

		/*
		@Test
		public void de()
		{
			createBuffer(
				"<caret>one two"
			).executeSuccess(
				"de"
			).assertBuffer(
				"<caret> two"
			);
		}

		@Test
		public void de_lastword()
		{
			createBuffer(
				"<caret>two"
			).executeSuccess(
				"de"
			).assertBuffer(
				"<caret>"
			);
		}

		@Test
		public void de_end()
		{
			createBuffer(
				"on<caret>e two"
			).executeSuccess(
				"de"
			).assertBuffer(
				"o<caret>n"
			);
		}
		*/

		@Test
		public void db()
		{
			createBuffer(
				"one tw<caret>o"
			).executeSuccess(
				"db"
			).assertBuffer(
				"one <caret>o"
			).executeSuccess(
				"u"
			).assertBuffer(
				"one tw<caret>o"
			);
		}

		@Test
		public void db_beginning()
		{
			createBuffer(
				"one <caret>two"
			).executeSuccess(
				"db"
			).assertBuffer(
				"<caret>two"
			).executeSuccess(
				"u"
			).assertBuffer(
				"one <caret>two"
			);
		}

		@Test
		public void db_one_word()
		{
			createBuffer(
				"on<caret>e"
			).executeSuccess(
				"db"
			).assertBuffer(
				"<caret>e"
			).executeSuccess(
				"u"
			).assertBuffer(
				"on<caret>e"
			);
		}

		@Test
		public void diw()
		{
			createBuffer(
				"<caret>one two"
			).executeSuccess(
				"diw"
			).assertBuffer(
				"<caret> two"
			).executeSuccess(
				"u"
			).assertBuffer(
				"<caret>one two"
			);
		}

		@Test
		public void diw_middle()
		{
			createBuffer(
				"o<caret>ne two"
			).executeSuccess(
				"diw"
			).assertBuffer(
				"<caret> two"
			).executeSuccess(
				"u"
			).assertBuffer(
				"o<caret>ne two"
			);
		}

		@Test
		public void diw_end()
		{
			createBuffer(
				"on<caret>e two"
			).executeSuccess(
				"diw"
			).assertBuffer(
				"<caret> two"
			).executeSuccess(
				"u"
			).assertBuffer(
				"on<caret>e two"
			);
		}

		@Test
		public void dd_oneline()
		{
			createBuffer(
				"on<caret>e two"
			).executeSuccess(
				"dd"
			).assertBuffer(
				"<caret>"
			).executeSuccess(
				"u"
			).assertBuffer(
				"on<caret>e two"
			);
		}

		@Test
		public void dd_first()
		{
			createBuffer(
				"on<caret>e two",
				"hey"
			).executeSuccess(
				"dd"
			).assertBuffer(
				"<caret>hey"
			).executeSuccess(
				"u"
			).assertBuffer(
				"on<caret>e two",
				"hey"
			);
		}

		@Test
		public void dd_last()
		{
			createBuffer(
				"hey",
				"on<caret>e two"
			).executeSuccess(
				"dd"
			).assertBuffer(
				"<caret>hey"
			).executeSuccess(
				"u"
			).assertBuffer(
				"hey",
				"on<caret>e two"
			);
		}

		@Test
		public void dd_empty()
		{
			createBuffer(
				"<caret>"
			).executeSuccess(
				"dd"
			).assertBuffer(
				"<caret>"
			).executeSuccess(
				"u"
			).assertBuffer(
				"<caret>"
			);
		}

		@Test
		public void dd_empty_between()
		{
			createBuffer(
				"xyz",
				"<caret>",
				"abc"
			).executeSuccess(
				"dd"
			).assertBuffer(
				"xyz",
				"<caret>abc"
			).executeSuccess(
				"u"
			).assertBuffer(
				"xyz",
				"<caret>",
				"abc"
			);
		}

		@Test
		public void dj_bot()
		{
			createBuffer(
				"hey",
				"on<caret>e two"
			).executeFail(
				"dj"
			);
		}

		@Test
		public void dj_first()
		{
			createBuffer(
				"on<caret>e two",
				"second",
				"hey"
			).executeSuccess(
				"dj"
			).assertBuffer(
				"<caret>hey"
			).executeSuccess(
				"u"
			).assertBuffer(
				"on<caret>e two",
				"second",
				"hey"
			);
		}

		@Test
		public void dj_last()
		{
			createBuffer(
				"hey",
				"on<caret>e two",
				"second"
			).executeSuccess(
				"dj"
			).assertBuffer(
				"<caret>hey"
			).executeSuccess(
				"u"
			).assertBuffer(
				"hey",
				"on<caret>e two",
				"second"
			);
		}

		@Test
		public void dj_empty()
		{
			createBuffer(
				"<caret>",
				""
			).executeSuccess(
				"dj"
			).assertBuffer(
				"<caret>"
			).executeSuccess(
				"u"
			).assertBuffer(
				"<caret>",
				""
			);
		}

		@Test
		public void dj_empty_between()
		{
			createBuffer(
				"xyz",
				"<caret>",
				"",
				"abc"
			).executeSuccess(
				"dj"
			).assertBuffer(
				"xyz",
				"<caret>abc"
			).executeSuccess(
				"u"
			).assertBuffer(
				"xyz",
				"<caret>",
				"",
				"abc"
			);
		}

		@Test
		public void dk_top()
		{
			createBuffer(
				"on<caret>e two",
				"second"
			).executeFail(
				"dk"
			);
		}

		@Test
		public void dk_first()
		{
			createBuffer(
				"hey",
				"on<caret>e two",
				"second"
			).executeSuccess(
				"dk"
			).assertBuffer(
				"<caret>second"
			).executeSuccess(
				"u"
			).assertBuffer(
				"hey",
				"on<caret>e two",
				"second"
			);
		}

		@Test
		public void dk_last()
		{
			createBuffer(
				"hey",
				"on<caret>e two"
			).executeSuccess(
				"dk"
			).assertBuffer(
				"<caret>"
			).executeSuccess(
				"u"
			).assertBuffer(
				"hey",
				"on<caret>e two"
			);
		}

		@Test
		public void dk_empty()
		{
			createBuffer(
				"",
				"<caret>"
			).executeSuccess(
				"dk"
			).assertBuffer(
				"<caret>"
			).executeSuccess(
				"u"
			).assertBuffer(
				"",
				"<caret>"
			);
		}

		@Test
		public void dk_empty_between()
		{
			createBuffer(
				"xyz",
				"",
				"<caret>",
				"abc"
			).executeSuccess(
				"dk"
			).assertBuffer(
				"xyz",
				"<caret>abc"
			).executeSuccess(
				"u"
			).assertBuffer(
				"xyz",
				"",
				"<caret>",
				"abc"
			);
		}
	}

	public static class Change
	{
		@Test
		public void cw()
		{
			createBuffer(
				"<caret>one two"
			).executeSuccess(
				"cwxy<esc>"
			).assertBuffer(
				"x<caret>y two"
			).executeSuccess(
				"u"
			).assertBuffer(
				"<caret>one two"
			);
		}

		@Test
		public void cw_end()
		{
			createBuffer(
				"on<caret>e two"
			).executeSuccess(
				"cwxy<esc>"
			).assertBuffer(
				"onx<caret>ytwo"
			).executeSuccess(
				"u"
			).assertBuffer(
				"on<caret>e two"
			);
		}

		@Test
		public void cb()
		{
			createBuffer(
				"one tw<caret>o"
			).executeSuccess(
				"cbxy<esc>"
			).assertBuffer(
				"one x<caret>yo"
			).executeSuccess(
				"u"
			).assertBuffer(
				"one tw<caret>o"
			);
		}

		@Test
		public void cb_begin()
		{
			createBuffer(
				"one <caret>two"
			).executeSuccess(
				"cbxy<esc>"
			).assertBuffer(
				"x<caret>ytwo"
			).executeSuccess(
				"u"
			).assertBuffer(
				"one <caret>two"
			);
		}

		/*
		@Test
		public void ce()
		{
			createBuffer(
				"<caret>one two"
			).executeSuccess(
				"cexy<esc>"
			).assertBuffer(
				"x<caret>y two"
			);
		}

		@Test
		public void ce_end()
		{
			createBuffer(
				"on<caret>e two"
			).executeSuccess(
				"cexy<esc>"
			).assertBuffer(
				"onx<caret>y"
			);
		}
		*/

		@Test
		public void ciw()
		{
			createBuffer(
				"<caret>one two"
			).executeSuccess(
				"ciwxy<esc>"
			).assertBuffer(
				"x<caret>y two"
			).executeSuccess(
				"u"
			).assertBuffer(
				"<caret>one two"
			);
		}

		@Test
		public void ciw_middle()
		{
			createBuffer(
				"o<caret>ne two"
			).executeSuccess(
				"ciwxy<esc>"
			).assertBuffer(
				"x<caret>y two"
			).executeSuccess(
				"u"
			).assertBuffer(
				"o<caret>ne two"
			);
		}

		@Test
		public void ciw_end()
		{
			createBuffer(
				"on<caret>e two"
			).executeSuccess(
				"ciwxy<esc>"
			).assertBuffer(
				"x<caret>y two"
			).executeSuccess(
				"u"
			).assertBuffer(
				"on<caret>e two"
			);
		}
	}

	public static class Paste
	{
		@Test
		public void after()
		{
			createBuffer(
				"o<caret>ne"
			).executeSuccess(
				"xp"
			).assertBuffer(
				"oe<caret>n"
			).executeSuccess(
				"p"
			).assertBuffer(
				"oen<caret>n"
			).executeSuccess(
				"uuu"
			).assertBuffer(
				"o<caret>ne"
			);
		}

		@Test
		public void before()
		{
			createBuffer(
				"o<caret>ne"
			).executeSuccess(
				"xP"
			).assertBuffer(
				"o<caret>ne"
			).executeSuccess(
				"P"
			).assertBuffer(
				"o<caret>nne"
			).executeSuccess(
				"uuu"
			).assertBuffer(
				"o<caret>ne"
			);
		}

		@Test
		public void after_eol()
		{
			createBuffer(
				"o<caret>n"
			).executeSuccess(
				"x"
			).assertBuffer(
				"<caret>o"
			).executeSuccess(
				"p"
			).assertBuffer(
				"o<caret>n"
			).executeSuccess(
				"uu"
			).assertBuffer(
				"o<caret>n"
			);
		}

		@Test
		public void before_start()
		{
			createBuffer(
				"<caret>on"
			).executeSuccess(
				"x"
			).assertBuffer(
				"<caret>n"
			).executeSuccess(
				"P"
			).assertBuffer(
				"<caret>on"
			).executeSuccess(
				"uu"
			).assertBuffer(
				"<caret>on"
			);
		}

		@Test
		public void from_dd()
		{
			createBuffer(
				"one",
				"<caret>two",
				"three"
			).executeSuccess(
				"ddp"
			).assertBuffer(
				"one",
				"three",
				"<caret>two"
			).executeSuccess(
				"ggP"
			).assertBuffer(
				"<caret>two",
				"one",
				"three",
				"two"
			).executeSuccess(
				"uuu"
			).assertBuffer(
				"one",
				"<caret>two",
				"three"
			);
		}

		@Test
		public void from_dj()
		{
			createBuffer(
				"one",
				"<caret>two",
				"three"
			).executeSuccess(
				"djp"
			).assertBuffer(
				"one",
				"<caret>two",
				"three"
			).executeSuccess(
				"P"
			).assertBuffer(
				"one",
				"<caret>two",
				"three",
				"two",
				"three"
			).executeSuccess(
				"uuu"
			).assertBuffer(
				"one",
				"<caret>two",
				"three"
			);
		}

		@Test
		public void from_dk()
		{
			createBuffer(
				"one",
				"<caret>two",
				"three"
			).executeSuccess(
				"dkp"
			).assertBuffer(
				"three",
				"<caret>one",
				"two"
			).executeSuccess(
				"P"
			).assertBuffer(
				"three",
				"<caret>one",
				"two",
				"one",
				"two"
			).executeSuccess(
				"uuu"
			).assertBuffer(
				"one",
				"<caret>two",
				"three"
			);
		}
	}

	public static class LineSelect
	{
		@Test
		public void delete()
		{
			createBuffer(
				"one",
				"<caret>two",
				"three"
			).executeSuccess(
				"Vd"
			).assertBuffer(
				"one",
				"<caret>three"
			).executeSuccess(
				"."
			).assertBuffer(
				"<caret>one"
			).executeSuccess(
				"uu"
			).assertBuffer(
				"one",
				"<caret>two",
				"three"
			);
		}

		@Test
		public void delete_multiple_move_up_down()
		{
			createBuffer(
				"a",
				"<caret>two",
				"three",
				"b",
				"c"
			).executeSuccess(
				"Vkjjd"
			).assertBuffer(
				"a",
				"<caret>b",
				"c"
			).executeSuccess(
				"."
			).assertBuffer(
				"<caret>a"
			).executeSuccess(
				"uu"
			).assertBuffer(
				"a",
				"<caret>two",
				"three",
				"b",
				"c"
			).executeSuccess(
				"p"
			).assertBuffer(
				"a",
				"two",
				"<caret>b",
				"c",
				"three",
				"b",
				"c"
			);
		}

		@Test
		public void jump_other_side()
		{
			createBuffer(
				"a",
				"<caret>two",
				"three",
				"b"
			).executeSuccess(
				"Vkojd"
			).assertBuffer(
				"<caret>b"
			).executeSuccess(
				"u"
			).assertBuffer(
				"a",
				"<caret>two",
				"three",
				"b"
			);
		}
	}

	public static class Repeat
	{
		@Test
		public void insert()
		{
			createBuffer(
				"<caret>"
			).executeSuccess(
				"iab<esc>"
			).assertBuffer(
				"a<caret>b"
			).executeSuccess(
				"."
			).assertBuffer(
				"aa<caret>bb"
			).executeSuccess(
				"uu"
			).assertBuffer(
				"<caret>"
			);
		}

		@Test
		public void insert_beginning()
		{
			createBuffer(
				"hello<caret>"
			).executeSuccess(
				"Iab<esc>"
			).assertBuffer(
				"a<caret>bhello"
			).executeSuccess(
				"."
			).assertBuffer(
				"a<caret>babhello"
			).executeSuccess(
				"uu"
			).assertBuffer(
				"hello<caret>"
			);
		}

		@Test
		public void insert_line()
		{
			createBuffer(
				"the q<caret>uick",
				"abc"
			).executeSuccess(
				"osomething<esc>"
			).assertBuffer(
				"the quick",
				"somethin<caret>g",
				"abc"
			).executeSuccess(
				".."
			).assertBuffer(
				"the quick",
				"something",
				"something",
				"somethin<caret>g",
				"abc"
			).executeSuccess(
				"uuu"
			).assertBuffer(
				"the q<caret>uick",
				"abc"
			);
		}

		@Test
		public void insert_line_before()
		{
			createBuffer(
				"abc",
				"the q<caret>uick"
			).executeSuccess(
				"Osomething<esc>"
			).assertBuffer(
				"abc",
				"somethin<caret>g",
				"the quick"
			).executeSuccess(
				".."
			).assertBuffer(
				"abc",
				"somethin<caret>g",
				"something",
				"something",
				"the quick"
			).executeSuccess(
				"uuu"
			).assertBuffer(
				"abc",
				"the q<caret>uick"
			);
		}

		@Test
		public void append()
		{
			createBuffer(
				"a<caret>bc"
			).executeSuccess(
				"axy<esc>"
			).assertBuffer(
				"abx<caret>yc"
			).executeSuccess(
				".."
			).assertBuffer(
				"abxyxyx<caret>yc"
			).executeSuccess(
				"uuu"
			).assertBuffer(
				"a<caret>bc"
			);
		}

		@Test
		public void append_end()
		{
			createBuffer(
				"a<caret>bc"
			).executeSuccess(
				"Axy<esc>"
			).assertBuffer(
				"abcx<caret>y"
			).executeSuccess(
				"^."
			).assertBuffer(
				"abcxyx<caret>y"
			).executeSuccess(
				"uu"
			).assertBuffer(
				"a<caret>bc"
			);
		}

		@Test
		public void x()
		{
			createBuffer(
				"a<caret>bc"
			).executeSuccess(
				"x"
			).assertBuffer(
				"a<caret>c"
			).executeSuccess(
				"."
			).assertBuffer(
				"<caret>a"
			).executeSuccess(
				"."
			).assertBuffer(
				"<caret>"
			).executeSuccess(
				"uuu"
			).assertBuffer(
				"a<caret>bc"
			);
		}

		@Test
		public void dw()
		{
			createBuffer(
				"<caret>one two three"
			).executeSuccess(
				"dw"
			).assertBuffer(
				"<caret> two three" // different from vim (space is left)
			).executeSuccess(
				"."
			).assertBuffer(
				"<caret> three"
			).executeSuccess(
				"."
			).assertBuffer(
				"<caret>"
			).executeSuccess(
				"uuu"
			).assertBuffer(
				"<caret>one two three"
			);
		}

		/*
		@Test
		public void de()
		{
			createBuffer(
				"<caret>one two"
			).executeSuccess(
				"de"
			).assertBuffer(
				"<caret> two"
			).executeSuccess(
				"."
			).assertBuffer(
				"<caret>"
			);
		}

		@Test
		public void de_end()
		{
			createBuffer(
				"on<caret>e two"
			).executeSuccess(
				"de"
			).assertBuffer(
				"o<caret>n"
			).executeSuccess(
				"."
			).assertBuffer(
				"<caret>o"
			);
		}
		*/

		@Test
		public void db()
		{
			createBuffer(
				"one two <caret> "
			).executeSuccess(
				"db"
			).assertBuffer(
				"one <caret> "
			).executeSuccess(
				"."
			).assertBuffer(
				"<caret> "
			).executeFail(
				"."
			).executeSuccess(
				"uu"
			).assertBuffer(
				"one two <caret> "
			);
		}

		@Test
		public void diw()
		{
			createBuffer(
				"one two"
			).executeSuccess(
				"diw$."
			).assertBuffer(
				"<caret> "
			);
		}

		@Test
		public void dd()
		{
			createBuffer(
				"<caret>one",
				"two",
				"three"
			).executeSuccess(
				"dd."
			).assertBuffer(
				"<caret>three"
			);
		}

		@Test
		public void dj()
		{
			createBuffer(
				"<caret>one",
				"two",
				"three",
				"four",
				"five"
			).executeSuccess(
				"dj."
			).assertBuffer(
				"<caret>five"
			).executeFail(
				"."
			);
		}

		@Test
		public void dk()
		{
			createBuffer(
				"one",
				"two",
				"<caret>three",
				"four"
			).executeSuccess(
				"dk."
			).assertBuffer(
				"<caret>"
			).executeFail(
				"."
			);
		}

		@Test
		public void cw()
		{
			createBuffer(
				"<caret>one two"
			).executeSuccess(
				"cwxy<esc>"
			).assertBuffer(
				"x<caret>y two"
			).executeSuccess(
				"."
			).assertBuffer(
				"xx<caret>ytwo"
			).executeSuccess(
				"uu"
			).assertBuffer(
				"<caret>one two"
			);
		}

		@Test
		public void cw_end()
		{
			createBuffer(
				"on<caret>e two"
			).executeSuccess(
				"cwxy<esc>"
			).assertBuffer(
				"onx<caret>ytwo"
			).executeSuccess(
				"."
			).assertBuffer(
				"onxx<caret>y"
			).executeSuccess(
				"uu"
			).assertBuffer(
				"on<caret>e two"
			);
		}

		/*
		@Test
		public void ce()
		{
			createBuffer(
				"<caret>one two"
			).executeSuccess(
				"cexy<esc>"
			).assertBuffer(
				"x<caret>y two"
			).executeSuccess(
				"."
			).assertBuffer(
				"xx<caret>y"
			);
		}
		*/

		@Test
		public void cb()
		{
			createBuffer(
				"one tw<caret>o"
			).executeSuccess(
				"cbxy<esc>"
			).assertBuffer(
				"one x<caret>yo"
			).executeSuccess(
				"."
			).assertBuffer(
				"one x<caret>yyo"
			).executeSuccess(
				"uu"
			).assertBuffer(
				"one tw<caret>o"
			);
		}

		@Test
		public void ciw()
		{
			createBuffer(
				"the quick br<caret>own fox"
			).executeSuccess(
				"ciwblack<esc>ll."
			).assertBuffer(
				"the quick black blac<caret>k"
			).executeSuccess(
				"uu"
			).assertBuffer(
				"the quick br<caret>own fox"
			);
		}
	}

	/**
	 * Most previous test already test undo after their own tests, this thing tests more cases.
	 */
	public static class Undo
	{
		@Test
		public void insert_delete_line()
		{
			createBuffer(
				"one",
				"<caret>two"
			).executeSuccess(
				"i<bs><bs><bs><bs><esc>u"
			).assertBuffer(
				"one",
				"<caret>two"
			);
		}

		@Test
		public void insert_multiple_lines()
		{
			createBuffer(
				"one",
				"<caret>two"
			).executeSuccess(
				"i<bs><bs><bs><bs>\n\n\n<esc>u"
			).assertBuffer(
				"one",
				"<caret>two"
			);
		}

		@Test
		public void delete_multiple_lines()
		{
			createBuffer(
				"a",
				"b",
				"c",
				"<caret>d"
			).executeSuccess(
				"a<bs><bs><bs><bs><esc>"
			).assertBuffer(
				"a",
				"<caret>b"
			).executeSuccess(
				"u"
			).assertBuffer(
				"a",
				"b",
				"c",
				"<caret>d"
			);
		}
	}

	private static EditBufferTest createBuffer(String...lines)
	{
		EditBufferTest test = new EditBufferTest();
		int caretx = 0, carety = 0;
		for (int i = 0; i < lines.length; i++) {
			int idx = lines[i].indexOf("<caret>");
			if (idx != -1) {
				lines[i] = lines[i].substring(0, idx) + lines[i].substring(idx + 7);
				caretx = idx;
				carety = i;
			}
		}
		test.buf = new EditBuffer(new Jeanine(), String.join("\n", lines));
		test.buf.caretx = caretx;
		test.buf.carety = carety;
		return test;
	}

	private EditBuffer buf;

	private static void appendChar(StringBuilder sb, char c)
	{
		switch (c) {
		case 8: sb.append("<BS>"); return;
		case 27: sb.append("<ESC>"); return;
		case 127: sb.append("<DEL>"); return;
		default: sb.append(c); return;
		}
	}

	private static String translateCommand(String command)
	{
		command = command.replace("<esc>", "\033");
		command = command.replace("<bs>", "\010");
		command = command.replace("<del>", "\u007f");
		return command;
	}

	private EditBufferTest executeFail(String command)
	{
		KeyInput e = new KeyInput();
		for (char c : translateCommand(command).toCharArray()) {
			e.c = c;
			this.buf.handlePhysicalInput(e);
		}
		assertTrue("did not fail: " + command, e.error);
		return this;
	}

	private EditBufferTest executeSuccess(String command)
	{
		KeyInput e = new KeyInput();
		char[] chars = translateCommand(command).toCharArray();
		for (int i = 0; i < chars.length; i++) {
			e.c = chars[i];
			this.buf.handlePhysicalInput(e);
			if (e.error) {
				StringBuilder msg = new StringBuilder();
				msg.append("Failed to execute full command.");
				msg.append("\nExecuted: ");
				for (int j = 0; j < i; j++) {
					appendChar(msg, chars[j]);
				}
				msg.append("\nErrored on: ");
				appendChar(msg, chars[i]);
				fail(msg.toString());
			}
		}
		return this;
	}

	private EditBufferTest assertBuffer(String...expectedLines)
	{
		StringBuilder sb = new StringBuilder();
		String exp = String.join("\n", expectedLines);
		boolean needcaret = false;
		for (String line : expectedLines) {
			if (line.contains("<caret>")) {
				needcaret = true;
				break;
			}
		}
		String actual;
		if (needcaret) {
			for (int i = 0; i < this.buf.lines.size(); i++) {
				if (i != 0) {
					sb.append("\n");
				}
				StringBuilder line = this.buf.lines.get(i);
				if (this.buf.carety == i) {
					int caretx = this.buf.caretx;
					if (caretx < 0) {
						caretx = 0;
					}
					sb.append(line, 0, caretx);
					sb.append("<caret>");
					sb.append(line, caretx, line.length());
				} else {
					sb.append(line);
				}
			}
			actual = sb.toString();
			sb.setLength(0);
		} else {
			actual = String.join("\n", this.buf.lines);
		}
		int linediff = this.buf.lines.size() - expectedLines.length;
		if (linediff > 0) {
			sb.append("excess ").append(linediff).append(" line(s) in buffer");
			failComparison(sb.toString(), exp, actual);
		} else if (linediff < 0) {
			sb.append("missing ").append(-linediff).append(" line(s) in buffer");
			failComparison(sb.toString(), exp, actual);
		}
		for (int i = 0; i < expectedLines.length; i++) {
			StringBuilder line = this.buf.lines.get(i);
			int len = line.length();
			char[] chars = Util.getValue(line);
			for (int j = 0; j < len; j++) {
				if (chars[j] == '\n') {
					sb.append("Line ").append(i);
					sb.append(" has LF, lines shouldn't have LFs:");
					sb.append("\n>>>\n").append(this.buf.lines.get(i));
					fail(sb.toString());
				}
			}
		}
		if (!actual.equals(exp)) {
			failComparison("", exp, actual);
		}
		return this;
	}

	private static void failComparison(String message, String expected, String actual)
	{
		fail(message + "\nEXPECTED>>>\n" + expected + "\n===\n" + actual + "\n<<<ACTUAL");
	}
}
