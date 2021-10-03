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
		public void enter_copy_indentation()
		{
			createBuffer(
				"\t\tab<caret>c"
			).executeSuccess(
				"i\n<esc>"
			).assertBuffer(
				"\t\tab",
				"\t<caret>\tc"
			).executeSuccess(
				"u"
			).assertBuffer(
				"\t\tab<caret>c"
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

		@Test
		public void copy_indentation()
		{
			createBuffer(
				"<caret>\t\ta"
			).executeSuccess(
				"ohey<esc>"
			).assertBuffer(
				"\t\ta",
				"\t\the<caret>y"
			).executeSuccess(
				"u"
			).assertBuffer(
				"<caret>\t\ta"
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

		@Test
		public void copy_indentation()
		{
			createBuffer(
				"<caret>\t\ta"
			).executeSuccess(
				"Ohey<esc>"
			).assertBuffer(
				"\t\the<caret>y",
				"\t\ta"
			).executeSuccess(
				"u"
			).assertBuffer(
				"<caret>\t\ta"
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

		@Test
		public void d$()
		{
			createBuffer(
				"xyz<caret>def"
			).executeSuccess(
				"d$"
			).assertBuffer(
				"xy<caret>z"
			).executeSuccess(
				"u"
			).assertBuffer(
				"xyz<caret>def"
			);
		}

		@Test
		public void d$_empty()
		{
			createBuffer(
				"<caret>"
			).executeSuccess(
				"d$"
			).assertBuffer(
				"<caret>"
			).executeSuccess(
				"u"
			).assertBuffer(
				"<caret>"
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
		public void cc()
		{
			createBuffer(
				"on<caret>e two"
			).executeSuccess(
				"cchey<esc>"
			).assertBuffer(
				"he<caret>y"
			).executeSuccess(
				"u"
			).assertBuffer(
				"on<caret>e two"
			);
		}

		@Test
		public void cc_empty()
		{
			createBuffer(
				"<caret>"
			).executeSuccess(
				"cchey<esc>"
			).assertBuffer(
				"he<caret>y"
			).executeSuccess(
				"u"
			).assertBuffer(
				"<caret>"
			);
		}

		@Test
		public void cc_with_indent()
		{
			createBuffer(
				"\t<caret>\t"
			).executeSuccess(
				"cchey<esc>"
			).assertBuffer(
				"\t\the<caret>y"
			).executeSuccess(
				"u"
			).assertBuffer(
				"\t<caret>\t"
			);
		}

		@Test
		public void c$()
		{
			createBuffer(
				"\t<caret>\they"
			).executeSuccess(
				"c$okay<esc>"
			).assertBuffer(
				"\toka<caret>y"
			).executeSuccess(
				"u"
			).assertBuffer(
				"\t<caret>\they"
			);
		}
	}

	public static class ChangeIn
	{
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

		@Test
		public void ciw_empty()
		{
			createBuffer(
				"<caret>"
			).executeSuccess(
				"ciwxy<esc>"
			).assertBuffer(
				"x<caret>y"
			).executeSuccess(
				"u"
			).assertBuffer(
				"<caret>"
			);
		}

		@Test
		public void ciquote()
		{
			createBuffer(
				"okay 'here <caret>it' goes"
			).executeSuccess(
				"ci'what<esc>"
			).assertBuffer(
				"okay 'wha<caret>t' goes"
			).executeSuccess(
				"u"
			).assertBuffer(
				"okay 'here <caret>it' goes"
			);
		}

		@Test
		public void ciquote_caret_left()
		{
			createBuffer(
				"okay <caret>'here it' goes"
			).executeSuccess(
				"ci'what<esc>"
			).assertBuffer(
				"okay 'wha<caret>t' goes"
			).executeSuccess(
				"u"
			).assertBuffer(
				"okay <caret>'here it' goes"
			);
		}

		@Test
		public void ciquote_caret_right()
		{
			createBuffer(
				"okay 'here it<caret>' goes"
			).executeSuccess(
				"ci'what<esc>"
			).assertBuffer(
				"okay 'wha<caret>t' goes"
			).executeSuccess(
				"u"
			).assertBuffer(
				"okay 'here it<caret>' goes"
			);
		}

		@Test
		public void ciquote_lonely()
		{
			createBuffer(
				"'here <caret>it'"
			).executeSuccess(
				"ci'what<esc>"
			).assertBuffer(
				"'wha<caret>t'"
			).executeSuccess(
				"u"
			).assertBuffer(
				"'here <caret>it'"
			);
		}

		@Test
		public void ciquote_lonely_caret_left()
		{
			createBuffer(
				"<caret>'here it'"
			).executeSuccess(
				"ci'what<esc>"
			).assertBuffer(
				"'wha<caret>t'"
			).executeSuccess(
				"u"
			).assertBuffer(
				"<caret>'here it'"
			);
		}

		@Test
		public void ciquote_lonely_caret_right()
		{
			createBuffer(
				"'here it<caret>'"
			).executeSuccess(
				"ci'what<esc>"
			).assertBuffer(
				"'wha<caret>t'"
			).executeSuccess(
				"u"
			).assertBuffer(
				"'here it<caret>'"
			);
		}

		@Test
		public void ciquote_use_closest_sameline_left()
		{
			createBuffer(
				"abc 'd<caret>'ef' gh"
			).executeSuccess(
				"ci'x<esc>"
			).assertBuffer(
				"abc '<caret>x'ef' gh"
			).executeSuccess(
				"u"
			).assertBuffer(
				"abc 'd<caret>'ef' gh"
			);
		}

		@Test
		public void ciquote_multiline()
		{
			createBuffer(
				"here 'it<caret>",
				"goes'"
			).executeSuccess(
				"ci'what<esc>"
			).assertBuffer(
				"here 'wha<caret>t'"
			).executeSuccess(
				"u"
			).assertBuffer(
				"here 'it<caret>",
				"goes'"
			);
		}

		@Test
		public void ciquote_multiline2()
		{
			createBuffer(
				"' start",
				"<caret>",
				"here ' end"
			).executeSuccess(
				"ci'what<esc>"
			).assertBuffer(
				"'wha<caret>t' end"
			).executeSuccess(
				"u"
			).assertBuffer(
				"' start",
				"<caret>",
				"here ' end"
			);
		}

		@Test
		public void ciquote_use_closest_sameline_right()
		{
			createBuffer(
				"abc 'de<caret>'f' gh"
			).executeSuccess(
				"ci'x<esc>"
			).assertBuffer(
				"abc 'de'<caret>x' gh"
			).executeSuccess(
				"u"
			).assertBuffer(
				"abc 'de<caret>'f' gh"
			);
		}

		@Test
		public void ciquote_use_closest_otherline_left()
		{
			createBuffer(
				"'fxxxyyyzzzzzzzzzzz<caret>'",
				"'abc"
			).executeSuccess(
				"ci'x<esc>"
			).assertBuffer(
				"'<caret>x'",
				"'abc"
			).executeSuccess(
				"u"
			).assertBuffer(
				"'fxxxyyyzzzzzzzzzzz<caret>'",
				"'abc"
			);
		}

		@Test
		public void ciquote_use_closest_otherline_right()
		{
			createBuffer(
				"abc'",
				"<caret>'fxxxyyyzzzzzzzzzzz'"
			).executeSuccess(
				"ci'x<esc>"
			).assertBuffer(
				"abc'",
				"'<caret>x'"
			).executeSuccess(
				"u"
			).assertBuffer(
				"abc'",
				"<caret>'fxxxyyyzzzzzzzzzzz'"
			);
		}

		// Not testing all combinations. Quote has many tests with common behavior,
		// just adding some tests with bracket here to see behavior when left and right
		// are different.

		@Test
		public void cibracket()
		{
			createBuffer(
				"okay [here <caret>it] goes"
			).executeSuccess(
				"ci[what<esc>"
			).assertBuffer(
				"okay [wha<caret>t] goes"
			).executeSuccess(
				"u"
			).assertBuffer(
				"okay [here <caret>it] goes"
			);
		}

		@Test
		public void cibracket_pick_correct_one()
		{
			createBuffer(
				"okay [here ]<caret>it]]] goes"
			).executeSuccess(
				"ci[what<esc>"
			).assertBuffer(
				"okay [wha<caret>t]]] goes"
			).executeSuccess(
				"u"
			).assertBuffer(
				"okay [here ]<caret>it]]] goes"
			);
		}
	}

	public static class DeleteIn
	{
		// The behavior tested in ChangeIn also counts for DeleteIn

		@Test
		public void diquote()
		{
			createBuffer(
				"okay 'here <caret>it' goes"
			).executeSuccess(
				"di'"
			).assertBuffer(
				"okay '<caret>' goes"
			).executeSuccess(
				"u"
			).assertBuffer(
				"okay 'here <caret>it' goes"
			);
		}
	}

	public static class Substitute
	{
		@Test
		public void beginning()
		{
			createBuffer(
				"<caret>hey"
			).executeSuccess(
				"sxxx<esc>"
			).assertBuffer(
				"xx<caret>xey"
			).executeSuccess(
				"u"
			).assertBuffer(
				"<caret>hey"
			);
		}

		@Test
		public void middle()
		{
			createBuffer(
				"h<caret>ey"
			).executeSuccess(
				"sxxx<esc>"
			).assertBuffer(
				"hxx<caret>xy"
			).executeSuccess(
				"u"
			).assertBuffer(
				"h<caret>ey"
			);
		}

		@Test
		public void end()
		{
			createBuffer(
				"he<caret>y"
			).executeSuccess(
				"sxxx<esc>"
			).assertBuffer(
				"hexx<caret>x"
			).executeSuccess(
				"u"
			).assertBuffer(
				"he<caret>y"
			);
		}
	}

	public static class Replace
	{
		@Test
		public void simple()
		{
			createBuffer(
				"a<caret>bc"
			).executeSuccess(
				"rx"
			).assertBuffer(
				"a<caret>xc"
			).executeSuccess(
				"u"
			).assertBuffer(
				"a<caret>bc"
			);
		}

		@Test
		public void disallow_on_empty_line()
		{
			createBuffer(
				"<caret>"
			).executeFail(
				"r"
			);
		}
	}

	public static class Join
	{
		@Test
		public void simple()
		{
			createBuffer(
				"one <caret>two",
				"three four"
			).executeSuccess(
				"J"
			).assertBuffer(
				"one two<caret> three four"
			).executeSuccess(
				"u"
			).assertBuffer(
				"one <caret>two",
				"three four"
			);
		}

		@Test
		public void trim_leading_ws()
		{
			createBuffer(
				"one <caret>two",
				"\t  \tthree four"
			).executeSuccess(
				"J"
			).assertBuffer(
				"one two<caret> three four"
			).executeSuccess(
				"u"
			).assertBuffer(
				"one <caret>two",
				"\t  \tthree four"
			);
		}

		@Test
		public void next_line_empty()
		{
			createBuffer(
				"one <caret>two",
				""
			).executeSuccess(
				"J"
			).assertBuffer(
				"one tw<caret>o"
			).executeSuccess(
				"u"
			).assertBuffer(
				"one <caret>two",
				""
			);
		}

		@Test
		public void next_line_ws()
		{
			createBuffer(
				"one <caret>two",
				"\t  \t  "
			).executeSuccess(
				"J"
			).assertBuffer(
				"one tw<caret>o"
			).executeSuccess(
				"u"
			).assertBuffer(
				"one <caret>two",
				"\t  \t  "
			);
		}

		@Test
		public void fail_when_last()
		{
			createBuffer(
				"one <caret>two"
			).executeFail(
				"J"
			);
		}
	}

	public static class Indent
	{
		@Test
		public void dont_add_when_empty()
		{
			createBuffer(
				"<caret>"
			).executeSuccess(
				">>"
			).assertBuffer(
				"<caret>"
			);
		}

		@Test
		public void add()
		{
			createBuffer(
				"<caret>hi"
			).executeSuccess(
				">>"
			).assertBuffer(
				"\t<caret>hi"
			).executeSuccess(
				">>"
			).assertBuffer(
				"\t\t<caret>hi"
			).executeSuccess(
				"u"
			).assertBuffer(
				"\t<caret>hi"
			).executeSuccess(
				"u"
			).assertBuffer(
				"<caret>hi"
			);
		}

		@Test
		public void remove()
		{
			createBuffer(
				"  \t<caret>hi"
			).executeSuccess(
				"<<"
			).assertBuffer(
				"\t<caret>hi"
			).executeSuccess(
				"<<"
			).assertBuffer(
				"<caret>hi"
			).executeSuccess(
				"<<"
			).assertBuffer(
				"<caret>hi"
			).executeSuccess(
				"u"
			).assertBuffer(
				"\t<caret>hi"
			).executeSuccess(
				"u"
			).assertBuffer(
				"  \t<caret>hi"
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

		@Test
		public void indent()
		{
			createBuffer(
				"<caret>a",
				"",
				"c",
				"\td"
			).executeSuccess(
				"Vjjj>"
			).assertBuffer(
				"<caret>\ta",
				"",
				"\tc",
				"\t\td"
			).executeSuccess(
				"."
			).assertBuffer(
				"<caret>\t\ta",
				"",
				"\t\tc",
				"\t\t\td"
			).executeSuccess(
				"u"
			).assertBuffer(
				"<caret>\ta",
				"",
				"\tc",
				"\t\td"
			).executeSuccess(
				"u"
			).assertBuffer(
				"<caret>a",
				"",
				"c",
				"\td"
			);
		}

		@Test
		public void deindent()
		{
			createBuffer(
				"<caret>a",
				"\tb",
				"\t\tc",
				"           d"
			).executeSuccess(
				"Vjjj<"
			).assertBuffer(
				"<caret>a",
				"b",
				"\tc",
				"   d"
			).executeSuccess(
				"."
			).assertBuffer(
				"<caret>a",
				"b",
				"c",
				"d"
			).executeSuccess(
				"u"
			).assertBuffer(
				"<caret>a",
				"b",
				"\tc",
				"   d"
			).executeSuccess(
				"u"
			).assertBuffer(
				"<caret>a",
				"\tb",
				"\t\tc",
				"           d"
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

		@Test
		public void r()
		{
			createBuffer(
				"<caret>abcdefx"
			).executeSuccess(
				"rzl.l.l."
			).assertBuffer(
				"zzz<caret>zefx"
			).executeSuccess(
				"uuuu"
			).assertBuffer(
				"<caret>abcdefx"
			);
		}

		@Test
		public void s()
		{
			createBuffer(
				"<caret>abc",
				"def"
			).executeSuccess(
				"sxx<esc>"
			).assertBuffer(
				"x<caret>xbc",
				"def"
			).executeSuccess(
				"j."
			).assertBuffer(
				"xxbc",
				"dx<caret>xf"
			).executeSuccess(
				"uu"
			).assertBuffer(
				"<caret>abc",
				"def"
			);
		}

		@Test
		public void J()
		{
			createBuffer(
				"<caret>abc",
				"def",
				"ghi"
			).executeSuccess(
				"J."
			).assertBuffer(
				"abc def<caret> ghi"
			).executeSuccess(
				"uu"
			).assertBuffer(
				"<caret>abc",
				"def",
				"ghi"
			);
		}

		@Test
		public void indent()
		{
			createBuffer(
				"<caret>abc"
			).executeSuccess(
				">>."
			).assertBuffer(
				"\t\t<caret>abc"
			).executeSuccess(
				"uu"
			).assertBuffer(
				"<caret>abc"
			);
		}

		@Test
		public void deindent()
		{
			createBuffer(
				"\t  <caret>abc"
			).executeSuccess(
				"<<.."
			).assertBuffer(
				"<caret>abc"
			).executeSuccess(
				"uu"
			).assertBuffer(
				"\t  <caret>abc"
			);
		}
	}

	public static class MoveParagraph
	{
		@Test
		public void up()
		{
			createBuffer(
				"a",
				"",
				"<caret>b"
			).executeSuccess(
				"{"
			).assertBuffer(
				"a",
				"<caret>",
				"b"
			);
		}

		@Test
		public void up_but_no_target_should_go_to_first_line()
		{
			createBuffer(
				"a",
				"<caret>b"
			).executeSuccess(
				"{"
			).assertBuffer(
				"<caret>a",
				"b"
			);
		}

		@Test
		public void up_while_at_first_line_is_nop()
		{
			createBuffer(
				"<caret>b",
				"a"
			).executeSuccess(
				"{"
			).assertBuffer(
				"<caret>b",
				"a"
			);
		}

		@Test
		public void down()
		{
			createBuffer(
				"<caret>b",
				"",
				"a"
			).executeSuccess(
				"}"
			).assertBuffer(
				"b",
				"<caret>",
				"a"
			);
		}

		@Test
		public void down_but_no_target_should_go_to_last_line()
		{
			createBuffer(
				"<caret>b",
				"x",
				"a"
			).executeSuccess(
				"}"
			).assertBuffer(
				"b",
				"x",
				"<caret>a"
			);
		}

		@Test
		public void down_while_at_last_line_is_nop()
		{
			createBuffer(
				"a",
				"<caret>b"
			).executeSuccess(
				"}"
			).assertBuffer(
				"a",
				"<caret>b"
			);
		}

		@Test
		public void should_keep_virtual_x()
		{
			createBuffer(
				"the quick",
				"",
				"brown fox jumps",
				"",
				"over the laz<caret>y dog"
			).executeSuccess(
				"{"
			).assertBuffer(
				"the quick",
				"",
				"brown fox jumps",
				"<caret>",
				"over the lazy dog"
			).executeSuccess(
				"{"
			).assertBuffer(
				"the quick",
				"<caret>",
				"brown fox jumps",
				"",
				"over the lazy dog"
			).executeSuccess(
				"{"
			).assertBuffer(
				"the quic<caret>k",
				"",
				"brown fox jumps",
				"",
				"over the lazy dog"
			).executeSuccess(
				"}"
			).assertBuffer(
				"the quick",
				"<caret>",
				"brown fox jumps",
				"",
				"over the lazy dog"
			).executeSuccess(
				"}"
			).assertBuffer(
				"the quick",
				"",
				"brown fox jumps",
				"<caret>",
				"over the lazy dog"
			).executeSuccess(
				"}"
			).assertBuffer(
				"the quick",
				"",
				"brown fox jumps",
				"",
				"over the laz<caret>y dog"
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

		@Test
		public void type_and_bs()
		{
			createBuffer(
				"abc<caret>def"
			).executeSuccess(
				"ihey<bs><bs><bs>x<esc>"
			).assertBuffer(
				"abc<caret>xdef"
			).executeSuccess(
				"u"
			).assertBuffer(
				"abc<caret>def"
			);
		}

		@Test
		public void bs_and_type()
		{
			createBuffer(
				"one",
				"abc<caret>def"
			).executeSuccess(
				"i<bs><bs><bs><bs><bs>xy<bs><bs><bs><esc>"
			).assertBuffer(
				"<caret>odef"
			).executeSuccess(
				"u"
			).assertBuffer(
				"one",
				"abc<caret>def"
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
		test.buf = new EditBuffer(new Jeanine(), null);
		test.buf.lines.clear();
		for (String line : lines) {
			test.buf.lines.add(new SB(line));
		}
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
		SB sb = new SB();
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
					sb.append('\n');
				}
				SB line = this.buf.lines.get(i);
				if (this.buf.carety == i) {
					int caretx = this.buf.caretx;
					if (caretx < 0) {
						caretx = 0;
					}
					sb.append(line.value, 0, caretx);
					sb.append("<caret>");
					sb.append(line.value, caretx, line.length);
				} else {
					sb.append(line);
				}
			}
			actual = sb.toString();
			sb.setLength(0);
		} else {
			actual = String.join("\n", this.buf.lines.lines);
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
			SB line = this.buf.lines.get(i);
			for (int j = 0; j < line.length; j++) {
				if (line.value[j] == '\n') {
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
