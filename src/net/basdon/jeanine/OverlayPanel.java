package net.basdon.jeanine;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;

public class OverlayPanel extends JComponent
{
	private final JeanineFrame jf;
	private final Jeanine j;
	private final SB tmpsb;

	private CodePanel infopanel;
	private int infoX, infoY;

	public OverlayPanel(JeanineFrame jf)
	{
		this.jf = jf;
		this.j = jf.j;
		this.setLayout(null);
		this.setFocusable(false);
		this.tmpsb = new SB(100);
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
		if (this.infopanel != null) {
			tmpsb.length = 0;
			if (this.infopanel.group.title != null) {
				tmpsb.append(this.infopanel.group.title);
				tmpsb.append('|');
			}
			tmpsb.append(this.infopanel.firstline);
			tmpsb.append('-');
			tmpsb.append(this.infopanel.lastline);
			int w = tmpsb.length * this.j.fx + 4;
			int h = this.j.fy + 4;
			int x = this.infoX + 2;
			int y = this.infoY - 2 - h;
			g.setFont(this.j.font);
			g.translate(x, y);
			g.setColor(Color.black);
			g.drawRect(0, 0, w, h);
			g.translate(1, 1);
			g.setColor(Color.yellow);
			g.fillRect(0, 0, w - 2, h - 2);
			g.translate(1, 1);
			g.setColor(Color.black);
			g.drawString(this.tmpsb.toString(), 0, this.j.fmaxascend);
		}
	}

	private void drawLink(Graphics g, CodePanel parent, CodePanel child, int link)
	{
		float scale = this.jf.getRenderScale();
		int hump = (int) (10 * scale);
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

	public void showInfoForPanel(int x, int y, CodePanel panel)
	{
		if (this.infopanel != panel || this.infoX != x || this.infoY != y) {
			this.infopanel = panel;
			this.infoX = x;
			this.infoY = y;
			this.repaint();
		}
	}
}
