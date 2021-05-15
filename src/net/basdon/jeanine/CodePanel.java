package net.basdon.jeanine;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JInternalFrame;

public class CodePanel extends JInternalFrame implements MouseListener, KeyListener, CommandBar.Listener
{
	private final JeanineFrame jf;

	public Point locationBeforeDrag;


	public CodePanel(JeanineFrame jf)
	{
		super("hi", false, false, false, false);
		this.jf = jf;
		this.locationBeforeDrag = new Point();
		this.setFrameIcon(null);
		this.setVisible(true);
		this.setFocusable(true);
		this.setBackground(Color.white);
		this.addMouseListener(this);
		this.addKeyListener(this);
	}

	public void setCodeViewSize(int rows, int cols)
	{
		int w, h;

		w = cols * 10 +
			/*border size*/ 2 + /*raised border size*/ 1;
		h = rows * 10 +
			/*border size*/ 2 + /*raised border size*/ 1 +
			/*header height*/ 10 + /*header bottom border*/ 1;

		this.setSize(w, h);
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		if (e.getKeyChar() == ':') {
			this.jf.commandbar.show("", this);
			e.consume();
		}
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
	}

	@Override
	public boolean acceptCommand(String command)
	{
		return false;
	}
}
