package net.basdon.jeanine;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;

public class OverlayPanel extends JComponent
{
	private final JeanineFrame jf;
	private final Jeanine j;

	public OverlayPanel(JeanineFrame jf)
	{
		this.jf = jf;
		this.j = jf.j;
		this.setLayout(null);
		this.setFocusable(false);
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		int x1, x2, y1, y2;
		Rectangle a = new Rectangle(), b = new Rectangle();
		g.setColor(Color.gray);
		for (CodeGroup group : this.jf.codegroups) {
			for (CodePanel panel : group.panels.values()) {
				if (panel.parent != null) {
					panel.getBounds(a);
					panel.parent.getBounds(b);
					switch (panel.anchor) {
					case 'b':
						// top line (child)
						x1 = a.x + a.width / 2;
						x2 = x1;
						y1 = a.y;
						y2 = a.y - 10;
						g.drawLine(x1, y1, x2, y2);
						// connecting line
						x1 = b.x + b.width / 2;
						y1 = b.y + b.height + 10;
						g.drawLine(x1, y1, x2, y2);
						// bottom line (parent)
						x2 = x1;
						y2 = y1 - 10;
						g.drawLine(x1, y1, x2, y2);
						break;
					case 't':
						// right line (child)
						x1 = a.x;
						x2 = a.x - 10;
						y1 = a.y + this.j.fy / 2;
						y2 = y1;
						g.drawLine(x1, y1, x2, y2);
						// connecting line
						x1 = b.x + b.width + 10;
						y1 = b.y + this.j.fy / 2;
						g.drawLine(x1, y1, x2, y2);
						// left line (parent)
						x2 = b.x + b.width;
						y2 = y1;
						g.drawLine(x1, y1, x2, y2);
						break;
					}
				}
			}
		}
	}
}
