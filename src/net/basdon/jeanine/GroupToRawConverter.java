package net.basdon.jeanine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class GroupToRawConverter implements Iterator<SB>
{
	private final ArrayList<SB> lines;
	private final HashMap<Integer, CodePanel> panels;

	private CodePanel currentPanel;
	private int nextLine;
	private SB next;

	public GroupToRawConverter(ArrayList<SB> lines, HashMap<Integer, CodePanel> panels)
	{
		this.lines = lines;
		this.panels = panels;
		this.currentPanel = panels.get(Integer.valueOf(0));
	}

	@Override
	public boolean hasNext()
	{
		if (this.next == null && this.nextLine < this.lines.size()) {
			if (currentPanel.lastline <= this.nextLine) {
				for (CodePanel panel : panels.values()) {
					if (panel.firstline == this.nextLine) {
						currentPanel = panel;
						this.next = new SB("/*jeanine:p:");
						this.next.append("i:");
						this.next.append(panel.id.intValue());
						this.next.append(';');
						this.next.append("p:");
						this.next.append(panel.parent.id.intValue());
						this.next.append(';');
						this.next.append("a:");
						this.next.append(PanelLink.getAnchor(panel.link));
						this.next.append(';');
						this.next.append("x:");
						this.next.append(panel.location.x);
						this.next.append(';');
						this.next.append("y:");
						this.next.append(panel.location.y);
						this.next.append(';');
						this.next.append("*/");
						return true;
					}
				}
				// fallback if no codepanel owns next line
			}
			this.next = lines.get(this.nextLine);
			boolean hasLink = false;
			for (CodePanel panel : panels.values()) {
				if (panel.parent == currentPanel &&
					PanelLink.getAnchor(panel.link) == 'r' &&
					PanelLink.getLine(panel.link) == this.nextLine)
				{
					if (!hasLink) {
						this.next = new SB(this.next);
						this.next.append("/*jeanine:l:");
						hasLink = true;
					} else {
						this.next.append(',');
					}
					this.next.append(panel.id.intValue());
				}
			}
			if (hasLink) {
				this.next.append("*/");
			}
			this.nextLine++;
		}
		return this.next != null;
	}

	@Override
	public SB next()
	{
		SB next = this.next;
		this.next = null;
		return next;
	}

}
