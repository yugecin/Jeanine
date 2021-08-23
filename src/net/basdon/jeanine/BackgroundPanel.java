package net.basdon.jeanine;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JDesktopPane;

public class BackgroundPanel extends JDesktopPane implements MouseListener, MouseMotionListener
{
	private final Jeanine j;
	private final JeanineFrame jf;

	private Point dragStart;

	public BackgroundPanel(Jeanine j, JeanineFrame jf)
	{
		this.setLayout(null);
		this.j = j;
		this.jf = jf;
		this.setFocusable(false);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.dragStart = new Point();
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		int w, h;

		w = this.getWidth();
		h = this.getHeight();
		Paint p = new GradientPaint(0f, 0f, Color.white, 0f, h, new Color(224, 248, 255, 255));
		((Graphics2D) g).setPaint(p);
		g.fillRect(0, 0, w, h);
	}

	/*MouseMotionListener*/
	@Override
	public void mouseDragged(MouseEvent e)
	{
		int x = e.getX() - this.dragStart.x;
		int y = e.getY() - this.dragStart.y;
		this.dragStart.x = e.getX();
		this.dragStart.y = e.getY();

		for (CodeGroup cg : this.jf.codegroups) {
			cg.mouseDragged(x, y);
		}
	}

	/*MouseMotionListener*/
	@Override
	public void mouseMoved(MouseEvent e)
	{
	}

	@Override
	/*MouseListener*/
	public void mouseClicked(MouseEvent e)
	{
	}

	/*MouseListener*/
	@Override
	public void mousePressed(MouseEvent e)
	{
		this.dragStart.x = e.getX();
		this.dragStart.y = e.getY();
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
}
