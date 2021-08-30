package net.basdon.jeanine;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

public class CodePanel
extends JPanel
implements MouseListener, MouseMotionListener, FocusListener
{
	private static final Color selectColor = new Color(0x66AAFF);

	public final CodeGroup group;
	public final JeanineFrame jf;
	public final Jeanine j;
	public final EditBuffer buffer;
	public final Integer id;

	public String title;
	public int firstline;
	/**
	 * Exclusive
	 */
	public int lastline;
	public boolean requirePositionSizeValidation;
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
		this.addFocusListener(this);
		this.recheckMaxLineLength();
		this.ensureCodeViewSize();
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		Point thisloc = this.getLocationOnScreen();
		Point contentloc = this.jf.getContentPane().getLocationOnScreen();
		Dimension contentsize = this.jf.getContentPane().getSize();
		int heightleft = contentsize.height - (thisloc.y - contentloc.y);
		int hiddenHeight = contentloc.y - thisloc.y;

		int w = this.getWidth();
		int h = this.getHeight();

		g.setFont(this.j.font);
		g.setColor(Color.black);
		g.drawRect(0, 0, w - 1, h - 1);
		g.translate(1, 1);
		g.setColor(Color.white);
		g.fillRect(0, 0, w - 2, h - 2);
		if (hiddenHeight < this.j.fy) {
			g.setColor(new Color(0xdddddd));
			g.fillRect(0, 0, w - 2, this.j.fy);
			g.setColor(Color.black);
			SB title = new SB(100);
			title.append(String.valueOf(this.id));
			if (this.title != null) {
				title.append('|');
				title.append(this.title);
			}
			if (this.buffer.readonly) {
				title.append("|RO");
			}
			g.drawString(title.toString(), 0, this.j.fmaxascend);
		}
		hiddenHeight -= this.j.fy;
		g.translate(1, this.j.fy + 1);
		heightleft--;

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
				g.fillRect(0, y, this.maxLineLength * this.j.fx, height);
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

	/*FocusListener*/
	@Override
	public void focusGained(FocusEvent e)
	{
		if (this.group.focusGained(this)) {
			this.repaint(); // TODO: should only repaint the cursor really
		}
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
		if (e.getY() < this.j.fy + 2) {
			this.isDragging = true;
			this.dragStart = e.getLocationOnScreen();
		} else if (this.group.focusGained(this)) {
			this.putCaret(e.getX(), e.getY());
		}
	}

	/*MouseListener*/
	@Override
	public void mouseReleased(MouseEvent e)
	{
		if (this.isDragging) {
			this.isDragging = false;
			this.dragStart = null;
		} else if (this.group.hasFocus(this)) {
			this.putCaret(e.getX(), e.getY());
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
		if (this.isDragging) {
			Point now = e.getLocationOnScreen();
			this.location.x += now.x - this.dragStart.x;
			this.location.y += now.y - this.dragStart.y;
			this.group.position(this);
			this.dragStart = now;
		} else if (this.group.hasFocus(this)) {
			this.putCaret(e.getX(), e.getY());
		}
	}

	/*MouseMotionListener*/
	@Override
	public void mouseMoved(MouseEvent e)
	{
	}

	public void setTitle(String title)
	{
		this.title = title;
		this.repaint();
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
		x = (x - 2) / this.j.fx; // -2 for panel padding
		y = (y - 2 - this.j.fy) / this.j.fy; // -2 for panel padding, -fy for title
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

	public void setCodeViewSize(int rows, int cols)
	{
		this.rows = rows;
		this.cols = cols;
		Dimension size = new Dimension();
		size.width = /*padding*/ 4 + cols * this.j.fx;
		size.height = /*padding*/ 4 + (rows + 1) * this.j.fy;
		this.setSize(size);
	}
}
