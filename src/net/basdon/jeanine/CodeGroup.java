package net.basdon.jeanine;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

	public void readFile(File file) throws IOException
	{
		this.title = file.getName();
		this.ownerFile = file;
		Iterator<String> iter = Files.readAllLines(file.toPath()).iterator();
		this.setContents(new Iterator<SB>()
		{
			private SB sb = new SB(4096);

			@Override
			public boolean hasNext()
			{
				return iter.hasNext();
			}

			@Override
			public SB next()
			{
				sb.length = 0;
				sb.append(iter.next());
				return sb;
			}
		}, true);
	}

	public void saveFile() throws IOException
	{
		if (this.ownerFile == null) {
			this.jf.setError("can't save - no file linked");
			return;
		}
		// TODO: check file modify time, to warn if we're overriding unknown changes
		GroupToRawConverter converter;
		converter = new GroupToRawConverter(this.buffer.lines.lines, this.panels, 0);
		// TODO: write to tmp file first to not lose data in case of error?
		try (FileOutputStream fos = new FileOutputStream(this.ownerFile, false)) {
			OutputStreamWriter writer;
			writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
			while (converter.hasNext()) {
				SB sb = converter.next();
				writer.write(sb.value, 0, sb.length);
				writer.write('\n');
			}
			writer.flush();
		}
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
		// TODO: show the errors if not empty
		SB errors = new SB();
		Integer id = Integer.valueOf(0);
		Integer nextInvalidId = Integer.valueOf(9000);
		int physicalLine = 0;
		int logicalLine = 0;
		// key:child, value:parentId
		HashMap<CodePanel, Integer> parents = new HashMap<>();
		// key:childId, value:lineNr
		HashMap<Integer, Integer> rightLinks = new HashMap<>();
		class ScndryLink
		{
			CodePanel parent;
			Integer childId;
			int link;
			int physicalLine; /*for error reporting usage only*/
		}
		ArrayList<ScndryLink> secondaryLinks = new ArrayList<>();
		CodePanel panel = this.root = new CodePanel(this, id, 0, 0);
		this.panels.put(id, this.root);
		while (lines.hasNext()) {
			physicalLine++;
			SB sb = lines.next();
			if (!sb.startsWith("/*jeanine:") || sb.length < 12) {
				int lineLength = sb.length, idx;
				// TODO goes wrong when there's both primary and secondary links
				// TODO even goes wrong when there's multiple secondary links
				if (sb.endsWith("*/") &&
					(idx = sb.lastIndexOf("/*jeanine:")) != -1 &&
					idx < sb.length - 12 &&
					sb.value[idx + 11] == ':')
				{
					if (sb.value[idx + 10] == 'l') {
						// normal right link
						Integer line = Integer.valueOf(logicalLine);
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
					} else if (sb.value[idx + 10] == 's') {
						// secondary right link
						JeanineProperties props;
						props = JeanineProperties.parse(sb, idx + 12);
						ScndryLink scnd = new ScndryLink();
						scnd.parent = panel;
						scnd.physicalLine = physicalLine;
						// this can only be right link, because not in the beginning of a line
						scnd.link = PanelLink.createRightLink(logicalLine);
						// i: child id
						if (props.isValidInt['i']) {
							scnd.childId = Integer.valueOf(props.intValue['i']);
							secondaryLinks.add(scnd);
						} else {
							errors.append(physicalLine + ": missing id in props").lf();
						}
					} else {
						errors.append(physicalLine + ": unk directive");
					}
					lineLength = idx;
				}
				sb = new SB(sb.value, 0, lineLength);
				this.buffer.lines.lines.add(sb);
				panel.lastline++;
				logicalLine++;
			} else if (sb.value[10] == 'p') {
				// new panel definition
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
					errors.append(physicalLine + ": bad anchor").lf();
				}
			} else if (sb.value[10] == 's') {
				// secondary link
				JeanineProperties props = JeanineProperties.parse(sb, 12);
				ScndryLink scnd = new ScndryLink();
				scnd.parent = panel;
				scnd.physicalLine = physicalLine;
				// c: child id
				if (props.isValidInt['i']) {
					scnd.childId = Integer.valueOf(props.intValue['i']);
					secondaryLinks.add(scnd);
				} else {
					errors.append(physicalLine + ": missing id in props").lf();
				}
				// a: anchor
				if (props.isPresent['a'] && props.strValue['a'].length() == 1) {
					switch (props.strValue['a'].charAt(0)) {
					default: errors.append(physicalLine + ": bad anchor").lf();
					case 't': scnd.link = PanelLink.TOP; break;
					case 'b': scnd.link = PanelLink.BOTTOM; break;
					case 'r':
						scnd.link = PanelLink.createRightLink(physicalLine);
						break;
					}
				} else {
					scnd.link = PanelLink.TOP;
					errors.append(physicalLine + ": bad anchor").lf();
				}
			} else {
				errors.append("unk directive at line ").append(physicalLine);
				errors.append(": ").append(sb).lf();
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
		// ensure no cyclic dependencies (only applies to primary links)
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
		// apply secondary links
		for (ScndryLink s : secondaryLinks) {
			CodePanel child = this.panels.get(s.childId);
			if (child != null) {
				s.parent.secondaryLinks.add(new SecondaryLink(child, s.link));
			} else {
				errors.append("can't find child for link at line ");
				errors.append(s.physicalLine).append(", dropping").lf();
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

	public void getBounds(Rectangle rv)
	{
		if (this.panels.isEmpty()) {
			rv.x = this.location.x;
			rv.y = this.location.y;
			rv.width = 0;
			rv.height = 0;
			return;
		}
		rv.x = Integer.MAX_VALUE;
		rv.y = Integer.MAX_VALUE;
		rv.width = Integer.MIN_VALUE;
		rv.height = Integer.MIN_VALUE;
		Rectangle rect = new Rectangle();
		for (CodePanel panel : this.panels.values()) {
			panel.getBounds(rect);
			rv.x = Math.min(rv.x, rect.x);
			rv.y = Math.min(rv.y, rect.y);
			rv.width = Math.max(rv.width, rect.x + rect.width);
			rv.height = Math.max(rv.height, rect.y + rect.height);
		}
		rv.width -= rv.x;
		rv.height -= rv.y;
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

		CodePanel a = this.activePanel, b, c, newBotPanel;
		if (this.buffer.lineselectfrom == a.firstline &&
			this.buffer.lineselectto == a.lastline)
		{
			this.jf.setError("can't split, no lines left");
			return;
		}

		this.buffer.mode = EditBuffer.NORMAL_MODE;

		int from, to;
		int initialLastline = a.lastline;
		Integer nextId = Integer.valueOf(this.findMaxId() + 1);
		if (this.buffer.lineselectfrom == a.firstline) {
			a.lastline = this.buffer.lineselectto;
			a.recheckMaxLineLength();
			a.ensureCodeViewSize();
			from = this.buffer.lineselectto;
			to = initialLastline;
			b = this.add(nextId, a, 1, 0, 30, from, to);
			newBotPanel = b;
		} else {
			a.lastline = this.buffer.lineselectfrom;
			a.recheckMaxLineLength();
			a.ensureCodeViewSize();
			if (this.buffer.lineselectto == initialLastline) {
				from = this.buffer.lineselectfrom; to = initialLastline;
				b = this.add(nextId, a, 1, 0, 30, from, to);
				newBotPanel = b;
			} else {
				from = this.buffer.lineselectfrom; to = this.buffer.lineselectto;
				b = this.add(nextId, a, 1, 0, 30, from, to);
				nextId = Integer.valueOf(nextId.intValue() + 1);
				from = to;
				to = initialLastline;
				c = this.add(nextId, b, 1, 0, 30, from, to);
				newBotPanel = c;
			}
		}

		this.activePanel = b;
		// Patch bot linked panels so they're bot linked to the new panel.
		for (CodePanel pnl : this.panels.values()) {
			if (pnl.parent == a && pnl != b && pnl.link == PanelLink.BOTTOM) {
				pnl.parent = newBotPanel;
			}
		}
		for (Iterator<SecondaryLink> iter = a.secondaryLinks.iterator(); iter.hasNext();) {
			SecondaryLink slink = iter.next();
			if (slink.link == PanelLink.BOTTOM) {
				newBotPanel.secondaryLinks.add(slink);
				iter.remove();
			}
		}

		// Patch panels that were right linked but their parent changed and need update.
		for (CodePanel pnl : this.panels.values()) {
			if (pnl.parent == a && PanelLink.getAnchor(pnl.link) == 'r') {
				pnl.parent = this.panelAtLine(PanelLink.getLine(pnl.link));
			}
		}
		for (Iterator<SecondaryLink> iter = a.secondaryLinks.iterator(); iter.hasNext();) {
			SecondaryLink slink = iter.next();
			if (PanelLink.getAnchor(slink.link) == 'r') {
				CodePanel np = this.panelAtLine(PanelLink.getLine(slink.link));
				if (np != a) {
					np.secondaryLinks.add(slink);
					iter.remove();
				}
			}
		}

		// Position the parent, since its max line length and thus size might have changed.
		this.position(a);

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
		if (!this.jf.focusGained(this)) {
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
		return this.jf.activeGroup == this &&
			panel == this.activePanel &&
			!this.jf.shouldBlockInput();
	}

	public boolean shouldDrawCaret(CodePanel panel)
	{
		return (this.buffer.mode != EditBuffer.NORMAL_MODE) ||
			this.jf.activeGroup == this &&
			panel == this.activePanel &&
			this.jf.shouldDrawCaret();
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
			Iterator<SecondaryLink> slinks = child.secondaryLinks.iterator();
			while (slinks.hasNext()) {
				if (slinks.next().child == parent) {
					slinks.remove();
				}
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
			for (int i = 0; i < panel.secondaryLinks.size(); i++) {
				SecondaryLink slink = panel.secondaryLinks.get(i);
				if (PanelLink.getAnchor(slink.link) == 'r') {
					int line = PanelLink.getLine(slink.link);
					if (line >= idx && line < slink.child.lastline) {
						slink.link = PanelLink.createRightLink(line + 1);
					}
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
			for (int i = 0; i < panel.secondaryLinks.size(); i++) {
				SecondaryLink slink = panel.secondaryLinks.get(i);
				if (PanelLink.getAnchor(slink.link) == 'r') {
					int line = PanelLink.getLine(slink.link);
					if (line == idx) {
						slink.link = PanelLink.TOP;
					} else if (line > idx && line < slink.child.lastline) {
						slink.link = PanelLink.createRightLink(line - 1);
					}
				}
			}
		}
	}

	public void toggleRaw()
	{
		Point rootLocation = this.root.location;
		ArrayList<SB> lines = new ArrayList<>(this.buffer.lines.lines);
		HashMap<Integer, CodePanel> panels = new HashMap<>(this.panels);
		int caretx = this.buffer.caretx;
		int carety = this.buffer.carety;
		Point cursorPos = this.jf.findCursorPosition();
		if (this.raw) {
			this.setContents(lines.iterator(), true);
			for (CodePanel panel : this.panels.values()) {
				if (panel.parent != null && panel.firstline < carety) {
					carety--;
				}
			}
		} else {
			GroupToRawConverter conv = new GroupToRawConverter(lines, panels, carety);
			this.setContents(conv, false);
			carety = conv.newCarety;
		}
		this.buffer.carety = carety;
		this.buffer.caretx = caretx;
		this.buffer.virtualCaretx = caretx;
		this.activePanel = this.panelAtLine(this.buffer.carety);
		this.jf.moveToGetCursorAtPosition(cursorPos);
		this.raw = !this.raw;
		this.root.location = rootLocation;
		this.position(this.root);
	}

	public void reChild(CodePanel child, String position)
	{
		if (child == this.activePanel || child.isEventualParentOf(this.activePanel)) {
			this.jf.setError("can't rechild, cyclic dependency");
			return;
		}
		child.parent = this.activePanel;
		switch (position) {
		case "bot":
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

	public void slink(CodePanel child, String position)
	{
		// skip cyclic dependency checks for secondary links
		int link;
		switch (position) {
		default: link = PanelLink.BOTTOM; break;
		case "right": link = PanelLink.createRightLink(this.buffer.carety); break;
		case "top": link = PanelLink.TOP; break;
		}
		for (SecondaryLink slink : this.activePanel.secondaryLinks) {
			if (slink.child == child && slink.link == link) {
				// an identical link already exists, return without error
				return;
			}
		}
		this.activePanel.secondaryLinks.add(new SecondaryLink(child, link));
	}

	public void unlink(CodePanel child, String position)
	{
		int link;
		switch (position) {
		default: link = PanelLink.BOTTOM; break;
		case "right": link = PanelLink.createRightLink(this.buffer.carety); break;
		case "top": link = PanelLink.TOP; break;
		}
		Iterator<SecondaryLink> iter = this.activePanel.secondaryLinks.iterator();
		while (iter.hasNext()) {
			SecondaryLink slink = iter.next();
			if (slink.child == child && slink.link == link) {
				iter.remove();
				return;
			}
		}
		this.jf.setError("can't unlink, specified link doesn't exist");
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
		for (CodePanel panel : this.panels.values()) {
			this.jf.getContentPane().remove(panel);
		}
	}
}
