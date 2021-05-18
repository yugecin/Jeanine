package net.basdon.jeanine;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JPanel;
import javax.swing.LookAndFeel;

public class CodePanel
	extends JPanel
	implements MouseListener, FocusListener, KeyListener, CommandBar.Listener
{
	private static final int
		NORMAL_MODE = 0,
		INSERT_MODE = 1,
		CHANGE_MODE = 2,
		CHANGE_IN_MODE = 3;

	private final CodeFrame frame;
	private final JeanineFrame jf;
	private final Jeanine j;

	private ArrayList<StringBuilder> lines;
	private int maxLineLength;
	/**
	 * This is always logical pos (disregarding char/tab width).
	 */
	private int caretx;
	private int carety;
	/**
	 * Stores the last visual caretx to use when we were on a line that has less chars than the previous one.
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
	private int mode;

	public CodePanel(CodeFrame frame, JeanineFrame jf, String code)
	{
		this.jf = jf;
		this.j = jf.j;
		this.lines = new ArrayList<>();
		this.frame = frame;
		this.setFocusable(true);
		this.addMouseListener(this);
		this.addKeyListener(this);
		for (String line : code.split("\n")) {
			this.lines.add(new StringBuilder(line));
		}
		this.recheckMaxLineLength();
		this.ensureCodeViewSize();
		// make that we get the TAB key events
		LookAndFeel.installProperty(this, "focusTraversalKeysForward", Collections.EMPTY_SET);
		LookAndFeel.installProperty(this, "focusTraversalKeysBackward", Collections.EMPTY_SET);
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.setColor(Color.white);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		g.translate(1, 1);
		if (this.mode == INSERT_MODE) {
			g.setColor(Color.green);
		} else {
			g.setColor(Color.red);
		}
		int caretx = Line.logicalToVisualPos(this.lines.get(this.carety), this.caretx);
		if (this.hasFocus()) {
			g.fillRect(caretx * this.j.fx, this.carety * this.j.fy, this.j.fx, this.j.fy);
		} else {
			g.drawRect(caretx * this.j.fx, this.carety * this.j.fy, this.j.fx, this.j.fy);
		}
		g.setFont(this.j.font);
		g.setColor(Color.black);
		for (int i = 0; i < this.lines.size(); i++) {
			g.drawString(Line.tabs2spaces(this.lines.get(i)), 0, this.j.fmaxascend);
			g.translate(0, this.j.fy);
		}
	}

	private void handleInputNormal(KeyInput e)
	{
		StringBuilder line;
		int len;
		Point pt;
		switch (e.c) {
		case 'c':
			if (this.caretx >= this.lines.get(this.carety).length()) {
				e.error = true;
				return;
			}
			this.mode = CHANGE_MODE;
			break;
		case ':':
			this.jf.commandbar.show("", this);
			break;
		case 'o':
			this.carety++;
		case 'O':
			this.lines.add(this.carety, new StringBuilder());
			this.caretx = 0;
			this.mode = INSERT_MODE;
			this.ensureCodeViewSize();
			this.repaint();
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
			this.repaint(); // TODO: only should repaint the caret
			break;
		case 'A':
			this.caretx = this.lines.get(this.carety).length();
			this.mode = INSERT_MODE;
		case 'a':
			if (this.caretx < this.lines.get(this.carety).length()) {
				this.caretx++;
			}
			this.mode = INSERT_MODE;
			this.repaint(); // TODO: only should repaint the caret
			break;
		case '^':
			this.caretx = this.virtualCaretx = 0;
			this.repaint(); // TODO: only should repaint the caret
			break;
		case '$':
			this.caretx = this.lines.get(this.carety).length() - 1;
			if (this.caretx < 0) {
				this.caretx = 0;
			}
			this.virtualCaretx = 999999;
			this.repaint(); // TODO: only should repaint the caret
			break;
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
			this.recheckMaxLineLength();
			this.ensureCodeViewSize();
			this.repaint();
			break;
		case 'p':
			if (this.j.pastebuffer.endsWith("\n")) {

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
			this.recheckMaxLineLength();
			this.ensureCodeViewSize();
			this.repaint();
			break;
		case 'P':
			if (this.j.pastebuffer.endsWith("\n")) {

			} else {
				line = this.lines.get(this.carety);
				line.insert(this.caretx, this.j.pastebuffer);
				this.caretx += this.j.pastebuffer.length() - 1;
				this.virtualCaretx = Line.logicalToVisualPos(line, this.caretx);
			}
			this.recheckMaxLineLength();
			this.ensureCodeViewSize();
			this.repaint();
			break;
		case 'h':
			if (this.caretx > 0) {
				this.caretx--;
				this.virtualCaretx = Line.logicalToVisualPos(this.lines.get(this.carety), this.caretx);
				this.repaint(); // TODO: only should repaint the caret
			} else {
				e.error = true;
			}
			break;
		case 'j':
			if (this.carety < this.lines.size() - 1) {
				int visual = Line.logicalToVisualPos(this.lines.get(this.carety), this.caretx);
				if (this.virtualCaretx > visual) {
					visual = this.virtualCaretx;
				}
				this.carety++;
				line = this.lines.get(this.carety);
				if (visual > line.length()) {
					if (visual > this.virtualCaretx) {
						this.virtualCaretx = visual;
					}
					this.caretx = line.length() - 1;
				} else {
					this.caretx = Line.visualToLogicalPos(line, visual);
				}
				this.repaint(); // TODO: only should repaint the caret
			} else {
				e.error = true;
			}
			break;
		case 'k':
			if (this.carety > 0) {
				line = this.lines.get(this.carety);
				int visual = Line.logicalToVisualPos(line, this.caretx);
				if (this.virtualCaretx > visual) {
					visual = this.virtualCaretx;
				}
				this.carety--;
				line = this.lines.get(this.carety);
				if (visual > line.length()) {
					if (visual > this.virtualCaretx) {
						this.virtualCaretx = visual;
					}
					this.caretx = line.length() - 1;
				} else {
					this.caretx = Line.visualToLogicalPos(line, visual);
				}
				this.repaint(); // TODO: only should repaint the caret
			} else {
				e.error = true;
			}
			break;
		case 'l':
			line = this.lines.get(this.carety);
			if (this.caretx < line.length() - 1) {
				this.caretx++;
				this.virtualCaretx = Line.logicalToVisualPos(line, this.caretx);
				this.repaint(); // TODO: only should repaint the caret
			} else {
				e.error = true;
			}
			break;
		case 'e':
			pt = VimOps.forwards(this.lines, this.caretx, this.carety);
			if (pt.x == this.caretx && pt.y == this.carety) {
				e.error = true;
			} else {
				this.caretx = pt.x;
				this.carety = pt.y;
				this.repaint(); // TODO: only should repaint the caret
			}
			break;
		case 'w':
			pt = VimOps.forwardsEx(this.lines, this.caretx, this.carety);
			if (pt.x == this.caretx && pt.y == this.carety) {
				e.error = true;
			} else {
				this.caretx = pt.x;
				this.carety = pt.y;
				this.repaint(); // TODO: only should repaint the caret
			}
			break;
		case 'b':
			pt = VimOps.backwards(this.lines, this.caretx, this.carety);
			if (pt.x == this.caretx && pt.y == this.carety) {
				e.error = true;
			} else {
				this.caretx = pt.x;
				this.carety = pt.y;
				this.repaint(); // TODO: only should repaint the caret
			}
			break;
		}
		e.consumed = true;
	}

	private void handleInputInsert(KeyInput e)
	{
		StringBuilder line;
		switch (e.c) {
		case /*bs*/8:
			if (this.caretx > 0) {
				this.lines.get(this.carety).delete(this.caretx - 1, this.caretx);
				this.caretx--;
			} else if (this.carety > 0) {
				String linecontent = this.lines.get(this.carety).toString();
				this.lines.remove(this.carety);
				this.carety--;
				StringBuilder prev = this.lines.get(this.carety);
				this.caretx = prev.length();
				prev.insert(this.caretx, linecontent);
			} else {
				e.error = true;
				return;
			}
			break;
		case /*esc*/27:
			this.mode = NORMAL_MODE;
			if (this.caretx > 0) {
				this.caretx--;
			}
			line = this.lines.get(this.carety);
			this.virtualCaretx = Line.logicalToVisualPos(line, this.caretx);
			this.repaint(); // TODO: only should repaint the caret
			break;
		case /*del*/127:
			line = this.lines.get(this.carety);
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
			this.recheckMaxLineLength();
			this.ensureCodeViewSize();
			break;
		default:
			line = this.lines.get(this.carety);
			line.insert(this.caretx, e.c);
			this.virtualCaretx = Line.logicalToVisualPos(line, ++this.caretx);
			this.recheckMaxLineLength();
			this.ensureCodeViewSize();
		}
		e.consumed = true;
		this.repaint();
	}

	private void handleInputChange(KeyInput e)
	{
		StringBuilder line;
		char[] dst;
		Point pt;
		int from, to;
		e.consumed = true;
		switch (e.c) {
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
			this.mode = INSERT_MODE;
			this.recheckMaxLineLength();
			this.ensureCodeViewSize();
			this.repaint();
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
			this.mode = INSERT_MODE;
			this.recheckMaxLineLength();
			this.ensureCodeViewSize();
			this.repaint();
			return;
		case 'i':
			this.mode = CHANGE_IN_MODE;
			return;
		}
		this.mode = NORMAL_MODE;
		e.error = true;
	}

	private void handleInputChangeIn(KeyInput e)
	{
		e.consumed = true;
		if (e.c == 'w') {
			StringBuilder line = this.lines.get(this.carety);
			if (this.caretx >= line.length()) {
				this.mode = INSERT_MODE;
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
			this.mode = INSERT_MODE;
			this.recheckMaxLineLength();
			this.ensureCodeViewSize();
			this.repaint();
			return;
		}
		// TODO handle strings?
		this.mode = NORMAL_MODE;
		e.error = true;
	}

	/*KeyListener*/
	@Override
	public void keyTyped(KeyEvent e)
	{
		KeyInput event = new KeyInput();
		event.c = e.getKeyChar();
		if (event.c == KeyEvent.CHAR_UNDEFINED) {
			e.consume();
			return;
		}
		switch (mode) {
		case NORMAL_MODE:
			this.handleInputNormal(event);
			break;
		case INSERT_MODE:
			this.handleInputInsert(event);
			break;
		case CHANGE_MODE:
			this.handleInputChange(event);
			break;
		case CHANGE_IN_MODE:
			this.handleInputChangeIn(event);
			break;
		}
		if (event.error) {
			Toolkit.getDefaultToolkit().beep();
			e.consume();
		} else if (event.consumed) {
			e.consume();
		}
	}

	/*KeyListener*/
	@Override
	public void keyPressed(KeyEvent e)
	{
	}

	/*KeyListener*/
	@Override
	public void keyReleased(KeyEvent e)
	{
	}

	/*CommandBar.Listener*/
	@Override
	public boolean acceptCommand(String command)
	{
		return false;
	}

	/*FocusListener*/
	@Override
	public void focusGained(FocusEvent e)
	{
		this.repaint(); // TODO: should only repaint the cursor really
	}

	/*FocusListener*/
	@Override
	public void focusLost(FocusEvent e)
	{
		this.repaint(); // TODO: should only repaint the cursor really
	}

	/*MouseListener*/
	@Override
	public void mouseClicked(MouseEvent e)
	{
	}

	/*MouseListener*/
	@Override
	public void mousePressed(MouseEvent e)
	{
	}

	/*MouseListener*/
	@Override
	public void mouseReleased(MouseEvent e)
	{
	}

	/*MouseListener*/
	@Override
	public void mouseEntered(MouseEvent e)
	{
	}

	/*MouseListener*/
	@Override
	public void mouseExited(MouseEvent e)
	{
	}

	private void ensureCodeViewSize()
	{
		this.frame.ensureCodeViewSize(this.lines.size(), this.maxLineLength + /*caret*/1);
	}

	private void recheckMaxLineLength()
	{
		this.maxLineLength = 0;
		for (int i = this.lines.size(); i > 0;) {
			int visualLen = this.lines.get(--i).length();
			if (visualLen > this.maxLineLength) {
				this.maxLineLength = visualLen;
			}
		}
	}

	private static class KeyInput
	{
		public char c;
		public boolean consumed;
		public boolean error;
	}
}
