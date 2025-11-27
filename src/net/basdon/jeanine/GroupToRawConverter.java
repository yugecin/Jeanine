package net.basdon.jeanine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class GroupToRawConverter implements Iterator<SB>
{
	private final ArrayList<SB> lines;
	private final HashMap<Integer, CodePanel> panels;

	private CodePanel currentPanel;
	private SB next;
	private int carety;
	private CodePanel secondaryLinksPrintedFor;

	public int nextLine;
	/**
	 * This will be the y-location where the caret should be after converting to raw mode so
	 * that the caret will be on the same line as the line that was given in the constructor
	 * parameter {@code carety}.
	 */
	public int newRawCarety;
	/**
	 * The current carety location for the current line being printed, ie the line number where
	 * the carety should be in parsed mode for the current line number in raw mode.
	 */
	public int currentParsedCarety;

	public GroupToRawConverter(CodeGroup group)
	{
		this(group.buffer.lines.lines, group.panels, group.buffer.carety);
	}

	public GroupToRawConverter(
		ArrayList<SB> lines,
		HashMap<Integer, CodePanel> panels,
		int carety)
	{
		this.lines = lines;
		this.panels = panels;
		this.currentPanel = panels.get(Integer.valueOf(0));
		this.carety = carety;
		this.newRawCarety = carety;
	}

	@Override
	public boolean hasNext()
	{
		if (this.next != null) {
			return true;
		}
		if (this.currentPanel.lastline <= this.nextLine) {
			if (this.secondaryLinksPrintedFor != this.currentPanel) {
				for (int i = 0; i < this.currentPanel.secondaryLinks.size(); i++) {
					SecondaryLink slink;
					slink = this.currentPanel.secondaryLinks.get(i);
					char anchor = PanelLink.getAnchor(slink.link);
					// right links are put on their line, not at the end
					if (anchor != 'r') {
						if (this.next == null) {
							this.next = new SB(100);
							this.next.append("/*jeanine");
						}
						this.next.append(":s:a:");
						this.next.append(anchor);
						this.next.append(";i:");
						this.next.append(slink.child.id.intValue());
						this.next.append(';');
					}
				}
				this.secondaryLinksPrintedFor = this.currentPanel;
				if (this.next != null) {
					this.next.append("*/");
					return true;
				}
			}
			if (this.nextLine >= this.lines.size()) {
				return false;
			}
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
					if (panel.location.x != 0) {
						this.next.append("x:");
						this.next.append(panel.location.x, 2);
						this.next.append(';');
					}
					if (panel.location.y != 0) {
						this.next.append("y:");
						this.next.append(panel.location.y, 2);
						this.next.append(';');
					}
					if (panel.name != null) {
						this.next.append("n:");
						this.next.append(panel.name);
						this.next.append(';');
					}
					this.next.append("*/");
					if (this.carety >= 0) {
						this.newRawCarety++;
					}
					return true;
				}
			}
			// fallback if no codepanel owns next line
		}
		this.carety--;
		this.currentParsedCarety++;
		this.next = lines.get(this.nextLine);
		boolean hasLink = false;
		for (CodePanel panel : panels.values()) {
			if (panel.parent == currentPanel &&
				PanelLink.getAnchor(panel.link) == 'r' &&
				PanelLink.getLine(panel.link) == this.nextLine)
			{
				if (!hasLink) {
					this.next = new SB(this.next, 100);
					this.next.append("/*jeanine");
					hasLink = true;
				}
				this.next.append(":r:i:");
				this.next.append(panel.id.intValue());
				this.next.append(';');
			}
		}
		for (int i = 0; i < this.currentPanel.secondaryLinks.size(); i++) {
			SecondaryLink slink;
			slink = this.currentPanel.secondaryLinks.get(i);
			if (PanelLink.getAnchor(slink.link) == 'r' &&
				PanelLink.getLine(slink.link) == this.nextLine)
			{
				if (!hasLink) {
					this.next = new SB(this.next, 100);
					this.next.append("/*jeanine");
					hasLink = true;
				}
				this.next.append(":s:a:r;i:");
				this.next.append(slink.child.id.intValue());
				this.next.append(';');
			}
		}
		if (hasLink) {
			this.next.append("*/");
		}
		this.nextLine++;
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
