package net.basdon.jeanine;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

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
	public boolean raw;

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

	public void setContents(Iterator<SB> lines, boolean interpret)
	{
		this.buffer.carety = 0;
		this.buffer.caretx = 0;
		this.buffer.virtualCaretx = 0;
		this.buffer.lines.clear();
		for (CodePanel panel : this.panels.values()) {
			this.jf.getContentPane().remove(panel);
		}
		this.panels.clear();

		if (interpret) {
			this.interpretSource(lines);
		} else {
			Integer id = Integer.valueOf(0);
			this.root = new CodePanel(this, id, 0, 0);
			this.panels.put(id, this.root);
			while (lines.hasNext()) {
				this.buffer.lines.lines.add(new SB(lines.next()));
				this.root.lastline++;
			}
		}

		this.activePanel = this.root;
		for (CodePanel pnl : this.panels.values()) {
			pnl.recheckMaxLineLength();
			pnl.ensureCodeViewSize(); // TODO: this should not repaint stuff etc
		}
		this.position(this.root);
		for (CodePanel pnl : this.panels.values()) {
			this.jf.getContentPane().add(pnl);
		}
	}

	private SB interpretSource(Iterator<SB> lines)
	{
		SB errors = new SB();
		Integer id = Integer.valueOf(0);
		Integer nextInvalidId = Integer.valueOf(9000);
		int physicalLine = 0;
		int parsedLine = 0;
		HashMap<CodePanel, Integer> parents = new HashMap<>();
		HashMap<Integer, Integer> rightLinks = new HashMap<>();
		CodePanel panel = this.root = new CodePanel(this, id, 0, 0);
		this.panels.put(id, this.root);
		while (lines.hasNext()) {
			physicalLine++;
			SB sb = lines.next();
			if (sb.startsWith("/*jeanine:p:")) {
				JeanineProperties props = JeanineProperties.parse(sb, 12);
				id = nextInvalidId;
				if (props.isValidInt['i']) {
					id = Integer.valueOf(props.intValue['i']);
					if (this.panels.containsKey(id)) {
						errors.append(physicalLine + ": duplicate id").lf();
						id = nextInvalidId;
					}
				} else {
					errors.append(physicalLine + ": missing id in props").lf();
				}
				panel = new CodePanel(this, id, panel.lastline, panel.lastline);
				this.panels.put(id, panel);
				nextInvalidId = Integer.valueOf(nextInvalidId.intValue() + 1);
				if (props.isValidInt['p']) {
					parents.put(panel, Integer.valueOf(props.intValue['p']));
				} else {
					parents.put(panel, Integer.valueOf(0));
					errors.append(physicalLine + ": no parent in props").lf();
				}
				if (props.isValidInt['x']) {
					panel.location.x = props.intValue['x'];
				}
				if (props.isValidInt['y']) {
					panel.location.y = props.intValue['y'];
				}
				if (props.isPresent['a'] && props.strValue['a'].length() == 1) {
					switch (props.strValue['a'].charAt(0)) {
					default: errors.append(physicalLine + ": bad anchor").lf();
					case 't': panel.link = PanelLink.TOP; break;
					case 'b': panel.link = PanelLink.BOTTOM; break;
					case 'r': panel.link = PanelLink.INVALID_RIGHT; break;
					}
				} else {
					panel.link = PanelLink.TOP;
				}
			} else {
				int lineLength = sb.length;
				if (sb.endsWith("*/")) {
					int idx = sb.lastIndexOf("/*jeanine:l:");
					if (idx != -1) {
						Integer line = Integer.valueOf(parsedLine);
						int from = idx + 12, len = sb.length - 2 - from;
						String links = new String(sb.value, from, len);
						for (String link : links.split(",")) {
							int child;
							try {
								child = Integer.parseInt(link);
							} catch (Exception e) {
								errors.append(physicalLine + ":");
								errors.append(" bad link id").lf();
								continue;
							}
							Integer Child = Integer.valueOf(child);
							rightLinks.put(Child, line);
						}
						lineLength = idx;
					}
				}
				sb = new SB(sb.value, 0, lineLength);
				this.buffer.lines.lines.add(sb);
				panel.lastline++;
				parsedLine++;
			}
		}
		// apply right links that were found
		for (Map.Entry<Integer, Integer> entry : rightLinks.entrySet()) {
			Integer child = entry.getKey();
			Integer line = entry.getValue();
			panel = this.panels.get(child);
			if (panel == null) {
				errors.append("bad link to nonexisting panel " + child).lf();
			} else {
				if (PanelLink.getAnchor(panel.link) != 'r') {
					errors.append("overriding non-right link for panel ");
					errors.append(child.toString()).lf();
				} else if (panel.link != PanelLink.INVALID_RIGHT) {
					errors.append("overriding existing right link for panel ");
					errors.append(child.toString()).lf();
				}
				panel.link = PanelLink.createRightLink(line.intValue());
			}
		}
		int invalids = 0;
		// reset invalid right links
		for (CodePanel pnl : this.panels.values()) {
			if (pnl.link == PanelLink.INVALID_RIGHT) {
				errors.append(pnl.id + " has right anchor but no link");
				errors.lf();
				pnl.link = PanelLink.TOP;
				pnl.location.x = ++invalids * 20;
				pnl.location.y = -100;
			}
		}
		// apply parent links
		for (Map.Entry<CodePanel, Integer> parentLink : parents.entrySet()) {
			CodePanel child = parentLink.getKey();
			Integer parentId = parentLink.getValue();
			child.parent = this.panels.get(parentId);
			if (child.parent == null) {
				errors.append("parent " + parentId + " not found for " + child.id);
				errors.lf();
				child.parent = this.root;
				child.link = PanelLink.TOP;
				child.location.x = ++invalids * 20;
				child.location.y = -100;
			}
		}
		// ensure no cyclic dependencies
		for (CodePanel pnl : this.panels.values()) {
			HashSet<CodePanel> seen = new HashSet<>();
			seen.add(pnl);
			while (pnl != null) {
				pnl = pnl.parent;
				if (!seen.add(pnl)) {
					errors.append("cyclic dependency for " + pnl.id);
					errors.lf();
					pnl.parent = this.root;
					break;
				}
			}
		}
		return errors;
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
		if (this.raw) {
			this.jf.setError("can't split while in raw mode");
			return;
		}

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
		CodePanel cf = new CodePanel(this, id, linefrom, lineto);
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

	public void toggleRaw()
	{
		Point rootLocation = this.root.location;
		ArrayList<SB> lines = new ArrayList<>(this.buffer.lines.lines);
		HashMap<Integer, CodePanel> panels = new HashMap<>(this.panels);
		if (this.raw) {
			this.setContents(lines.iterator(), true);
		} else {
			this.setContents(new GroupToRawConverter(lines, panels), false);
		}
		this.raw = !this.raw;
		this.root.location = rootLocation;
		this.position(this.root);
	}

	public void reChild(Integer childId, String position)
	{
		if (this.activePanel == null) {
			this.jf.setError("can't rechild, no active panel");
			return;
		}
		CodePanel child = this.panels.get(childId);
		if (child == null) {
			this.jf.setError("can't rechild, unknown child");
			return;
		}
		if (child == this.activePanel || child.isEventualParentOf(this.activePanel)) {
			this.jf.setError("can't rechild, cyclic dependency");
			return;
		}
		child.parent = this.activePanel;
		switch (position) {
		case "bottom":
			child.link = PanelLink.BOTTOM;
			child.location.x = 0;
			child.location.y = 30;
			break;
		case "right":
			child.link = PanelLink.createRightLink(this.buffer.carety);
			child.location.x = 30;
			child.location.y = 0;
			break;
		case "top":
			child.link = PanelLink.TOP;
			child.location.x = 30;
			child.location.y = 0;
			break;
		}
		this.position(child);
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
