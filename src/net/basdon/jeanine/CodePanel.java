package net.basdon.jeanine;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.KeyboardFocusManager;
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

public class CodePanel extends JPanel implements MouseListener, FocusListener, KeyListener, CommandBar.Listener
{
	private static final int NORMAL_MODE = 0, INSERT_MODE = 1;

	private final CodeFrame frame;
	private final JeanineFrame jf;
	private final Jeanine j;

	private ArrayList<StringBuilder> lines;
	private int maxLineLength;
	private int caretx, carety, virtualCaretx;
	private int mode;

	public CodePanel(CodeFrame frame, JeanineFrame jf)
	{
		this.jf = jf;
		this.j = jf.j;
		this.lines = new ArrayList<>();
		this.lines.add(new StringBuilder());
		this.frame = frame;
		this.setFocusable(true);
		this.addMouseListener(this);
		this.addKeyListener(this);
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
		if (this.hasFocus()) {
			g.fillRect(this.caretx * this.j.fx, this.carety * this.j.fy, this.j.fx, this.j.fy);
		} else {
			g.drawRect(this.caretx * this.j.fx, this.carety * this.j.fy, this.j.fx, this.j.fy);
		}
		g.setFont(this.j.font);
		g.setColor(Color.black);
		for (int line = 0; line < this.lines.size(); line++) {
			g.drawString(this.lines.get(line).toString(), 0, this.j.fmaxascend);
			g.translate(0, this.j.fy);
		}
	}

	private void keyTypedNormal(KeyEvent e, char c)
	{
		switch (c) {
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
		case 'i':
			this.mode = INSERT_MODE;
			this.repaint(); // TODO: only should repaint the caret
			break;
		}
		e.consume();
	}

	private void keyTypedInsert(KeyEvent e, char c)
	{
		switch (c) {
		case KeyEvent.CHAR_UNDEFINED:
		case /*bs*/8:
		case /*del*/127:
			e.consume();
			return;
		case '\r':
		case '\n':
			this.lines.add(new StringBuilder());
			this.caretx = this.virtualCaretx = 0;
			this.carety++;
			break;
		default:
			StringBuilder sb = this.lines.get(this.carety);
			sb.insert(this.caretx, c);
			if (sb.length() > this.maxLineLength) {
				this.maxLineLength = sb.length();
			}
			this.virtualCaretx = ++this.caretx;
		}
		e.consume();
		this.ensureCodeViewSize();
		this.repaint();
	}

	/*KeyListener*/
	@Override
	public void keyTyped(KeyEvent e)
	{
		char c = e.getKeyChar();
		switch (mode) {
		case NORMAL_MODE:
			this.keyTypedNormal(e, c);
			break;
		case INSERT_MODE:
			this.keyTypedInsert(e, c);
			break;
		}
	}

	private void keyPressedNormal(KeyEvent e, int c)
	{
		switch (c) {
		case KeyEvent.VK_J:
			if (this.carety < this.lines.size() - 1) {
				this.carety++;
				int linelen = this.lines.get(this.carety).length();
				this.caretx = this.virtualCaretx;
				if (this.caretx > linelen) {
					this.caretx = linelen;
				}
				this.repaint(); // TODO: only should repaint the caret
			} else {
				Toolkit.getDefaultToolkit().beep();
			}
			break;
		case KeyEvent.VK_K:
			if (this.carety > 0) {
				this.carety--;
				int linelen = this.lines.get(this.carety).length();
				this.caretx = this.virtualCaretx;
				if (this.caretx > linelen) {
					this.caretx = linelen;
				}
				this.repaint(); // TODO: only should repaint the caret
			} else {
				Toolkit.getDefaultToolkit().beep();
			}
			break;
		case KeyEvent.VK_H:
			if (this.caretx > 0) {
				this.caretx--;
				this.repaint(); // TODO: only should repaint the caret
			} else {
				Toolkit.getDefaultToolkit().beep();
			}
			break;
		case KeyEvent.VK_L:
			if (this.caretx < this.lines.get(this.carety).length()) {
				this.caretx++;
				this.repaint(); // TODO: only should repaint the caret
			} else {
				Toolkit.getDefaultToolkit().beep();
			}
			break;
		default:
			return;
		}
		e.consume();
	}

	private void keyPressedInsert(KeyEvent e, int c)
	{
		switch (c) {
		case KeyEvent.VK_ESCAPE:
			this.mode = NORMAL_MODE;
			this.virtualCaretx = this.caretx;
			this.repaint(); // TODO: only should repaint the caret
			break;
		case KeyEvent.VK_BACK_SPACE:
			if (this.caretx > 0) {
				this.lines.get(this.carety).delete(this.caretx - 1, this.caretx);
				this.caretx--;
				this.recheckMaxLineLength();
				this.ensureCodeViewSize();
				this.repaint();
			} else {
				Toolkit.getDefaultToolkit().beep();
			}
			break;
		default:
			return;
		}
		e.consume();
	}

	/*KeyListener*/
	@Override
	public void keyPressed(KeyEvent e)
	{
		int c = e.getKeyCode();
		switch (mode) {
		case NORMAL_MODE:
			this.keyPressedNormal(e, c);
			break;
		case INSERT_MODE:
			this.keyPressedInsert(e, c);
			break;
		}
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
			int len = this.lines.get(--i).length();
			if (len > this.maxLineLength) {
				this.maxLineLength = len;
			}
		}
	}
}
