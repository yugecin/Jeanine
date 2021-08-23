package net.basdon.jeanine;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;

public class EditBuffer
{
	public static final char
		BS = 8,
		ESC = 27,
		DEL = 127;
	public static final int
		NORMAL_MODE = 0,
		INSERT_MODE = 1,
		CHANGE_MODE = 2,
		CHANGE_IN_MODE = 3,
		DELETE_MODE = 4,
		DELETE_IN_MODE = 5,
		G_MODE = 6,
		SELECT_LINE_MODE = 7;

	private final Jeanine j;
	public final ArrayList<StringBuilder> lines;

	private boolean creatingCommand;
	private char storedCmdBuf[];
	private int storedCommandLength;
	private char creatingCmdBuf[];
	private int creatingCmdLength;
	/**
	 * This is always logical pos (disregarding char/tab width).
	 */
	public int caretx;
	public int carety;
	private int lineselectinitial;
	public int lineselectfrom;
	/**exclusive*/
	public int lineselectto;
	/**
	 * Stores the last visual caretx to use when we were on a line that has
	 * less chars than the previous one.
	 * ie:
	 * <pre>
	 * abcdefg[caret] caret=7 virtualcaret=7
	 * abc
	 * abcdefghijkl
	 * // go down
	 * abcdefg
	 * abc[caret] caret=3 virtualcaret=7
	 * abcdefghijkl
	 * // go down
	 * abcdefg
	 * abc
	 * abcdefg[caret]hijkl caret=7 virtualcaret=7
	 * </pre>
	 * Note that it is always visual pos.
	 */
	private int virtualCaretx;
	public int mode;
	private ArrayList<UndoStuff> undostuff = new ArrayList<>();
	private int undostuffptr;
	/**
	 * The undo stuff that is currently being written (ie because we're in insert mode).
	 */
	private UndoStuff writingUndo;

	public static class UndoStuff
	{
		/**Start position x of the content that should be replaced with
		{@link #replacement} when performing the undo.*/
		public int fromx;
		/**Start position y of the content that should be replaced with
		{@link #replacement} when performing the undo.*/
		public int fromy;
		/**exclusive*/
		public int tox;
		public int toy;
		/**Caret position before this operation
		(can be different from {@link #fromx} eg when the O command was used).*/
		public int caretx;
		/**Caret position before this operation
		(can be different from {@link #fromy} eg when the O command was used).*/
		public int carety;
		public StringBuilder replacement = new StringBuilder();
		public boolean isBlock;

		public UndoStuff(int caretx, int carety)
		{
			this.tox = this.fromx = this.caretx = caretx;
			this.toy = this.fromy = this.carety = carety;
		}
	}

	public EditBuffer(Jeanine j, String text)
	{
		this.j = j;
		this.storedCmdBuf = new char[100];
		this.creatingCmdBuf = new char[100];
		this.lines = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		this.lines.add(sb);
		if (text != null) {
			for (char c : text.toCharArray()) {
				if (c == '\n') {
					sb = new StringBuilder();
					this.lines.add(sb);
				} else {
					sb.append(c);
				}
			}
		}
	}

	private int curVisualX()
	{
		int visual = Line.logicalToVisualPos(this.lines.get(this.carety), this.caretx);
		if (this.virtualCaretx > visual) {
			return this.virtualCaretx;
		}
		return visual;
	}

	private void putCaretX(int visual)
	{
		StringBuilder line = this.lines.get(this.carety);
		if (visual > line.length()) {
			if (visual > this.virtualCaretx) {
				this.virtualCaretx = visual;
			}
			this.caretx = line.length() - 1;
			if (this.caretx < 0) {
				this.caretx = 0;
			}
		} else {
			this.caretx = Line.visualToLogicalPos(line, visual);
		}
	}

	private void addCurrentWritingUndo()
	{
		Util.setArrayListSize(this.undostuff, this.undostuffptr);
		this.undostuff.add(this.writingUndo);
		this.undostuffptr++;
		this.writingUndo = null;
	}

	private void handleInputNormal(KeyInput e)
	{
		StringBuilder line;
		int len, visual;
		Point pt;
		int prevCaretx = this.caretx;
		int prevCarety = this.carety;
		switch (e.c) { // break when the key starts a new command (for .), return otherwise
		case 'c':
			if (this.caretx >= this.lines.get(this.carety).length()) {
				e.error = true;
				return;
			}
			this.mode = CHANGE_MODE;
			break;
		case 'd':
			if (this.caretx > this.lines.get(this.carety).length()) {
				e.error = true;
				return;
			}
			this.mode = DELETE_MODE;
			break;
		case 'u':
			if (this.undostuffptr == 0) {
				e.error = true;
			} else {
				this.undostuffptr--;
				UndoStuff u = this.undostuff.get(this.undostuffptr);
				line = this.lines.get(u.fromy);
				char[] toappend;
				if (u.toy > u.fromy) {
					line.setLength(u.fromx);
					StringBuilder lastline = this.lines.get(u.toy);
					int pos = u.tox;
					for (int y = u.toy; y > u.fromy; y--) {
						this.lines.remove(y);
					}
					toappend = new char[lastline.length() - pos];
					lastline.getChars(pos, lastline.length(), toappend, 0);
				} else {
					if (line.length() == 0) {
						toappend = new char[0];
					} else {
						toappend = new char[line.length() - u.tox];
						line.getChars(u.tox, line.length(), toappend, 0);
					}
					line.setLength(u.fromx);
				}
				// put in replacement
				int idx = u.replacement.indexOf("\n");
				int from = 0, nextfrom = 0;
				int linenr = u.fromy + 1;
				while (idx != -1) {
					from = idx + 1;
					line.append(u.replacement, nextfrom, idx);
					nextfrom = idx + 1;
					idx = u.replacement.indexOf("\n", idx + 1);
					line = new StringBuilder();
					this.lines.add(linenr, line);
					linenr++;
				}
				line.append(u.replacement, from, u.replacement.length());
				// trailing existing part
				line.append(toappend, 0, toappend.length);
				this.caretx = u.caretx;
				this.carety = u.carety;
				this.virtualCaretx = this.caretx;
				e.needRepaint = true;
				e.needCheckLineLength = true;
				e.needEnsureViewSize = true;
			}
			return;
		case 'o':
			this.writingUndo = new UndoStuff(this.caretx, this.carety);
			this.writingUndo.fromx = this.lines.get(this.carety).length();
			this.writingUndo.fromy = this.carety;
			this.writingUndo.tox = 0;
			this.writingUndo.toy = this.carety + 1;
			this.carety++;
			this.lines.add(this.carety, new StringBuilder());
			this.caretx = 0;
			this.mode = INSERT_MODE;
			e.needRepaint = true;
			e.needEnsureViewSize = true;
			break;
		case 'O':
			this.writingUndo = new UndoStuff(this.caretx, this.carety);
			this.writingUndo.fromx = 0;
			this.writingUndo.fromy = this.carety;
			this.writingUndo.tox = 0;
			this.writingUndo.toy = this.carety + 1;
			this.lines.add(this.carety, new StringBuilder());
			this.caretx = 0;
			this.mode = INSERT_MODE;
			e.needRepaint = true;
			e.needEnsureViewSize = true;
			break;
		case 'g':
			this.mode = G_MODE;
			break;
		case 'G':
			visual = this.curVisualX();
			this.carety = this.lines.size() - 1;
			this.putCaretX(visual);
			e.needRepaintCaret = true;
			break;
		case 'I':
			int pos = 0;
			for (char chr : this.lines.get(this.carety).toString().toCharArray()) {
				if (chr != ' ' && chr != '\t') {
					break;
				}
				pos++;
			}
			this.writingUndo = new UndoStuff(this.caretx, this.carety);
			this.writingUndo.fromx = this.writingUndo.tox = pos;
			this.caretx = pos;
			this.mode = INSERT_MODE;
			e.needRepaintCaret = true;
			break;
		case 'i':
			this.mode = INSERT_MODE;
			e.needRepaintCaret = true;
			this.writingUndo = new UndoStuff(this.caretx, this.carety);
			break;
		case 'A':
			this.caretx = this.lines.get(this.carety).length();
			this.mode = INSERT_MODE;
		case 'a':
			this.writingUndo = new UndoStuff(prevCaretx, prevCarety);
			if (this.caretx < this.lines.get(this.carety).length()) {
				this.caretx++;
			}
			this.writingUndo.fromx = this.caretx;
			this.writingUndo.tox = this.caretx;
			this.mode = INSERT_MODE;
			e.needRepaintCaret = true;
			break;
		case '^':
			this.caretx = this.virtualCaretx = 0;
			e.needRepaintCaret = true;
			return;
		case '$':
			this.caretx = this.lines.get(this.carety).length() - 1;
			if (this.caretx < 0) {
				this.caretx = 0;
			}
			this.virtualCaretx = Integer.MAX_VALUE;
			e.needRepaintCaret = true;
			return;
		case 'x':
			line = this.lines.get(this.carety);
			len = line.length();
			if (len == 0) {
				e.error = true;
				return;
			}
			char[] dst = new char[1];
			line.getChars(this.caretx, this.caretx + 1, dst, 0);
			line.delete(this.caretx, this.caretx + 1);
			this.j.pastebuffer = new String(dst);
			this.writingUndo = new UndoStuff(this.caretx, this.carety);
			this.writingUndo.replacement = new StringBuilder();
			this.writingUndo.replacement.append(dst[0]);
			this.addCurrentWritingUndo();
			if (this.caretx >= len - 1 && this.caretx > 0) {
				this.caretx--;
				this.virtualCaretx = Line.logicalToVisualPos(line, this.caretx);
			}
			e.needCheckLineLength = true;
			e.needEnsureViewSize = true;
			e.needRepaint = true;
			break;
		case 'p':
			this.writingUndo = new UndoStuff(this.caretx, this.carety);
			if (this.j.pastebuffer.endsWith("\n")) {
				this.writingUndo.fromx = this.lines.get(this.carety).length();
				String lines[] = this.j.pastebuffer.split("\n");
				for (int i = lines.length - 1; i >= 0; i--) {
					this.lines.add(this.carety + 1, new StringBuilder(lines[i]));
				}
				this.writingUndo.tox = lines[lines.length - 1].length();
				this.writingUndo.toy = this.carety + lines.length;
				this.carety++;
				this.virtualCaretx = this.caretx = 0;
			} else {
				// TODO: paste with newlines
				line = this.lines.get(this.carety);
				if (line.length() == 0) {
					line.insert(0, this.j.pastebuffer);
					this.writingUndo.fromx = 0;
				} else {
					line.insert(this.caretx + 1, this.j.pastebuffer);
					this.writingUndo.fromx = this.caretx + 1;
				}
				len = this.j.pastebuffer.length();
				this.writingUndo.tox = this.writingUndo.fromx + len;
				this.caretx += this.j.pastebuffer.length();
				this.virtualCaretx = Line.logicalToVisualPos(line, this.caretx);
			}
			this.addCurrentWritingUndo();
			e.needCheckLineLength = true;
			e.needEnsureViewSize = true;
			e.needRepaint = true;
			break;
		case 'P':
			this.writingUndo = new UndoStuff(this.caretx, this.carety);
			if (this.j.pastebuffer.endsWith("\n")) {
				String lines[] = this.j.pastebuffer.split("\n");
				for (int i = lines.length - 1; i >= 0; i--) {
					this.lines.add(this.carety, new StringBuilder(lines[i]));
				}
				this.virtualCaretx = this.caretx = 0;
				this.writingUndo.fromx = 0;
				this.writingUndo.toy = this.carety + lines.length;
				this.writingUndo.tox = 0;
			} else {
				// TODO: paste with newlines
				len = this.j.pastebuffer.length();
				line = this.lines.get(this.carety);
				line.insert(this.caretx, this.j.pastebuffer);
				this.writingUndo.tox = this.writingUndo.fromx + len;
				this.caretx += len - 1;
				this.virtualCaretx = Line.logicalToVisualPos(line, this.caretx);
			}
			this.addCurrentWritingUndo();
			e.needCheckLineLength = true;
			e.needEnsureViewSize = true;
			e.needRepaint = true;
			this.creatingCmdBuf[0] = e.c; this.creatingCmdLength = 1;
			break;
		case 'h':
			if (this.caretx > 0) {
				this.caretx--;
				line = this.lines.get(this.carety);
				this.virtualCaretx = Line.logicalToVisualPos(line, this.caretx);
				e.needRepaintCaret = true;
			} else {
				e.error = true;
			}
			return;
		case 'j':
			if (this.carety < this.lines.size() - 1) {
				visual = this.curVisualX();
				this.carety++;
				this.putCaretX(visual);
				e.needRepaintCaret = true;
			} else {
				e.error = true;
			}
			return;
		case 'k':
			if (this.carety > 0) {
				visual = this.curVisualX();
				this.carety--;
				this.putCaretX(visual);
				e.needRepaintCaret = true;
			} else {
				e.error = true;
			}
			return;
		case 'l':
			line = this.lines.get(this.carety);
			if (this.caretx < line.length() - 1) {
				this.caretx++;
				this.virtualCaretx = Line.logicalToVisualPos(line, this.caretx);
				e.needRepaintCaret = true;
			} else {
				e.error = true;
			}
			return;
		case 'e':
			pt = VimOps.forwards(this.lines, this.caretx, this.carety);
			if (pt.x == this.caretx && pt.y == this.carety) {
				e.error = true;
			} else {
				this.caretx = pt.x;
				this.carety = pt.y;
				e.needRepaintCaret = true;
			}
			return;
		case 'w':
			pt = VimOps.forwardsEx(this.lines, this.caretx, this.carety);
			if (pt.x == this.caretx && pt.y == this.carety) {
				e.error = true;
			} else {
				this.caretx = pt.x;
				this.carety = pt.y;
				e.needRepaintCaret = true;
			}
			return;
		case 'b':
			pt = VimOps.backwards(this.lines, this.caretx, this.carety);
			if (pt.x == this.caretx && pt.y == this.carety) {
				e.error = true;
			} else {
				this.caretx = pt.x;
				this.carety = pt.y;
				e.needRepaintCaret = true;
			}
			return;
		case '.':
			if (this.storedCommandLength == 0) {
				e.error = true;
			} else {
				this.replayKeys(this.storedCmdBuf, this.storedCommandLength, e);
			}
			return;
		case 'V':
			this.lineselectinitial = this.lineselectfrom = this.carety;
			this.lineselectto = this.carety + 1;
			this.mode = SELECT_LINE_MODE;
			e.needRepaint = true;
			break;
		default:
			e.error = true;
			return;
		}
		this.creatingCommand = true;
		this.creatingCmdLength = 0;
	}

	private void handleInputInsert(KeyInput e)
	{
		StringBuilder line;
		switch (e.c) {
		case BS:
			e.needCheckLineLength = true;
			e.needEnsureViewSize = true;
			if (this.caretx > 0) {
				this.caretx--;
				line = this.lines.get(this.carety);
				this.writingUndo.replacement.insert(0, line.charAt(this.caretx));
				if (this.carety == this.writingUndo.fromy) {
					this.writingUndo.fromx--;
				}
				if (this.carety == this.writingUndo.toy) {
					this.writingUndo.tox--;
				}
				line.delete(this.caretx, this.caretx + 1);
			} else if (this.carety > 0) {
				this.writingUndo.replacement.insert(0, '\n');
				String linecontent = this.lines.get(this.carety).toString();
				this.lines.remove(this.carety);
				StringBuilder prev = this.lines.get(this.carety - 1);
				this.caretx = prev.length();
				if (this.writingUndo.fromy == this.carety) {
					this.writingUndo.fromy--;
					this.writingUndo.fromx = this.caretx;
				}
				if (this.writingUndo.toy == this.carety && this.writingUndo.tox == 0) {
					this.writingUndo.toy--;
					this.writingUndo.tox = this.caretx;
				}
				this.carety--;
				this.writingUndo.fromx = this.caretx;
				prev.append(linecontent);
			} else {
				e.error = true;
				return;
			}
			break;
		case ESC:
			this.mode = NORMAL_MODE;
			if (this.caretx > 0) {
				this.caretx--;
			}
			this.addCurrentWritingUndo();
			line = this.lines.get(this.carety);
			this.virtualCaretx = Line.logicalToVisualPos(line, this.caretx);
			e.needRepaintCaret = true;
			return;
		case DEL:
			line = this.lines.get(this.carety);
			e.needCheckLineLength = true;
			e.needEnsureViewSize = true;
			if (this.caretx < line.length()) {
				this.writingUndo.replacement.append(line.charAt(this.caretx));
				line.delete(this.caretx, this.caretx + 1);
			} else if (this.carety + 1 < this.lines.size()) {
				this.writingUndo.replacement.append("\n");
				line.append(this.lines.remove(this.carety + 1));
			} else {
				e.error = true;
				return;
			}
			break;
		case '\r':
		case '\n':
			if (this.writingUndo.toy >= this.carety) {
				if (this.writingUndo.toy == this.carety) {
					this.writingUndo.tox = 0;
				}
				this.writingUndo.toy++;
			}
			line = this.lines.get(this.carety);
			char[] dst = new char[line.length() - this.caretx];
			line.getChars(this.caretx, line.length(), dst, 0);
			line.delete(this.caretx, line.length());
			this.lines.add(++this.carety, new StringBuilder().append(dst));
			this.caretx = this.virtualCaretx = 0;
			e.needCheckLineLength = true;
			e.needEnsureViewSize = true;
			break;
		default:
			if (this.writingUndo.toy == this.carety) {
				this.writingUndo.tox++;
			}
			line = this.lines.get(this.carety);
			line.insert(this.caretx, e.c);
			this.virtualCaretx = Line.logicalToVisualPos(line, ++this.caretx);
			e.needCheckLineLength = true;
			e.needEnsureViewSize = true;
		}
		e.needRepaint = true;
	}

	private void handleInputChangeDelete(KeyInput e, int next_mode, int next_in_mode)
	{
		StringBuilder line;
		char[] dst;
		Point pt;
		int from, to;
		switch (e.c) {
		case ESC:
			this.mode = NORMAL_MODE;
			return;
		case 'b':
			pt = VimOps.backwards(this.lines, this.caretx, this.carety);
			if (pt.x == this.caretx && pt.y == this.carety) {
				break;
			}
			line = this.lines.get(pt.y);
			if (pt.y != this.carety) {
				to = line.length();
			} else {
				to = this.caretx;
			}
			dst = new char[to - pt.x];
			line.getChars(pt.x, to, dst, 0);
			line.delete(pt.x, to);
			this.j.pastebuffer = new String(dst);
			this.writingUndo = new UndoStuff(this.caretx, this.carety);
			this.writingUndo.fromx = pt.x;
			this.writingUndo.fromy = this.carety;
			this.writingUndo.tox = pt.x;
			this.writingUndo.toy = this.carety;
			this.writingUndo.replacement.append(dst);
			if (next_mode == NORMAL_MODE) {
				this.addCurrentWritingUndo();
			}
			this.caretx = pt.x;
			this.carety = pt.y;
			this.mode = next_mode;
			e.needCheckLineLength = true;
			e.needEnsureViewSize = true;
			e.needRepaint = true;
			return;
		case 'w':
			line = this.lines.get(this.carety);
			char clss = VimOps.getCharClass(line.charAt(this.caretx));
			if (this.caretx + 1 < line.length() &&
				clss != 2 &&
				clss != VimOps.getCharClass(line.charAt(this.caretx + 1)))
			{
				// see tests.. case when caret is on last letter of current word
				pt = VimOps.forwardsEx(this.lines, this.caretx, this.carety);
				if (pt.x != this.caretx) {
					pt.x--;
				}
			} else {
				pt = VimOps.forwards(this.lines, this.caretx, this.carety);
			}
			if (pt.x == this.caretx && pt.y == this.carety) {
				break;
			}
			line = this.lines.get(pt.y);
			if (pt.y != this.carety) {
				from = 0;
			} else {
				from = this.caretx;
			}
			dst = new char[pt.x + 1 - from];
			line.getChars(from, pt.x + 1, dst, 0);
			line.delete(from, pt.x + 1);
			this.j.pastebuffer = new String(dst);
			this.writingUndo = new UndoStuff(this.caretx, this.carety);
			this.writingUndo.fromx = from;
			this.writingUndo.fromy = this.carety;
			this.writingUndo.tox = from;
			this.writingUndo.toy = this.carety;
			this.writingUndo.replacement.append(dst);
			if (next_mode == NORMAL_MODE) {
				this.addCurrentWritingUndo();
			}
			this.caretx = from;
			if (next_mode != INSERT_MODE && this.caretx == line.length()) {
				this.caretx--;
			}
			this.carety = pt.y;
			this.mode = next_mode;
			e.needCheckLineLength = true;
			e.needEnsureViewSize = true;
			e.needRepaint = true;
			return;
		case 'i':
			this.mode = next_in_mode;
			return;
		}
		this.mode = NORMAL_MODE;
		e.error = true;
	}

	private void handleInputChangeDeleteIn(KeyInput e, int next_mode)
	{
		if (e.c == 'w') {
			StringBuilder line = this.lines.get(this.carety);
			if (this.caretx >= line.length()) {
				this.mode = next_mode;
				return;
			}
			char[] chars = Util.getValue(line);
			char clazz = VimOps.getCharClass(chars[this.caretx]);
			int from = this.caretx;
			int to = this.caretx + 1;
			while (from > 0 && clazz == VimOps.getCharClass(chars[from - 1])) {
				from--;
			}
			while (to < line.length() && clazz == VimOps.getCharClass(chars[to])) {
				to++;
			}
			char[] dst = new char[to - from];
			line.getChars(from, to, dst, 0);
			line.delete(from, to);
			this.j.pastebuffer = new String(dst);
			this.writingUndo = new UndoStuff(this.caretx, this.carety);
			this.writingUndo.fromx = from;
			this.writingUndo.fromy = this.carety;
			this.writingUndo.tox = from;
			this.writingUndo.toy = this.carety;
			this.writingUndo.replacement.append(dst);
			if (next_mode == NORMAL_MODE) {
				this.addCurrentWritingUndo();
			}
			this.caretx = from;
			if (next_mode != INSERT_MODE && this.caretx == line.length()) {
				this.caretx--;
			}
			this.mode = next_mode;
			e.needCheckLineLength = true;
			e.needEnsureViewSize = true;
			e.needRepaint = true;
			return;
		}
		// TODO handle strings?
		this.mode = NORMAL_MODE;
		e.error = true;
	}

	private void handleInputG(KeyInput e)
	{
		if (e.c == 'g') {
			int visual = this.curVisualX();
			this.carety = 0;
			this.putCaretX(visual);
			e.needRepaintCaret = true;
			this.mode = NORMAL_MODE;
			return;
		}
		this.mode = NORMAL_MODE;
		e.error = true;
	}

	private void handleInputD(KeyInput e)
	{
		String line;
		if (e.c == 'd') {
			line = this.lines.get(this.carety).toString();
			this.lines.remove(this.carety);
			this.writingUndo = new UndoStuff(this.caretx, this.carety);
		} else if (e.c == 'j') {
			if (this.carety == this.lines.size() - 1) {
				e.error = true;
				return;
			}
			line = this.lines.get(this.carety).toString();
			this.lines.remove(this.carety);
			line += '\n' + this.lines.get(this.carety).toString();
			this.lines.remove(this.carety);
			this.writingUndo = new UndoStuff(this.caretx, this.carety);
		} else if (e.c == 'k') {
			if (this.carety == 0) {
				e.error = true;
				return;
			}
			this.writingUndo = new UndoStuff(this.caretx, this.carety);
			this.carety--;
			line = this.lines.get(this.carety).toString();
			this.lines.remove(this.carety);
			line += '\n' + this.lines.get(this.carety).toString();
			this.lines.remove(this.carety);
			this.writingUndo.fromy = this.writingUndo.toy = this.carety;
		} else {
			this.handleInputChangeDelete(e, NORMAL_MODE, DELETE_IN_MODE);
			return;
		}
		this.writingUndo.fromx = 0;
		this.writingUndo.tox = 0;
		if (this.lines.isEmpty()) {
			this.lines.add(new StringBuilder());
			this.writingUndo.replacement.append(line);
		} else if (this.carety >= this.lines.size()) {
			this.carety--;
			int len = this.lines.get(this.carety).length();
			this.writingUndo.fromy = this.writingUndo.toy = this.carety;
			this.writingUndo.fromx = this.writingUndo.tox = len;
			this.writingUndo.replacement.append('\n' + line);
		} else {
			this.writingUndo.replacement.append(line + '\n');
		}
		this.addCurrentWritingUndo();
		this.caretx = 0;
		this.mode = NORMAL_MODE;
		this.j.pastebuffer = line + '\n';
		e.needRepaintCaret = true;
		e.needCheckLineLength = true;
		e.needEnsureViewSize = true;
	}

	private void handleInputSelectLine(KeyInput e)
	{
		switch (e.c) {
		case 'h':
		case 'l':
			this.handleInputNormal(e);
			break;
		case 'j':
		case 'k':
			this.handleInputNormal(e);
			if (!e.error) {
				if (this.carety <= this.lineselectinitial) {
					this.lineselectfrom = this.carety;
				}
				if (this.carety >= this.lineselectinitial) {
					this.lineselectto = this.carety + 1;
				}
				e.needRepaint = true;
			}
			break;
		case 'o':
			if (this.carety == this.lineselectfrom) {
				this.carety = this.lineselectto - 1;
			} else {
				this.carety = this.lineselectfrom;
			}
			this.caretx = this.virtualCaretx = 0;
			e.needRepaintCaret = true;
			break;
		case 'd':
			this.writingUndo = new UndoStuff(0, this.lineselectinitial);
			int from = this.lineselectfrom;
			for (int i = this.lineselectfrom; i < this.lineselectto; i++) {
				StringBuilder line = this.lines.remove(from);
				this.writingUndo.replacement.append(line).append('\n');
			}
			this.j.pastebuffer = this.writingUndo.replacement.toString();
			if (this.lines.isEmpty()) {
				this.lines.add(new StringBuilder());
			}
			this.caretx = 0;
			if (from >= this.lines.size()) {
				StringBuilder replacement = this.writingUndo.replacement;
				replacement.setLength(replacement.length() - 1);
				replacement.insert(0, '\n');
				this.carety = from - 1;
				this.writingUndo.fromx = this.lines.get(this.carety).length();
				this.writingUndo.tox = this.writingUndo.fromx;
			} else {
				this.carety = from;
			}
			this.writingUndo.fromy = this.writingUndo.toy = this.carety;
			this.addCurrentWritingUndo();
			this.mode = NORMAL_MODE;
			e.needRepaint = true;
			e.needRepaintCaret = true;
			e.needCheckLineLength = true;
			e.needEnsureViewSize = true;
			break;
		case ESC:
			this.mode = NORMAL_MODE;
			e.needRepaint = true;
			break;
		default:
			e.error = true;
		}
	}

	/**
	 * Handles an input event, without storing the input in the command buffer.
	 */
	private void handleInput(KeyInput e)
	{
		switch (mode) {
		case NORMAL_MODE:
			this.handleInputNormal(e);
			break;
		case INSERT_MODE:
			this.handleInputInsert(e);
			break;
		case CHANGE_MODE:
			this.handleInputChangeDelete(e, INSERT_MODE, CHANGE_IN_MODE);
			break;
		case CHANGE_IN_MODE:
			this.handleInputChangeDeleteIn(e, INSERT_MODE);
			break;
		case DELETE_MODE:
			this.handleInputD(e);
			break;
		case DELETE_IN_MODE:
			this.handleInputChangeDeleteIn(e, NORMAL_MODE);
			break;
		case G_MODE:
			this.handleInputG(e);
			break;
		case SELECT_LINE_MODE:
			this.handleInputSelectLine(e);
			break;
		}
	}

	private void replayKeys(char keys[], int length, KeyInput e)
	{
		KeyInput virtualEvent = new KeyInput();
		for (int i = 0; i < length; i++) {
			virtualEvent.c = keys[i];
			this.handleInput(virtualEvent);
			if (virtualEvent.error) {
				e.error = true;
				break;
			}
		}
		e.needCheckLineLength = true;
		e.needEnsureViewSize = true;
		e.needRepaint = true;
		e.needRepaintCaret = true;
		// Since replaying can start recording again, suppress it by forcing false.
		this.creatingCommand = false;
	}

	public void handlePhysicalInput(KeyInput e)
	{
		this.handleInput(e);
		if (!e.error && this.creatingCommand) {
			if (this.creatingCmdBuf.length <= this.creatingCmdLength) {
				int len = this.creatingCmdLength * 2;
				this.creatingCmdBuf = Arrays.copyOf(this.creatingCmdBuf, len);
			}
			this.creatingCmdBuf[this.creatingCmdLength++] = e.c;
			if (this.mode == NORMAL_MODE) {
				this.creatingCommand = false;
				if (this.storedCmdBuf.length != this.creatingCmdBuf.length) {
					this.storedCmdBuf = new char[this.creatingCmdBuf.length];
				}
				int len = this.creatingCmdLength;
				System.arraycopy(this.creatingCmdBuf, 0, this.storedCmdBuf, 0, len);
				this.storedCommandLength = this.creatingCmdLength;
			}
		}
	}
}
