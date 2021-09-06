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
	private static final Color selectColor = new Color(0x66AAFF);

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
	public byte anchor;

	public CodePanel parent;
	public Point location;

	private int maxLineLength;
	private int rows, cols;
	private boolean isDragging;
	private Point dragStart;

	public CodePanel(JeanineFrame jf, CodeGroup group, Integer id, EditBuffer buffer, int linefrom, int lineto)
	{
		this.jf = jf;
		this.j = jf.j;
		this.id = id;
		this.group = group;
		this.buffer = buffer;
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
		g.setColor(Color.black);
		g.drawRect(0, 0, w, h); // border
		g.translate(1, 1);
		w--;
		h--;
		hiddenHeight--;
		if (hiddenHeight < this.j.fy) { // title
			g.setColor(new Color(0xdddddd));
			g.fillRect(0, 0, w, this.j.fy + 2);
			g.setColor(Color.black);
			SB title = new SB(100);
			title.append(String.valueOf(this.id));
			if (this.group.title != null) {
				title.append('|').append(this.group.title);
			}
			title.append('|').append(this.firstline).append('-').append(this.lastline);
			if (this.buffer.readonly) {
				title.append("|RO");
			}
			g.drawString(title.toString(), 1, 1 + this.j.fmaxascend);
		}
		hiddenHeight -= this.j.fy + 2;
		h -= this.j.fy + 2;
		g.translate(0, this.j.fy + 2);
		heightleft--;

		g.setColor(Color.white); // bg
		g.fillRect(0, 0, w, h);

		g.translate(1, 1);
		w -= 2;

		// line selection
		if (this.buffer.mode == EditBuffer.SELECT_LINE_MODE) {
			g.setColor(selectColor);
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

		// caret
		if ((this.group.hasFocus(this) || this.buffer.mode != EditBuffer.NORMAL_MODE) &&
			this.firstline <= this.buffer.carety && this.buffer.carety < this.lastline)
		{
			if (this.buffer.mode == EditBuffer.INSERT_MODE) {
				g.setColor(Color.green);
			} else {
				g.setColor(Color.red);
			}
			SB line = this.buffer.lines.get(this.buffer.carety);
			int x = Line.logicalToVisualPos(line, this.buffer.caretx) * this.j.fx;
			int y = (this.buffer.carety - this.firstline) * this.j.fy;
			g.fillRect(x, y, this.j.fx, this.j.fy);
		}

		// code
		g.setColor(Color.black);
		for (int i = this.firstline; i < this.lastline && heightleft > 0; i++) {
			if (hiddenHeight < this.j.fy) {
				SB line = Line.tabs2spaces(this.buffer.lines.get(i));
				g.drawString(line.toString(), 0, this.j.fmaxascend);
			} else {
				hiddenHeight -= this.j.fy;
			}
			g.translate(0, this.j.fy);
			heightleft -= this.j.fy;
		}
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
				this.putCaret(e.getX(), e.getY());
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
				this.putCaret(e.getX(), e.getY());
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
				this.putCaret(e.getX(), e.getY());
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

	private void putCaret(int x, int y)
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
		x = Math.min(x, this.buffer.lines.get(y).length() - 1);
		if (x < 0) {
			x = 0;
		}
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
		} else if (this.getHeight() != size.height) {
			this.setSize(size);
		}
	}
}
