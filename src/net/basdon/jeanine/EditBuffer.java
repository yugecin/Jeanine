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
		G_MODE = 6;

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

	public EditBuffer(Jeanine j, String text)
	{
		this.j = j;
		this.storedCmdBuf = new char[100];
		this.creatingCmdBuf = new char[100];
		this.lines = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		this.lines.add(sb);
		for (char c : text.toCharArray()) {
			if (c == '\n') {
				sb = new StringBuilder();
				this.lines.add(sb);
			} else {
				sb.append(c);
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

	private void handleInputNormal(KeyInput e)
	{
		StringBuilder line;
		int len, visual;
		Point pt;
		e.consumed = true;
		switch (e.c) { // break when the key starts a new command (for .), return otherwise
		case 'c':
			if (this.caretx >= this.lines.get(this.carety).length()) {
				e.error = true;
				return;
			}
			this.mode = CHANGE_MODE;
			break;
		case 'd':
			if (this.caretx >= this.lines.get(this.carety).length()) {
				e.error = true;
				return;
			}
			this.mode = DELETE_MODE;
			break;
		case ':':
			// Colon should open the command bar,
			// don't consume the event so the caller can deal with the command bar.
			e.consumed = false;
			return;
		case 'o':
			this.carety++;
		case 'O':
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
			this.caretx = pos;
		case 'i':
			this.mode = INSERT_MODE;
			e.needRepaintCaret = true;
			break;
		case 'A':
			this.caretx = this.lines.get(this.carety).length();
			this.mode = INSERT_MODE;
		case 'a':
			if (this.caretx < this.lines.get(this.carety).length()) {
				this.caretx++;
			}
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
			if (this.caretx >= len - 1 && this.caretx > 0) {
				this.caretx--;
				this.virtualCaretx = Line.logicalToVisualPos(line, this.caretx);
			}
			e.needCheckLineLength = true;
			e.needEnsureViewSize = true;
			e.needRepaint = true;
			break;
		case 'p':
			if (this.j.pastebuffer.endsWith("\n")) {
				// TODO: paste with newlines
			} else {
				line = this.lines.get(this.carety);
				len = line.length();
				if (len == 0) {
					line.insert(0, this.j.pastebuffer);
				} else {
					line.insert(this.caretx + 1, this.j.pastebuffer);
				}
				this.caretx += this.j.pastebuffer.length();
				this.virtualCaretx = Line.logicalToVisualPos(line, this.caretx);
			}
			e.needCheckLineLength = true;
			e.needEnsureViewSize = true;
			e.needRepaint = true;
			break;
		case 'P':
			if (this.j.pastebuffer.endsWith("\n")) {
				// TODO: paste with newlines
			} else {
				line = this.lines.get(this.carety);
				line.insert(this.caretx, this.j.pastebuffer);
				this.caretx += this.j.pastebuffer.length() - 1;
				this.virtualCaretx = Line.logicalToVisualPos(line, this.caretx);
			}
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
		default:
			e.error = true;
			return;
		}
		this.creatingCommand = true;
		this.creatingCmdLength = 0;
	}

	private void handleInputInsert(KeyInput e)
	{
		e.consumed = true;
		StringBuilder line;
		switch (e.c) {
		case BS:
			e.needCheckLineLength = true;
			e.needEnsureViewSize = true;
			if (this.caretx > 0) {
				this.lines.get(this.carety).delete(this.caretx - 1, this.caretx);
				this.caretx--;
			} else if (this.carety > 0) {
				String linecontent = this.lines.get(this.carety).toString();
				this.lines.remove(this.carety);
				this.carety--;
				StringBuilder prev = this.lines.get(this.carety);
				this.caretx = prev.length();
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
			line = this.lines.get(this.carety);
			this.virtualCaretx = Line.logicalToVisualPos(line, this.caretx);
			e.needRepaintCaret = true;
			return;
		case DEL:
			line = this.lines.get(this.carety);
			e.needCheckLineLength = true;
			e.needEnsureViewSize = true;
			if (this.caretx < line.length()) {
				line.delete(this.caretx, this.caretx + 1);
			} else if (this.carety + 1 < this.lines.size()) {
				StringBuilder next = this.lines.remove(this.carety + 1);
				line.insert(this.caretx, next.toString());
			} else {
				e.error = true;
				return;
			}
			break;
		case '\r':
		case '\n':
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
			line = this.lines.get(this.carety);
			line.insert(this.caretx, e.c);
			this.virtualCaretx = Line.logicalToVisualPos(line, ++this.caretx);
			e.needCheckLineLength = true;
			e.needEnsureViewSize = true;
		}
		e.needRepaint = true;
	}

	private void handleInputChangeDelete(KeyInput e, int next_mode, int in_mode)
	{
		StringBuilder line;
		char[] dst;
		Point pt;
		int from, to;
		e.consumed = true;
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
			this.caretx = pt.x;
			this.carety = pt.y;
			this.mode = next_mode;
			e.needCheckLineLength = true;
			e.needEnsureViewSize = true;
			e.needRepaint = true;
			return;
		case 'w':
			pt = VimOps.forwards(this.lines, this.caretx, this.carety);
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
			this.caretx = from;
			this.carety = pt.y;
			this.mode = next_mode;
			e.needCheckLineLength = true;
			e.needEnsureViewSize = true;
			e.needRepaint = true;
			return;
		case 'i':
			this.mode = in_mode;
			return;
		}
		this.mode = NORMAL_MODE;
		e.error = true;
	}

	private void handleInputChangeDeleteIn(KeyInput e, int next_mode)
	{
		e.consumed = true;
		if (e.c == 'w') {
			StringBuilder line = this.lines.get(this.carety);
			if (this.caretx >= line.length()) {
				this.mode = next_mode;
				return;
			}
			char[] chars = Line.getValue(line);
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
			this.caretx = from;
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
		e.consumed = true;
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
			this.handleInputChangeDelete(e, NORMAL_MODE, DELETE_IN_MODE);
			break;
		case DELETE_IN_MODE:
			this.handleInputChangeDeleteIn(e, NORMAL_MODE);
			break;
		case G_MODE:
			this.handleInputG(e);
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
