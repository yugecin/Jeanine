package net.basdon.jeanine;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;

public class OverlayPanel extends JComponent
{
	private JeanineFrame jf;

	public OverlayPanel(JeanineFrame jf)
	{
		this.jf = jf;
		this.setLayout(null);
		this.setFocusable(false);
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		Rectangle rect = new Rectangle();
		g.setColor(Color.gray);
		for (CodeGroup group : this.jf.codegroups) {
			for (CodePanel panel : group.panels.values()) {
				if (panel.parent != null) {
					panel.getBounds(rect);
					int x1 = rect.x + rect.width / 2;
					int x2 = x1;
					int y1 = rect.y;
					int y2 = rect.y - 10;
					g.drawLine(x1, y1, x2, y2);
					panel.parent.getBounds(rect);
					x1 = rect.x + rect.width / 2;
					y1 = rect.y + rect.height + 10;
					g.drawLine(x1, y1, x2, y2);
					x2 = x1;
					y2 = y1 - 10;
					g.drawLine(x1, y1, x2, y2);
				}
			}
		}
	}
}
