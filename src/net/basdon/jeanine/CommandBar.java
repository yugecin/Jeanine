package net.basdon.jeanine;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class CommandBar extends JPanel
{
	private final Jeanine j;

	public final StringBuilder val;

	private int caretPos;
	private char cmdChar;

	public boolean active;

	public CommandBar(Jeanine j)
	{
		this.j = j;
		this.val = new StringBuilder();
		this.setFont(j.font);
		this.setBackground(Color.white);
		this.setOpaque(true);
		this.setBorder(new LineBorder(Color.black, 1));
		this.setVisible(false);
		this.setFocusable(false);
	}

	private void showBar()
	{
		this.val.setLength(0);
		this.caretPos = this.val.length();
		Container parent = this.getParent();
		this.setSize(parent.getWidth() - 4, this.j.fy + 4);
		this.setLocation(2, parent.getHeight() - this.getHeight() - 2);
		this.setVisible(true);
		this.active = true;
	}

	public void showForCommand()
	{
		this.cmdChar = ':';
		this.showBar();
	}

	public void showForSearch()
	{
		this.cmdChar = '/';
		this.showBar();
	}

	public boolean isCommand()
	{
		return this.cmdChar == ':';
	}

	public boolean isSearch()
	{
		return this.cmdChar == '/';
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
		g.drawString(this.cmdChar + this.val.toString(), 0, this.j.fmaxascend);
	}

	public void handleKey(char c)
	{
		switch (c) {
		case KeyEvent.VK_LEFT:
			if (this.caretPos > 0) {
				this.caretPos--;
				break;
			}
			return;
		case KeyEvent.VK_RIGHT:
			if (this.caretPos < this.val.length()) {
				this.caretPos++;
				break;
			}
			return;
		case EditBuffer.BS:
			if (this.caretPos == 0) {
				this.val.setLength(0);
				this.active = false;
				this.setVisible(false);
				return;
			}
			this.caretPos--;
			this.val.delete(this.caretPos, this.caretPos + 1);
			break;
		case EditBuffer.DEL:
			if (this.caretPos < this.val.length()) {
				this.val.delete(this.caretPos, this.caretPos + 1);
			}
			break;
		case EditBuffer.ESC:
			this.val.setLength(0);
		case '\n':
			this.setVisible(false);
			this.active = false;
			return;
		default:
			this.val.insert(this.caretPos++, c);
		}
		this.repaint();
	}
}
