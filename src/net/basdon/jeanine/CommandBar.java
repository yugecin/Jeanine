package net.basdon.jeanine;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class CommandBar extends JPanel implements FocusListener, KeyListener
{
	private final Jeanine j;

	private ArrayList<Listener> listeners;
	private Component componentToFocusAfter;
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
		this.listeners = new ArrayList<>();
	}

	public void addListener(Listener listener)
	{
		this.listeners.add(listener);
	}

	public void show(String text, Component componentToFocusAfter)
	{
		this.componentToFocusAfter = componentToFocusAfter;
		this.sb = new StringBuilder(text);
		this.caretPos = this.sb.length();
		Container parent = this.getParent();
		this.setSize(parent.getWidth() - 4, this.j.fy + 4);
		this.setLocation(2, parent.getHeight() - this.getHeight() - 2);
		this.setVisible(true);
		this.requestFocusInWindow();
	}

	private void doHide()
	{
		this.setVisible(false);
		if (this.componentToFocusAfter != null) {
			this.componentToFocusAfter.requestFocusInWindow();
			this.componentToFocusAfter = null;
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
				e.consume();
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
			String cmd = this.sb.toString();
			for (int i = this.listeners.size();; ) {
				if (this.listeners.get(--i).acceptCommand(cmd)) {
					break;
				}
				if (i == 0) {
					Toolkit.getDefaultToolkit().beep();
					break;
				}
			}
		case KeyEvent.VK_ESCAPE:
			this.doHide();
			e.consume();
		default:
			return;
		}
		e.consume();
		this.repaint();
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
	}

	public interface Listener
	{
		boolean acceptCommand(String command);
	}
}
