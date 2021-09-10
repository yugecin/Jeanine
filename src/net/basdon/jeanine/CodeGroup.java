package net.basdon.jeanine;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.SwingUtilities;

public class CodeGroup
{
	public final Jeanine j;
	public final JeanineFrame jf;
	public final EditBuffer buffer;
	public final HashMap<Integer, CodePanel> panels;
	public final Point location;

	public CodePanel root;
	public File ownerFile;
	public CodePanel activePanel;
	public String title;

	public CodeGroup(JeanineFrame jf)
	{
		this.j = jf.j;
		this.jf = jf;
		this.panels = new HashMap<>();
		this.buffer = new EditBuffer(jf.j, this);
		this.location = new Point();
	}

	public void readFile(File file)
	{
		this.ownerFile = file;
	}

	public void setContents(String text)
	{
		SB sb = new SB();
		this.buffer.lines.clear();
		this.buffer.lines.add(sb);
		if (text != null) {
			for (char c : text.toCharArray()) {
				if (c == '\n') {
					sb = new SB();
					this.buffer.lines.add(sb);
				} else {
					sb.append(c);
				}
			}
		}

		this.panels.clear();
		Integer id = Integer.valueOf(0);
		this.root = new CodePanel(this.jf, this, id, this.buffer, 0, this.buffer.lines.size());
		this.panels.put(id, this.root);
		this.position(this.root);
		this.jf.getContentPane().add(this.root);
		this.activePanel = this.root;
	}

	public void setLocation(int x, int y)
	{
		this.location.x = x;
		this.location.y = y;
		if (this.root != null) {
			this.position(this.root);
		}
	}

	public void fontChanged()
	{
		for (CodePanel panel : this.panels.values()) {
			panel.validateSize();
		}
		this.setLocation(this.location.x, this.location.y);
	}

	public void updateLocation()
	{
		this.setLocation(this.location.x, this.location.y);
	}

	public void position(CodePanel panel)
	{
		int x = this.location.x + this.jf.location.x;
		int y = this.location.y + this.jf.location.y;
		if (panel.parent != null) {
			CodePanel parent = this.panels.get(panel.parent.id);
			if (parent != null) {
				Rectangle bounds = parent.getBounds();
				switch (PanelLink.getAnchor(panel.link)) {
				case 't':
					x = bounds.x + bounds.width;
					y = bounds.y;
					break;
				case 'r':
					int line = PanelLink.getLine(panel.link);
					x = bounds.x + bounds.width;
					y = bounds.y + this.j.fy * (line - panel.parent.firstline);
					break;
				case 'b':
					x = bounds.x + (bounds.width - panel.getWidth()) / 2;
					y = bounds.y + bounds.height;
					break;
				}
			}
		}
		x += panel.location.x;
		y += panel.location.y;
		panel.setLocation(x, y);
		panel.requireValidation = false;
		this.positionChildrenOf(panel);
	}

	public void positionChildrenOf(CodePanel panel)
	{
		for (CodePanel child : this.panels.values()) {
			if (child.parent == panel) {
				this.position(child);
			}
		}
	}

	public void split()
	{
		if (this.activePanel == null) {
			this.jf.setError("can't split, no code panel focused");
			return;
		}

		if (this.buffer.mode != EditBuffer.SELECT_LINE_MODE) {
			this.jf.setError("can't split, need to be in 'select line' mode (ctrl+v)");
			return;
		}
		if (this.panelAtLine(this.buffer.lineselectfrom) !=
			this.panelAtLine(this.buffer.lineselectto - 1))
		{
			this.jf.setError("can't split, selection spans multiple views");
			return;
		}

		CodePanel codepanel = this.activePanel;
		if (this.buffer.lineselectfrom == codepanel.firstline &&
			this.buffer.lineselectto == codepanel.lastline)
		{
			this.jf.setError("can't split, no lines left");
			return;
		}

		this.buffer.mode = EditBuffer.NORMAL_MODE;

		Integer nextId = Integer.valueOf(this.findMaxId() + 1);
		if (this.buffer.lineselectfrom == codepanel.firstline) {
			codepanel.lastline = this.buffer.lineselectto;
			codepanel.recheckMaxLineLength();
			codepanel.ensureCodeViewSize();
			this.activePanel = this.add(nextId, this.activePanel, 1, 0, 30,
				this.buffer.lineselectto,
				this.buffer.lines.size());
		} else {
			int initialLastline = codepanel.lastline;
			codepanel.lastline = this.buffer.lineselectfrom;
			codepanel.recheckMaxLineLength();
			codepanel.ensureCodeViewSize();
			if (this.buffer.lineselectto == initialLastline) {
				this.activePanel = this.add(nextId, this.activePanel, 1, 0, 30,
					this.buffer.lineselectfrom,
					initialLastline);
			} else {
				this.activePanel = this.add(nextId, this.activePanel, 1, 0, 30,
					this.buffer.lineselectfrom,
					this.buffer.lineselectto);
				nextId = Integer.valueOf(nextId.intValue() + 1);
				this.add(nextId, this.activePanel, 1, 0, 30,
					this.buffer.lineselectto,
					initialLastline);
			}
		}

		// Position the parent, since its max line length and thus size might have changed.
		this.position(codepanel);

		this.activePanel = this.panelAtLine(this.buffer.carety);
		if (this.activePanel != null) {
			SwingUtilities.invokeLater(this.activePanel::repaint);
		}
	}

	private CodePanel add(
		Integer id,
		CodePanel parent,
		int link,
		int posX,
		int posY,
		int linefrom,
		int lineto)
	{
		CodePanel cf = new CodePanel(this.jf, this, id, this.buffer, linefrom, lineto);
		this.panels.put(id, cf);
		cf.parent = parent;
		cf.link = link;
		cf.location.x = posX;
		cf.location.y = posY;
		cf.recheckMaxLineLength();
		this.jf.getContentPane().add(cf);
		return cf;
	}

	/**
	 * Call from codepanel when focus is gained.
	 *
	 * @return {@code false} if the request is denied
	 */
	public boolean focusGained(CodePanel panel)
	{
		if (this.jf.shouldBlockInput()) {
			return false;
		}
		if (this.activePanel == panel) {
			return true;
		}
		if (this.buffer.mode != EditBuffer.NORMAL_MODE) {
			return false;
		}
		CodePanel lastActivePanel = this.activePanel;
		this.activePanel = panel;
		if (lastActivePanel != null) {
			lastActivePanel.repaint(); // TODO should only be caret
		}
		return true;
	}

	public boolean hasFocus(CodePanel panel)
	{
		return panel == this.activePanel && !this.jf.shouldBlockInput();
	}

	public int findMaxId()
	{
		int max = 0;
		for (Integer i : this.panels.keySet()) {
			if (i.intValue() > max) {
				max = i.intValue();
			}
		}
		return max;
	}

	public CodePanel panelAtLine(int line)
	{
		for (CodePanel panel : this.panels.values()) {
			if (panel.firstline <= line && line < panel.lastline) {
				return panel;
			}
		}
		return null;
	}

	public void repaintAll()
	{
		for (CodePanel panel : this.panels.values()) {
			panel.repaint();
		}
	}

	/**
	 * To dispatch repaints after physical input is handled, because repaints might be needed
	 * if {@link #lineAdded} or {@link #lineRemoved} were invoked.
	 */
	public void dispatchInputEvent(KeyInput event, CodePanel to)
	{
		to.handleInput(event);
		// TODO: deal with panels that are empty now
		CodePanel newActivePanel = null;
		boolean needRepaintConnections = false;
		Iterator<CodePanel> iter = this.panels.values().iterator();
		while (iter.hasNext()) {
			CodePanel panel = iter.next();
			if (panel.firstline <= this.buffer.carety &&
				this.buffer.carety < panel.lastline)
			{
				newActivePanel = panel;
				if (event.needCheckLineLength || event.needEnsureViewSize) {
					panel.recheckMaxLineLength();
					panel.ensureCodeViewSize();
				}
			}
			if (panel.firstline == panel.lastline && panel != this.root) {
				this.jf.getContentPane().remove(panel);
				this.reparentChildren(panel, panel.parent);
				iter.remove();
				needRepaintConnections = true;
				continue;
			}
			if (panel.requireValidation) {
				panel.recheckMaxLineLength();
				panel.ensureCodeViewSize();
				this.position(panel);
			}
			if (panel.needRepaint) {
				panel.repaint();
			}
		}
		if (newActivePanel != null && newActivePanel != this.activePanel) {
			this.activePanel.repaint();
			this.activePanel = newActivePanel;
			this.activePanel.repaint();
		}
		if (needRepaintConnections) {
			this.jf.getGlassPane().repaint();
		}
	}

	private void reparentChildren(CodePanel parent, CodePanel newParent)
	{
		for (CodePanel child : this.panels.values()) {
			if (child.parent == parent) {
				child.parent = newParent;
			}
		}
		this.position(newParent);
	}

	public void beforeLineAdded(int idx)
	{
		for (CodePanel panel : this.panels.values()) {
			if (panel.firstline == panel.lastline) {
				// empty panels should be deleted,
				// but the root panel can't be deleted
				continue;
			}
			if (panel.firstline >= idx && idx != 0) {
				panel.firstline++;
				panel.requireValidation = true;
				panel.needRepaint = true;
			}
			if (panel.lastline >= idx) {
				panel.lastline++;
				panel.requireValidation = true;
				panel.needRepaint = true;
			}
			if (panel.parent != null && PanelLink.getAnchor(panel.link) == 'r') {
				int line = PanelLink.getLine(panel.link);
				if (line >= idx && line < panel.parent.lastline) {
					panel.link = PanelLink.createRightLink(line + 1);
					panel.requireValidation = true;
				}
			}
		}
	}

	public void beforeLineRemoved(int idx)
	{
		for (CodePanel panel : this.panels.values()) {
			if (panel.firstline > idx) {
				panel.firstline--;
				panel.lastline--;
				panel.requireValidation = true;
				panel.needRepaint = true;
			} else if (panel.lastline > idx && panel.firstline <= idx) {
				panel.lastline--;
				panel.requireValidation = true;
				panel.needRepaint = true;
			}
			if (panel.parent != null && PanelLink.getAnchor(panel.link) == 'r') {
				int line = PanelLink.getLine(panel.link);
				if (line == idx) {
					panel.link = PanelLink.TOP;
				} else if (line > idx && line < panel.parent.lastline) {
					panel.link = PanelLink.createRightLink(line - 1);
					panel.requireValidation = true;
				}
			}
		}
	}

	public void doUndo(Undo undo)
	{
		for (CodePanel panel : this.panels.values()) {
			if (panel.buffer == undo.buffer) {
				this.activePanel = panel;
				panel.handleInput(new KeyInput('u'));
				return;
			}
		}
		// TODO what now
		this.j.undolistptr--;
	}

	public void dispose()
	{
	}
}
