package net.basdon.jeanine;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;

import static java.util.Collections.EMPTY_SET;

public class CodePanel
extends JPanel
implements
	MouseListener, MouseMotionListener, FocusListener,
	KeyListener, CommandBar.Listener
{
	private static final Color selectColor = new Color(0x66AAFF);

	public final CodeFrame frame;
	public final CodeGroup group;
	public final JeanineFrame jf;
	public final Jeanine j;
	public final EditBuffer buffer;

	public int firstline;
	/**
	 * Exclusive
	 */
	public int lastline;

	private int maxLineLength;

	public CodePanel(CodeFrame frame, JeanineFrame jf)
	{
		this.jf = jf;
		this.j = jf.j;
		this.buffer = frame.buffer;
		this.lastline = this.buffer.lines.size();
		this.frame = frame;
		this.group = frame.group;
		this.setFocusable(true);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addKeyListener(this);
		this.addFocusListener(this);
		this.recheckMaxLineLength();
		this.ensureCodeViewSize();
		// make that we get the TAB key events
		LookAndFeel.installProperty(this, "focusTraversalKeysForward", EMPTY_SET);
		LookAndFeel.installProperty(this, "focusTraversalKeysBackward", EMPTY_SET);
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

		g.setColor(Color.white);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		g.translate(1, 1);
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
		if ((this.hasFocus() || this.buffer.mode != EditBuffer.NORMAL_MODE) &&
			this.firstline <= this.buffer.carety && this.buffer.carety < this.lastline)
		{
			if (this.buffer.mode == EditBuffer.INSERT_MODE) {
				g.setColor(Color.green);
			} else {
				g.setColor(Color.red);
			}
			StringBuilder line = this.buffer.lines.get(this.buffer.carety);
			int x = Line.logicalToVisualPos(line, this.buffer.caretx) * this.j.fx;
			int y = (this.buffer.carety - this.firstline) * this.j.fy;
			g.fillRect(x, y, this.j.fx, this.j.fy);
		}

		// code
		g.setFont(this.j.font);
		g.setColor(Color.black);
		for (int i = this.firstline; i < this.lastline && heightleft > 0; i++) {
			if (hiddenHeight < this.j.fy) {
				StringBuilder line = this.buffer.lines.get(i);
				g.drawString(Line.tabs2spaces(line), 0, this.j.fmaxascend);
			} else {
				hiddenHeight -= this.j.fy;
			}
			g.translate(0, this.j.fy);
			heightleft -= this.j.fy;
		}
	}

	/*KeyListener*/
	@Override
	public void keyTyped(KeyEvent e)
	{
		e.consume();
		this.handleInput(new KeyInput(e.getKeyChar()));
	}

	public void handleInput(KeyInput event)
	{
		this.group.dispatchInputEvent(event, this);
	}

	public void handleInputInternal(KeyInput event)
	{
		if (event.c == KeyEvent.CHAR_UNDEFINED) {
			return;
		}
		this.buffer.handlePhysicalInput(event);
		if (event.error) {
			if (event.c == ':') {
				this.jf.commandbar.show("", this);
				return;
			}
		}
		if (event.error) {
			Toolkit.getDefaultToolkit().beep();
		}
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
		if (this.group.focusGained(this.frame)) {
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
		// Won't have focus when the mouse is pressed, so use invokeLater
		SwingUtilities.invokeLater(() -> {
			if (this.hasFocus()) {
				this.putCaret(e.getX(), e.getY());
			}
		});
	}

	/*MouseListener*/
	@Override
	public void mouseReleased(MouseEvent e)
	{
		if (this.hasFocus()) {
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
		if (this.hasFocus()) {
			this.putCaret(e.getX(), e.getY());
		}
	}

	/*MouseMotionListener*/
	@Override
	public void mouseMoved(MouseEvent e)
	{
	}

	private void putCaret(int x, int y)
	{
		x = (x - 1) / this.j.fx; // -1 for panel padding
		y = (y - 1) / this.j.fy; // -1 for panel padding
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
		this.frame.ensureCodeViewSize(rows, this.maxLineLength + /*caret*/1);
	}

	public void recheckMaxLineLength()
	{
		this.maxLineLength = 0;
		for (int i = this.firstline; i < this.lastline; i++) {
			int visualLen = this.buffer.lines.get(i).length();
			if (visualLen > this.maxLineLength) {
				this.maxLineLength = visualLen;
			}
		}
	}
}
