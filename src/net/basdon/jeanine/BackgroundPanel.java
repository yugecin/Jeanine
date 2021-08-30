package net.basdon.jeanine;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;

import javax.swing.JPanel;

public class BackgroundPanel extends JPanel
{
	private final Color to = new Color(224, 248, 255, 255);

	public BackgroundPanel()
	{
		this.setLayout(null);
		this.setFocusable(false);
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		int w, h;

		w = this.getWidth();
		h = this.getHeight();
		Paint p = new GradientPaint(0f, 0f, Color.white, 0f, h, to);
		((Graphics2D) g).setPaint(p);
		g.fillRect(0, 0, w, h);
	}
}
