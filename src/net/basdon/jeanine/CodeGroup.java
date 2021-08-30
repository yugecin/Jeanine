package net.basdon.jeanine;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.SwingUtilities;

public class CodeGroup
{
	public final JeanineFrame jf;
	public final EditBuffer buffer;
	public final HashMap<Integer, CodePanel> panels;
	public final Point location;

	public CodePanel rootFrame;

	public File ownerFile;
	public CodePanel activePanel;

	public CodeGroup(JeanineFrame jf)
	{
		this.panels = new HashMap<>();
		this.buffer = new EditBuffer(jf.j, this);
		this.jf = jf;
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
		this.rootFrame = new CodePanel(this.jf, this, id, this.buffer, 0, this.buffer.lines.size());
		this.panels.put(id, this.rootFrame);
		this.position(this.rootFrame);
		this.jf.add(this.rootFrame);
		this.rootFrame.setVisible(true);
	}

	public void setLocation(int x, int y)
	{
		this.location.x = x;
		this.location.y = y;
		if (this.rootFrame != null) {
			this.position(this.rootFrame);
		}
	}

	public void position(CodePanel panel)
	{
		int x = this.location.x + this.jf.location.x;
		int y = this.location.y + this.jf.location.y;
		if (panel.parent != null) {
			CodePanel parent = this.panels.get(panel.parent.id);
			if (parent != null) {
				Rectangle bounds = parent.getBounds();
				switch (panel.anchor) {
				case 't':
					break;
				case 'r':
					break;
				case 'b':
					x = bounds.x;
					y = bounds.y + bounds.height;
					break;
				case 'l':
					break;
				}
			}
		}
		x += panel.location.x;
		y += panel.location.y;
		panel.setLocation(x, y);
		panel.requirePositionSizeValidation = false;
		this.framePositionChanged(panel);
	}

	public void framePositionChanged(CodePanel frame)
	{
		for (CodePanel child : this.panels.values()) {
			if (child.parent == frame) {
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
		if (this.frameAtLine(this.buffer.lineselectfrom) !=
			this.frameAtLine(this.buffer.lineselectto - 1))
		{
			this.jf.setError("can't split, selection spans multiple views");
			return;
		}
		if (this.buffer.lineselectfrom == 0 &&
			this.buffer.lineselectto == this.buffer.lines.size())
		{
			this.jf.setError("can't split, no lines left");
			return;
		}

		CodePanel codepanel = this.activePanel;
		this.buffer.mode = EditBuffer.NORMAL_MODE;

		Integer nextId = Integer.valueOf(this.findMaxId() + 1);
		if (this.buffer.lineselectfrom == codepanel.firstline) {
			codepanel.lastline = this.buffer.lineselectto;
			codepanel.recheckMaxLineLength();
			codepanel.ensureCodeViewSize();
			this.activePanel = this.add(nextId, this.activePanel, 'b', 0, 10,
				this.buffer.lineselectto,
				this.buffer.lines.size());
		} else {
			int initialLastline = codepanel.lastline;
			codepanel.lastline = this.buffer.lineselectfrom;
			codepanel.recheckMaxLineLength();
			codepanel.ensureCodeViewSize();
			if (this.buffer.lineselectto == initialLastline) {
				this.activePanel = this.add(nextId, this.activePanel, 'b', 0, 10,
					this.buffer.lineselectfrom,
					initialLastline);
			} else {
				this.activePanel = this.add(nextId, this.activePanel, 'b', 0, 10,
					this.buffer.lineselectfrom,
					this.buffer.lineselectto);
				nextId = Integer.valueOf(nextId.intValue() + 1);
				this.add(nextId, this.activePanel, 'b', 0, 10,
					this.buffer.lineselectto,
					initialLastline);
			}
		}

		this.activePanel = this.frameAtLine(this.buffer.carety);
		if (this.activePanel != null) {
			SwingUtilities.invokeLater(() -> {
				this.activePanel.requestFocusInWindow();
				this.activePanel.repaint();
			});
		}
	}

	private CodePanel add(
		Integer id,
		CodePanel parent,
		char anchor,
		int posX,
		int posY,
		int linefrom,
		int lineto)
	{
		CodePanel cf = new CodePanel(this.jf, this, id, this.buffer, linefrom, lineto);
		this.panels.put(id, cf);
		cf.parent = parent;
		cf.anchor = (byte) anchor;
		cf.location.x = posX;
		cf.location.y = posY;
		cf.recheckMaxLineLength();
		cf.ensureCodeViewSize();
		this.position(cf);
		cf.setVisible(true);
		this.jf.add(cf);
		return cf;
	}

	public void requestFocusInWindow()
	{
		if (this.activePanel != null) {
			this.activePanel.requestFocusInWindow();
		} else {
			Iterator<CodePanel> iter = this.panels.values().iterator();
			if (iter.hasNext()) {
				(this.activePanel = iter.next()).requestFocusInWindow();
			}
		}
		this.jf.activeGroup = this;
	}

	/**
	 * Call from codepanel when focus is gained.
	 *
	 * @return {@code false} if the request is denied
	 */
	public boolean focusGained(CodePanel panel)
	{
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
		return panel == this.activePanel;
	}

	public void mouseDragged(int dX, int dY)
	{
		Point loc = null;
		for (CodePanel frame : this.panels.values()) {
			loc = frame.getLocation(loc);
			frame.setLocation(loc.x + dX, loc.y + dY);
		}
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

	public CodePanel frameAtLine(int line)
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
		for (CodePanel frame : this.panels.values()) {
			frame.repaint();
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
		for (CodePanel panel : this.panels.values()) {
			if (panel.firstline <= this.buffer.carety &&
				this.buffer.carety < panel.lastline)
			{
				newActivePanel = panel;
				if (event.needCheckLineLength || event.needEnsureViewSize) {
					panel.recheckMaxLineLength();
					panel.ensureCodeViewSize();
				}
			}
			if (panel.requirePositionSizeValidation) {
				panel.recheckMaxLineLength();
				panel.ensureCodeViewSize();
				this.position(panel);
			}
		}
		if (newActivePanel != null && newActivePanel != this.activePanel) {
			this.activePanel.repaint();
			this.activePanel = newActivePanel;
			this.activePanel.requestFocusInWindow();
			this.activePanel.repaint();
		}
	}

	public void beforeLineAdded(int idx)
	{
		for (CodePanel frame : this.panels.values()) {
			if (frame.firstline >= idx && idx != 0) {
				frame.firstline++;
				frame.requirePositionSizeValidation = true;
			}
			if (frame.lastline >= idx) {
				frame.lastline++;
				frame.requirePositionSizeValidation = true;
			}
		}
	}

	public void beforeLineRemoved(int idx)
	{
		for (CodePanel panel : this.panels.values()) {
			if (panel.lastline >= idx) {
				panel.lastline--;
				panel.requirePositionSizeValidation = true;
			}
			if (panel.firstline > idx) {
				panel.firstline--;
				panel.requirePositionSizeValidation = true;
			}
		}
	}

	public void doUndo(Undo undo)
	{
		for (CodePanel panel : this.panels.values()) {
			if (panel.buffer == undo.buffer) {
				this.activePanel = panel;
				panel.requestFocusInWindow();
				panel.handleInput(new KeyInput('u'));
				return;
			}
		}
		// TODO what now
		this.jf.j.undolistptr--;
	}

	public void dispose()
	{
	}
}
