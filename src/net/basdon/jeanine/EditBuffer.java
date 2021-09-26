package net.basdon.jeanine;

import java.awt.Point;
import java.util.Arrays;

import static java.lang.System.arraycopy;

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
	private final CodeGroup group;

	public final BufferLines lines;

	private boolean creatingCommand;
	private char creatingCmdBuf[];
	private int creatingCmdLength;

	public boolean readonly;
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
	public int virtualCaretx;
	public int mode;
	/**
	 * The undo stuff that is currently being written (ie because we're in insert mode).
	 */
	private Undo writingUndo;

	/**
	 * Interact with {@link #lines} to change the contents.
	 * Always ensure there's at least one line.
	 */
	public EditBuffer(Jeanine j, CodeGroup group)
	{
		this.j = j;
		this.group = group;
		this.creatingCmdBuf = new char[100];
		this.lines = new BufferLines(group);
		this.lines.add(new SB());
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
		SB line = this.lines.get(this.carety);
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
		Util.setArrayListSize(this.j.undolist, this.j.undolistptr);
		this.j.undolist.add(this.writingUndo);
		this.j.undolistptr++;
		this.writingUndo = null;
	}

	private void handleInputNormal(KeyInput e)
	{
		SB line;
		int len, visual;
		Point pt;
		int prevCaretx = this.caretx;
		int prevCarety = this.carety;
		switch (e.c) { // break when the key starts a new command (for .), return otherwise
		case 'c':
			if (this.readonly) { e.error = true; return; }
			if (this.caretx >= this.lines.get(this.carety).length()) {
				e.error = true;
				return;
			}
			this.mode = CHANGE_MODE;
			break;
		case 'd':
			if (this.readonly) { e.error = true; return; }
			if (this.caretx > this.lines.get(this.carety).length) {
				e.error = true;
				return;
			}
			this.mode = DELETE_MODE;
			break;
		case 'u':
			if (this.readonly) { e.error = true; return; }
			if (this.j.undolistptr == 0) {
				e.error = true;
			} else {
				Undo u = this.j.undolist.get(this.j.undolistptr - 1);
				if (u.buffer != this) {
					this.group.doUndo(u);
					return;
				}
				this.j.undolistptr--;
				line = this.lines.get(u.fromy);
				char[] toappend;
				if (u.toy > u.fromy) {
					line.setLength(u.fromx);
					SB lastline = this.lines.get(u.toy);
					int pos = u.tox;
					for (int y = u.toy; y > u.fromy; y--) {
						this.lines.remove(y);
					}
					toappend = new char[lastline.length - pos];
					len = lastline.length - pos;
					arraycopy(lastline.value, pos, toappend, 0, len);
				} else {
					if (line.length == 0) {
						toappend = new char[0];
					} else {
						toappend = new char[line.length - u.tox];
						len = line.length - u.tox;
						arraycopy(line.value, u.tox, toappend, 0, len);
					}
					line.setLength(u.fromx);
				}
				// put in replacement
				int idx = u.replacement.indexOf('\n', 0);
				int from = 0, nextfrom = 0;
				int linenr = u.fromy + 1;
				while (idx != -1) {
					from = idx + 1;
					line.append(u.replacement.value, nextfrom, idx);
					nextfrom = idx + 1;
					idx = u.replacement.indexOf('\n', idx + 1);
					line = new SB();
					this.lines.add(linenr, line);
					linenr++;
				}
				line.append(u.replacement.value, from, u.replacement.length);
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
			if (this.readonly) { e.error = true; return; }
			this.writingUndo = this.newUndo(this.caretx, this.carety);
			this.writingUndo.fromx = this.lines.get(this.carety).length();
			this.writingUndo.fromy = this.carety;
			this.writingUndo.tox = 0;
			this.writingUndo.toy = this.carety + 1;
			this.carety++;
			this.lines.add(this.carety, new SB());
			this.caretx = 0;
			this.mode = INSERT_MODE;
			e.needRepaint = true;
			e.needEnsureViewSize = true;
			break;
		case 'O':
			if (this.readonly) { e.error = true; return; }
			this.writingUndo = this.newUndo(this.caretx, this.carety);
			this.writingUndo.fromx = 0;
			this.writingUndo.fromy = this.carety;
			this.writingUndo.tox = 0;
			this.writingUndo.toy = this.carety + 1;
			this.lines.add(this.carety, new SB());
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
			if (this.readonly) { e.error = true; return; }
			int pos = 0;
			for (char chr : this.lines.get(this.carety).toString().toCharArray()) {
				if (chr != ' ' && chr != '\t') {
					break;
				}
				pos++;
			}
			this.writingUndo = this.newUndo(this.caretx, this.carety);
			this.writingUndo.fromx = this.writingUndo.tox = pos;
			this.caretx = pos;
			this.mode = INSERT_MODE;
			e.needRepaintCaret = true;
			break;
		case 'i':
			if (this.readonly) { e.error = true; return; }
			this.mode = INSERT_MODE;
			e.needRepaintCaret = true;
			this.writingUndo = this.newUndo(this.caretx, this.carety);
			break;
		case 'A':
			if (this.readonly) { e.error = true; return; }
			this.caretx = this.lines.get(this.carety).length();
			this.mode = INSERT_MODE;
		case 'a':
			if (this.readonly) { e.error = true; return; }
			this.writingUndo = this.newUndo(prevCaretx, prevCarety);
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
			if (this.readonly) { e.error = true; return; }
			line = this.lines.get(this.carety);
			len = line.length();
			if (len == 0) {
				e.error = true;
				return;
			}
			char c = line.value[this.caretx];
			line.delete(this.caretx, this.caretx + 1);
			this.j.pastebuffer = String.valueOf(c);
			this.writingUndo = this.newUndo(this.caretx, this.carety);
			this.writingUndo.replacement = new SB().append(c);
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
			if (this.readonly) { e.error = true; return; }
			this.writingUndo = this.newUndo(this.caretx, this.carety);
			if (this.j.pastebuffer.endsWith("\n")) {
				this.writingUndo.fromx = this.lines.get(this.carety).length();
				String lines[] = this.j.pastebuffer.split("\n");
				for (int i = lines.length - 1; i >= 0; i--) {
					this.lines.add(this.carety + 1, new SB(lines[i]));
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
			if (this.readonly) { e.error = true; return; }
			this.writingUndo = this.newUndo(this.caretx, this.carety);
			if (this.j.pastebuffer.endsWith("\n")) {
				String lines[] = this.j.pastebuffer.split("\n");
				for (int i = lines.length - 1; i >= 0; i--) {
					this.lines.add(this.carety, new SB(lines[i]));
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
			pt = VimOps.forwards(this.lines.lines, this.caretx, this.carety);
			if (pt.x == this.caretx && pt.y == this.carety) {
				e.error = true;
			} else {
				this.caretx = pt.x;
				this.carety = pt.y;
				e.needRepaintCaret = true;
			}
			return;
		case 'w':
			pt = VimOps.forwardsEx(this.lines.lines, this.caretx, this.carety);
			if (pt.x == this.caretx && pt.y == this.carety) {
				e.error = true;
			} else {
				this.caretx = pt.x;
				this.carety = pt.y;
				e.needRepaintCaret = true;
			}
			return;
		case 'b':
			pt = VimOps.backwards(this.lines.lines, this.caretx, this.carety);
			if (pt.x == this.caretx && pt.y == this.carety) {
				e.error = true;
			} else {
				this.caretx = pt.x;
				this.carety = pt.y;
				e.needRepaintCaret = true;
			}
			return;
		case '.':
			if (this.readonly) { e.error = true; return; }
			if (this.j.commandLength == 0) {
				e.error = true;
			} else {
				this.replayKeys(this.j.commandBuf, this.j.commandLength, e);
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
		SB line;
		switch (e.c) {
		case BS:
			e.needCheckLineLength = true;
			e.needEnsureViewSize = true;
			if (this.caretx > 0) {
				this.caretx--;
				line = this.lines.get(this.carety);
				if (this.writingUndo.fromy == this.writingUndo.toy &&
					this.writingUndo.fromx == this.writingUndo.tox)
				{
					char c = line.charAt(this.caretx);
					this.writingUndo.replacement.insert(0, c);
					if (this.carety == this.writingUndo.fromy) {
						this.writingUndo.fromx--;
					}
					if (this.carety == this.writingUndo.toy) {
						this.writingUndo.tox--;
					}
				} else {
					this.writingUndo.tox--;
				}
				line.delete(this.caretx, this.caretx + 1);
			} else if (this.carety > 0) {
				this.writingUndo.replacement.insert(0, '\n');
				SB linecontent = this.lines.get(this.carety);
				this.lines.remove(this.carety);
				SB prev = this.lines.get(this.carety - 1);
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
				this.writingUndo.replacement.append('\n');
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
			arraycopy(line.value, this.caretx, dst, 0, line.length - this.caretx);
			line.delete(this.caretx, line.length());
			this.lines.add(++this.carety, new SB(dst));
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
		SB line;
		char[] dst;
		Point pt;
		int from, to;
		switch (e.c) {
		case ESC:
			this.mode = NORMAL_MODE;
			return;
		case 'b':
			pt = VimOps.backwards(this.lines.lines, this.caretx, this.carety);
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
			arraycopy(line.value, pt.x, dst, 0, to - pt.x);
			line.delete(pt.x, to);
			this.j.pastebuffer = new String(dst);
			this.writingUndo = this.newUndo(this.caretx, this.carety);
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
				pt = VimOps.forwardsEx(this.lines.lines, this.caretx, this.carety);
				if (pt.x != this.caretx) {
					pt.x--;
				}
			} else {
				pt = VimOps.forwards(this.lines.lines, this.caretx, this.carety);
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
			arraycopy(line.value, from, dst, 0, pt.x + 1 - from);
			line.delete(from, pt.x + 1);
			this.j.pastebuffer = new String(dst);
			this.writingUndo = this.newUndo(this.caretx, this.carety);
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
			SB line = this.lines.get(this.carety);
			if (this.caretx >= line.length()) {
				this.mode = next_mode;
				return;
			}
			char[] chars = line.value;
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
			arraycopy(line.value, from, dst, 0, to - from);
			line.delete(from, to);
			this.j.pastebuffer = new String(dst);
			this.writingUndo = this.newUndo(this.caretx, this.carety);
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
		SB line;
		if (e.c == 'd') {
			line = new SB(this.lines.get(this.carety));
			this.lines.remove(this.carety);
			this.writingUndo = this.newUndo(this.caretx, this.carety);
		} else if (e.c == 'j') {
			if (this.carety == this.lines.size() - 1) {
				e.error = true;
				return;
			}
			line = new SB(this.lines.get(this.carety));
			this.lines.remove(this.carety);
			line.append('\n').append(this.lines.get(this.carety));
			this.lines.remove(this.carety);
			this.writingUndo = this.newUndo(this.caretx, this.carety);
		} else if (e.c == 'k') {
			if (this.carety == 0) {
				e.error = true;
				return;
			}
			this.writingUndo = this.newUndo(this.caretx, this.carety);
			this.carety--;
			line = new SB(this.lines.get(this.carety));
			this.lines.remove(this.carety);
			line.append('\n').append(this.lines.get(this.carety));
			this.lines.remove(this.carety);
			this.writingUndo.fromy = this.writingUndo.toy = this.carety;
		} else {
			this.handleInputChangeDelete(e, NORMAL_MODE, DELETE_IN_MODE);
			return;
		}
		this.writingUndo.fromx = 0;
		this.writingUndo.tox = 0;
		if (this.lines.isEmpty()) {
			this.lines.add(new SB());
			this.writingUndo.replacement.append(line);
		} else if (this.carety >= this.lines.size()) {
			this.carety--;
			int len = this.lines.get(this.carety).length();
			this.writingUndo.fromy = this.writingUndo.toy = this.carety;
			this.writingUndo.fromx = this.writingUndo.tox = len;
			this.writingUndo.replacement.append('\n').append(line);
		} else {
			this.writingUndo.replacement.append(line).append('\n');
		}
		this.addCurrentWritingUndo();
		this.caretx = 0;
		this.mode = NORMAL_MODE;
		this.j.pastebuffer = line.toString() + '\n';
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
			this.writingUndo = this.newUndo(0, this.lineselectinitial);
			int from = this.lineselectfrom;
			for (int i = this.lineselectfrom; i < this.lineselectto; i++) {
				SB line = this.lines.remove(from);
				this.writingUndo.replacement.append(line).append('\n');
			}
			this.j.pastebuffer = this.writingUndo.replacement.toString();
			if (this.lines.isEmpty()) {
				this.lines.add(new SB());
			}
			this.caretx = 0;
			if (from >= this.lines.size()) {
				SB replacement = this.writingUndo.replacement;
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
			e.needGlobalRepaint = true;
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
				this.j.storeCommand(this.creatingCmdBuf, this.creatingCmdLength);
			}
		}
	}

	private Undo newUndo(int x, int y)
	{
		return new Undo(this, x, y);
	}

	public Point find(char[] text, int fromy, int fromx)
	{
		if (text.length == 0) {
			return null;
		}
		int minx = fromx;
		for (int j = fromy; j < this.lines.lines.size(); j++) {
			SB line = this.lines.lines.get(j);
			int idx = line.indexOf(text, minx);
			if (idx != -1) {
				return new Point(idx, j);
			}
			minx = 0;
		}
		return null;
	}

	public Point findBackwards(char[] text, int fromy, int fromx)
	{
		if (text.length == 0) {
			return null;
		}
		int maxx = fromx;
		if (fromy >= this.lines.size()) {
			fromy = this.lines.size() - 1;
		}
		for (int j = fromy; j >= 0; j--) {
			SB line = this.lines.lines.get(j);
			int idx = line.lastIndexOf(text, maxx);
			if (idx != -1) {
				return new Point(idx, j);
			}
			maxx = Integer.MAX_VALUE;
		}
		return null;
	}
}
