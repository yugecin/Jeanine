package net.basdon.jeanine;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

public class CodePanel
extends JPanel
implements MouseListener, MouseMotionListener
{
	private static final char[] BLOCK_COMMENT_START = new char[] { '/', '*' };
	private static final char[] BLOCK_COMMENT_END = new char[] { '*', '/' };
	private static final char[] COMMENT_START = new char[] { '/', '/' };

	public final CodeGroup group;
	public final JeanineFrame jf;
	public final Jeanine j;
	public final EditBuffer buffer;
	public final Integer id;

	public int firstline;
	/**
	 * Exclusive
	 */
	public int lastline;
	public boolean requireValidation;
	public boolean needRepaint;
	public CodePanel parent;
	public int link;
	public Point location;

	private int maxLineLength;
	private int rows, cols;
	private boolean isDragging;
	private Point dragStart;

	public CodePanel(CodeGroup group, Integer id, int linefrom, int lineto)
	{
		this.jf = group.jf;
		this.j = this.jf.j;
		this.id = id;
		this.group = group;
		this.buffer = group.buffer;
		this.firstline = linefrom;
		this.lastline = lineto;
		this.location = new Point();
		this.setFocusable(false);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.recheckMaxLineLength();
		this.ensureCodeViewSize();
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		Point thisloc = this.getLocationOnScreen();
		Point contentloc = this.jf.getContentPane().getLocationOnScreen();
		Dimension contentsize = this.jf.getContentPane().getSize();
		int heightleft = contentsize.height - (thisloc.y - contentloc.y);
		int hiddenHeight = contentloc.y - thisloc.y;

		int w = this.getWidth() - 1;
		int h = this.getHeight() - 1;

		g.setFont(this.j.font);
		g.setColor(Colors.border.col);
		g.drawRect(0, 0, w, h); // border
		g.translate(1, 1);
		w--;
		h--;
		hiddenHeight--;
		if (hiddenHeight < this.j.fy) { // title
			g.setColor(Colors.headerBg.col);
			g.fillRect(0, 0, w, this.j.fy + 2);
			g.setColor(Colors.headerFg.col);
			SB title = new SB(100);
			title.append(String.valueOf(this.id));
			if (this.parent != null) {
				title.append('^').append(String.valueOf(this.parent.id));
			}
			if (this.group.title != null) {
				title.append('|').append(this.group.title);
			}
			title.append('|').append(this.firstline).append('-').append(this.lastline);
			if (this.buffer.readonly) {
				title.append("|RO");
			}
			if (this.group.raw) {
				title.append("|RAW");
			}
			g.drawString(title.toString(), 1, 1 + this.j.fmaxascend);
			g.setColor(Colors.headerBorder.col);
			g.drawRect(0, this.j.fy + 1, w - 1, 0); // border
		}
		hiddenHeight -= this.j.fy + 2;
		h -= this.j.fy + 2;
		g.translate(0, this.j.fy + 2);
		heightleft--;

		g.setColor(Colors.textBg.col);
		g.fillRect(0, 0, w, h);

		g.translate(1, 1);
		w -= 2;

		// line selection
		if (this.buffer.mode == EditBuffer.SELECT_LINE_MODE) {
			g.setColor(Colors.selectionBg.col);
			int fromy = Math.max(this.buffer.lineselectfrom, this.firstline);
			int toy = Math.min(this.buffer.lineselectto, this.lastline);
			fromy -= this.firstline;
			toy -= this.firstline;
			if (fromy < toy) {
				int y = fromy * this.j.fy;
				int height = this.j.fy * toy - y;
				g.fillRect(0, y, w, height);
			}
		}

		boolean needCaret = this.group.shouldDrawCaret(this);

		// code & caret
		int ma = this.j.fmaxascend;
		boolean inBlockComment = false;
		for (int i = this.firstline; i < this.lastline && heightleft > 0; i++) {
			if (hiddenHeight < this.j.fy) {
				SB l = this.buffer.lines.get(i);
				SB line = Line.tabs2spaces(l);
				// mark ending whitespace
				if (this.buffer.mode == EditBuffer.NORMAL_MODE ||
					this.buffer.carety != i)
				{
					int wsfrom = line.length, wsto = line.length;
					while (wsfrom > 0) {
						char c = line.value[wsfrom - 1];
						if (c != '\t' && c != ' ') {
							break;
						}
						wsfrom--;
					}
					if (wsfrom != wsto) {
						int f = wsfrom * this.j.fx;
						int t = wsto * this.j.fx - f;
						g.setColor(Colors.whitespaceBg.col);
						g.fillRect(f, 0, t, this.j.fy);
					}
				}
				// search highlighting
				if (this.jf.liveSearchText != null) {
					int idx = 0;
					for (;;) {
						idx = line.indexOf(this.jf.liveSearchText, idx);
						if (idx != -1) {
							int f = Line.logicalToVisualPos(line, idx);
							int t = idx + this.jf.liveSearchText.length;
							t = Line.logicalToVisualPos(line, t) - f;
							f *= this.j.fx;
							g.setColor(Color.cyan);
							g.fillRect(f, 0, t * this.j.fx, this.j.fy);
							idx++;
						} else {
							break;
						}
					}
				}
				// caret
				if (needCaret && this.buffer.carety == i) {
					int y = 0, height = this.j.fy;
					if (this.buffer.mode == EditBuffer.INSERT_MODE) {
						g.setColor(Color.green);
					} else if (this.buffer.mode == EditBuffer.NORMAL_MODE) {
						g.setColor(Color.red);
					} else {
						y = height = this.j.fy / 2;
						g.setColor(Color.red);
					}
					int x = Line.logicalToVisualPos(l, this.buffer.caretx);
					g.fillRect(x * this.j.fx, y, this.j.fx, height);
				}
				// the code text
				// try coloring comments
				// this should check if comment start is not in string, but oh well
				int from = 0;
				int to = line.length;
				int x = 0;
				int len;
				while (from < to) {
					if (inBlockComment) {
						int idx = line.indexOf(BLOCK_COMMENT_END, from);
						if (idx == -1) {
							g.setColor(Colors.commentFg.col);
							len = to - from;
							g.drawChars(line.value, from, len, x, ma);
							break;
						}
						idx += 2;
						g.setColor(Colors.commentFg.col);
						len = idx - from;
						g.drawChars(line.value, from, len, x, ma);
						inBlockComment = false;
						x += this.j.fx * len;
						from = idx;
					} else {
						int lidx = line.indexOf(COMMENT_START, from);
						int bidx = line.indexOf(BLOCK_COMMENT_START, from);
						if (lidx != -1 && bidx != -1) {
							if (lidx < bidx) {
								bidx = -1;
							} else {
								lidx = -1;
							}
						}
						if (bidx == -1 && lidx == -1) {
							// no comment
							g.setColor(Colors.textFg.col);
							len = to - from;
							g.drawChars(line.value, from, len, x, ma);
							break;
						} else if (bidx != -1) {
							// block comment
							g.setColor(Colors.textFg.col);
							len = bidx - from;
							g.drawChars(line.value, from, len, x, ma);
							x += this.j.fx * len;
							inBlockComment = true;
							from = bidx;
						} else {
							// line comment
							g.setColor(Colors.textFg.col);
							len = lidx - from;
							g.drawChars(line.value, from, len, x, ma);
							x += this.j.fx * len;
							g.setColor(Colors.commentFg.col);
							len = to - lidx;
							g.drawChars(line.value, lidx, len, x, ma);
							break;
						}
					}
				}
			} else {
				hiddenHeight -= this.j.fy;
			}
			g.translate(0, this.j.fy);
			heightleft -= this.j.fy;
		}

		this.needRepaint = false;
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
		if (!this.jf.shouldBlockInput()) {
			if (e.getY() < this.j.fy + 2) {
				this.isDragging = true;
				this.dragStart = e.getLocationOnScreen();
			} else if (this.group.focusGained(this)) {
				this.putCaretFromMouseInput(e.getX(), e.getY());
			}
		}
	}

	/*MouseListener*/
	@Override
	public void mouseReleased(MouseEvent e)
	{
		if (!this.jf.shouldBlockInput()) {
			if (this.isDragging) {
				this.isDragging = false;
				this.dragStart = null;
			} else if (this.group.hasFocus(this)) {
				this.putCaretFromMouseInput(e.getX(), e.getY());
			}
		}
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

	/*MouseMotionListener*/
	@Override
	public void mouseDragged(MouseEvent e)
	{
		if (!this.jf.shouldBlockInput()) {
			if (this.isDragging) {
				Point now = e.getLocationOnScreen();
				this.location.x += now.x - this.dragStart.x;
				this.location.y += now.y - this.dragStart.y;
				this.group.position(this);
				this.dragStart = now;
				this.jf.overlay.repaint();
			} else if (this.group.hasFocus(this)) {
				this.putCaretFromMouseInput(e.getX(), e.getY());
			}
		}
	}

	/*MouseMotionListener*/
	@Override
	public void mouseMoved(MouseEvent e)
	{
	}

	public void handleInput(KeyInput event)
	{
		this.buffer.handlePhysicalInput(event);
		if (event.needGlobalRepaint) {
			this.group.repaintAll();
		} else if (event.needRepaint ||
			/*TODO: only repaint caret*/
			event.needRepaintCaret)
		{
			this.repaint();
		}
		if (event.needCheckLineLength) {
			this.recheckMaxLineLength();
		}
		if (event.needEnsureViewSize) {
			this.ensureCodeViewSize();
		}
	}

	private void putCaretFromMouseInput(int x, int y)
	{
		x -=
			/*border left*/ 1 +
			/*content padding left*/ 1;
		x /= this.j.fx;
		y -=
			/*border top*/ 1 +
			/*title padding up/down*/ 2 +
			/*title*/ this.j.fy +
			/*content padding up*/ 1;
		y /= this.j.fy;
		y = Math.min(y, this.lastline - this.firstline - 1);
		y += this.firstline;
		if (y < this.firstline) {
			y = this.firstline;
		}
		SB line = this.buffer.lines.get(y);
		int len = Line.visualLength(line);
		x = Math.min(x, len - 1);
		if (x < 0) {
			x = 0;
		}
		x = Line.visualToLogicalPos(line, x);
		if (this.buffer.carety != y || this.buffer.caretx != x) {
			this.buffer.carety = y;
			this.buffer.caretx = x;
			this.buffer.virtualCaretx = x;
			this.repaint(); // TODO: should only really repaint caret
		}
	}

	public void ensureCodeViewSize()
	{
		int rows = this.lastline - this.firstline;
		this.ensureCodeViewSize(rows, this.maxLineLength + /*caret*/1);
	}

	public void recheckMaxLineLength()
	{
		this.maxLineLength = 0;
		for (int i = this.firstline; i < this.lastline; i++) {
			int visualLen = Line.tabs2spaces(this.buffer.lines.get(i)).length;
			if (visualLen > this.maxLineLength) {
				this.maxLineLength = visualLen;
			}
		}
	}

	public void ensureCodeViewSize(int rows, int cols)
	{
		if (cols < 30) {
			cols = 30;
		}
		if (this.rows != rows || this.cols != cols) {
			this.setCodeViewSize(rows, cols);
		}
	}

	public void validateSize()
	{
		this.setCodeViewSize(this.rows, this.cols);
	}

	public void setCodeViewSize(int rows, int cols)
	{
		this.rows = rows;
		this.cols = cols;
		Dimension size = new Dimension();
		size.width =
			/*border left/right*/ 2 +
			/*padding left/right*/ 2 +
			/*content*/ cols * this.j.fx;
		size.height =
			/*border up/down*/ 2 +
			/*padding title up/down*/ 2 +
			/*title*/ this.j.fy +
			/*padding content up/down*/ 2 +
			/*content*/ rows * this.j.fy;
		if (this.getWidth() != size.width) {
			this.setSize(size);
			this.jf.overlay.repaint();
			this.group.positionChildrenOf(this);
		} else if (this.getHeight() != size.height) {
			this.setSize(size);
		}
	}

	public boolean isEventualParentOf(CodePanel other)
	{
		while (other != null) {
			if (other.parent == this) {
				return true;
			}
			other = other.parent;
		}
		return false;
	}
}
