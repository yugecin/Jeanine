package net.basdon.jeanine;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class CommandBar extends JPanel implements FocusListener, KeyListener
{
	private final Jeanine j;

	private Listener listener;
	private StringBuilder sb;
	private int caretPos;

	public CommandBar(Jeanine j)
	{
		this.j = j;
		this.setFont(j.font);
		this.setBackground(Color.white);
		this.setOpaque(true);
		this.setBorder(new LineBorder(Color.black, 1));
		this.setVisible(false);
		this.addFocusListener(this);
		this.addKeyListener(this);
	}

	public void show(String text, Listener listener)
	{
		this.sb = new StringBuilder(text);
		this.caretPos = this.sb.length();
		Container parent = this.getParent();
		this.setSize(parent.getWidth() - 4, this.j.fy + 4);
		this.setLocation(2, parent.getHeight() - this.getHeight() - 2);
		this.setVisible(true);
		this.requestFocusInWindow();
		this.listener = listener;
	}

	private void doHide()
	{
		this.setVisible(false);
		if (this.listener != null) {
			Component c = this.listener.getComponentToFocus();
			if (c != null) {
				c.requestFocusInWindow();
			}
			this.listener.acceptCommand(this.sb.toString());
			this.listener = null;
		}
	}

	@Override
	public void focusGained(FocusEvent e)
	{
	}

	@Override
	public void focusLost(FocusEvent e)
	{
		this.doHide();
	}

	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		g.translate(2, 2);
		g.setColor(Color.magenta);
		g.fillRect(this.j.fx * (1 + this.caretPos), 0, this.j.fx, this.j.fy);
		g.setColor(Color.black);
		g.setFont(this.j.font);
		g.drawString(':' + this.sb.toString(), 0, this.j.fmaxascend);
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		char c = e.getKeyChar();
		if (c != KeyEvent.CHAR_UNDEFINED && c != /*bs*/ 8 && c != /*del*/ 127) {
			this.sb.insert(this.caretPos++, c);
			this.repaint();
		}
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		int c = e.getKeyCode();
		switch (c) {
		case KeyEvent.VK_LEFT:
			if (this.caretPos > 0) {
				this.caretPos--;
			}
			break;
		case KeyEvent.VK_RIGHT:
			if (this.caretPos < this.sb.length()) {
				this.caretPos++;
			}
			break;
		case KeyEvent.VK_BACK_SPACE:
			if (this.caretPos == 0) {
				this.doHide();
				return;
			}
			this.caretPos--;
			this.sb.delete(this.caretPos, this.caretPos + 1);
			break;
		case KeyEvent.VK_DELETE:
			if (this.caretPos < this.sb.length()) {
				this.sb.delete(this.caretPos, this.caretPos + 1);
			}
			break;
		case KeyEvent.VK_ENTER:
			this.doHide();
			break;
		default:
			return;
		}
		this.repaint();
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
	}

	public interface Listener
	{
		Component getComponentToFocus();
		void acceptCommand(String command);
	}
}
