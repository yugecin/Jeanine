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

	public final StringBuilder cmd;

	private int caretPos;

	public boolean active;

	public CommandBar(Jeanine j)
	{
		this.j = j;
		this.cmd = new StringBuilder();
		this.setFont(j.font);
		this.setBackground(Color.white);
		this.setOpaque(true);
		this.setBorder(new LineBorder(Color.black, 1));
		this.setVisible(false);
		this.setFocusable(false);
	}

	public void show(String text)
	{
		this.cmd.setLength(0);
		this.cmd.append(text);
		this.caretPos = this.cmd.length();
		Container parent = this.getParent();
		this.setSize(parent.getWidth() - 4, this.j.fy + 4);
		this.setLocation(2, parent.getHeight() - this.getHeight() - 2);
		this.setVisible(true);
		this.active = true;
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
		g.drawString(':' + this.cmd.toString(), 0, this.j.fmaxascend);
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
			if (this.caretPos < this.cmd.length()) {
				this.caretPos++;
				break;
			}
			return;
		case EditBuffer.BS:
			if (this.caretPos == 0) {
				this.cmd.setLength(0);
				this.active = false;
				this.setVisible(false);
				return;
			}
			this.caretPos--;
			this.cmd.delete(this.caretPos, this.caretPos + 1);
			break;
		case EditBuffer.DEL:
			if (this.caretPos < this.cmd.length()) {
				this.cmd.delete(this.caretPos, this.caretPos + 1);
			}
			break;
		case EditBuffer.ESC:
			this.cmd.setLength(0);
		case '\n':
			this.setVisible(false);
			this.active = false;
			return;
		default:
			this.cmd.insert(this.caretPos++, c);
		}
		this.repaint();
	}
}
