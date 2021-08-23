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

	private int maxLineLength;

	public CodePanel(CodeFrame frame, JeanineFrame jf, String code)
	{
		this.jf = jf;
		this.j = jf.j;
		this.frame = frame;
		this.group = frame.group;
		this.setFocusable(true);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addKeyListener(this);
		this.addFocusListener(this);
		this.buffer = new EditBuffer(jf.j, code);
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
		int rely = thisloc.y - contentloc.y + this.j.fy;

		EditBuffer ec = this.buffer;
		g.setColor(Color.white);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		g.translate(1, 1);
		heightleft--;
		if (this.buffer.mode == EditBuffer.SELECT_LINE_MODE) {
			g.setColor(selectColor);
			int fromy = this.buffer.lineselectfrom * this.j.fy;
			int toy = this.buffer.lineselectto * this.j.fy;
			g.fillRect(0, fromy, this.maxLineLength * this.j.fx, toy - fromy);
		}
		if (this.buffer.mode == EditBuffer.INSERT_MODE) {
			g.setColor(Color.green);
		} else {
			g.setColor(Color.red);
		}
		int caretx = Line.logicalToVisualPos(ec.lines.get(ec.carety), ec.caretx);
		if (this.hasFocus() || this.buffer.mode != EditBuffer.NORMAL_MODE) {
			g.fillRect(caretx * this.j.fx, ec.carety * this.j.fy, this.j.fx, this.j.fy);
		}
		g.setFont(this.j.font);
		g.setColor(Color.black);
		int i = 0;
		while (rely < 0) {
			i++;
			g.translate(0, this.j.fy);
			rely += this.j.fy;
		}
		for (; i < ec.lines.size() && heightleft > 0; i++) {
			g.drawString(Line.tabs2spaces(ec.lines.get(i)), 0, this.j.fmaxascend);
			g.translate(0, this.j.fy);
			heightleft -= this.j.fy;
		}
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
		this.buffer.handlePhysicalInput(event);
		e.consume();
		if (event.error) {
			if (event.c == ':') {
				this.jf.commandbar.show("", this);
				return;
			}
		}
		if (event.error) {
			Toolkit.getDefaultToolkit().beep();
		}
		if (event.needRepaint || event.needRepaintCaret /*TODO: only repaint caret*/) {
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
		y = Math.min(y, this.buffer.lines.size() - 1);
		x = Math.min(x, this.buffer.lines.get(y).length() - 1);
		if (this.buffer.carety != y || this.buffer.caretx != x) {
			this.buffer.carety = y;
			this.buffer.caretx = x;
			this.buffer.virtualCaretx = x;
			this.repaint(); // TODO: should only really repaint caret
		}
	}

	public void ensureCodeViewSize()
	{
		int rows = this.buffer.lines.size();
		this.frame.ensureCodeViewSize(rows, this.maxLineLength + /*caret*/1);
	}

	public void recheckMaxLineLength()
	{
		this.maxLineLength = 0;
		for (int i = this.buffer.lines.size(); i > 0;) {
			int visualLen = this.buffer.lines.get(--i).length();
			if (visualLen > this.maxLineLength) {
				this.maxLineLength = visualLen;
			}
		}
	}
}
