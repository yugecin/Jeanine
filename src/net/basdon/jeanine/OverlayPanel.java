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
		for (CodeGroup group : this.jf.codegroups) {
			for (CodePanel panel : group.panels.values()) {
				if (panel.parent != null) {
					g.setColor(Color.gray);
					this.drawLink(g, panel.parent, panel, panel.link);
				}
				g.setColor(Color.lightGray);
				for (SecondaryLink ad : panel.secondaryLinks) {
					this.drawLink(g, panel, ad.child, ad.link);
				}
			}
		}
	}

	private void drawLink(Graphics g, CodePanel parent, CodePanel child, int link)
	{
		float scale = this.jf.scale / 10f;
		int hump = this.jf.scale;
		int x1, x2, y1, y2;
		Rectangle a = new Rectangle(), b = new Rectangle();
		child.getBounds(a);
		parent.getBounds(b);
		switch (PanelLink.getAnchor(link)) {
		case 'b':
			// top line (child)
			x1 = a.x + a.width / 2;
			x2 = x1;
			y1 = a.y;
			y2 = a.y - hump;
			g.drawLine(x1, y1, x2, y2);
			// connecting line
			x1 = b.x + b.width / 2;
			y1 = b.y + b.height + hump;
			g.drawLine(x1, y1, x2, y2);
			// bottom line (parent)
			x2 = x1;
			y2 = y1 - hump;
			g.drawLine(x1, y1, x2, y2);
			break;
		case 't':
			// right line (child)
			x1 = a.x;
			x2 = a.x - hump;
			y1 = a.y + (int) (scale * (
					Padding.BORDER +
					Padding.TOP +
					Padding.IN_HEADER +
					this.j.fy / 2
				));
			y2 = y1;
			g.drawLine(x1, y1, x2, y2);
			// connecting line
			x1 = b.x + b.width + hump;
			y1 = b.y + (int) (scale * (
					Padding.BORDER +
					Padding.TOP +
					Padding.IN_HEADER +
					this.j.fy / 2
				));
			g.drawLine(x1, y1, x2, y2);
			// left line (parent)
			x2 = b.x + b.width;
			y2 = y1;
			g.drawLine(x1, y1, x2, y2);
			break;
		case 'r':
			int line = PanelLink.getLine(link);
			// right line (child)
			x1 = a.x;
			x2 = a.x - hump;
			y1 = a.y + (int) (scale * (
					Padding.BORDER +
					Padding.TOP +
					Padding.IN_HEADER +
					this.j.fy +
					Padding.IN_HEADER +
					Padding.BETWEEN_HEADER_AND_CONTENTS +
					this.j.fy / 2
				));
			y2 = y1;
			g.drawLine(x1, y1, x2, y2);
			// connecting line
			int localLine = line - parent.firstline;
			x1 = b.x + b.width + hump;
			y1 = b.y + (int) (scale * (
					Padding.BORDER +
					Padding.TOP +
					Padding.IN_HEADER +
					this.j.fy +
					Padding.IN_HEADER +
					Padding.BETWEEN_HEADER_AND_CONTENTS +
					this.j.fy * localLine + this.j.fy / 2
				));
			g.drawLine(x1, y1, x2, y2);
			// left line (parent)
			// draw it to where the text ends
			SB l = parent.buffer.lines.get(line);
			x2 = b.x + (int) (scale * (
					(Line.visualLength(l) + 1) * this.j.fx +
					Padding.BORDER +
					Padding.RIGHT
				));
			y2 = y1;
			g.drawLine(x1, y1, x2, y2);
			break;
		}
	}
}
