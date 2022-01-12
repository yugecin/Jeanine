package net.basdon.jeanine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class RawToGroupConverter
{
	private final Jeanine j;
	private final CodeGroup group;

	private Integer nextInvalidId;
	private int physicalLine;
	private CodePanel panel;
	private int logicalLine = 0;
	/**key:child, value:parentId*/
	private HashMap<CodePanel, Integer> parents;
	private ArrayList<RightLink> rightLinks;
	private ArrayList<ScndryLink> secondaryLinks;

	public SB errors;
	public CodePanel root;
	public HashMap<Integer, CodePanel> panels;
	public ArrayList<SB> lines;

	public RawToGroupConverter(Jeanine j, CodeGroup group)
	{
		this.j = j;
		this.group = group;
		this.errors = new SB();
		this.panels = new HashMap<>();
		this.parents = new HashMap<>();
		this.rightLinks = new ArrayList<>();
		this.secondaryLinks = new ArrayList<>();
		this.lines = new ArrayList<>();
		this.nextInvalidId = Integer.valueOf(9000);
	}

	private void handlePanelDirective(JeanineDirective dir)
	{
		SB e = this.errors;
		Integer id = this.nextInvalidId;
		if (dir.isValidInt['i']) {
			id = Integer.valueOf(dir.intValue['i']);
			if (this.panels.containsKey(id)) {
				e.append(this.physicalLine).append(": duplicate panel id\n");
				id = this.nextInvalidId;
			}
		} else {
			e.append(this.physicalLine).append(": missing panel id in props\n");
		}
		int line = this.panel.lastline;
		this.panel = new CodePanel(this.group, id, line, line);
		this.panels.put(id, this.panel);
		this.nextInvalidId = Integer.valueOf(this.nextInvalidId.intValue() + 1);
		// p: parent
		if (dir.isValidInt['p']) {
			this.parents.put(this.panel, Integer.valueOf(dir.intValue['p']));
		} else {
			this.panel.parent = this.root;
			e.append(this.physicalLine).append(": no parent panel id in props\n");
		}
		// x: x pos in pixels or multiple of font width
		if (dir.isValidInt['x']) {
			this.panel.location.x = dir.intValue['x'] / (float) this.j.fx;
		} else if (dir.isValidFloat['x']) {
			this.panel.location.x = dir.floatValue['x'];
		}
		// y: y pos in pixels or multiple of font height
		if (dir.isValidInt['y']) {
			this.panel.location.y = dir.intValue['y'] / (float) this.j.fy;
		} else if (dir.isValidFloat['y']) {
			this.panel.location.y = dir.floatValue['y'];
		}
		// a: anchor
		if (dir.strLength['a'] == 1) {
			switch (dir.strValue['a'].charAt(0)) {
			case 't': this.panel.link = PanelLink.TOP; return;
			case 'b': this.panel.link = PanelLink.BOTTOM; return;
			case 'r': this.panel.link = PanelLink.INVALID_RIGHT; return;
			}
		}
		this.panel.link = PanelLink.TOP;
		e.append(this.physicalLine).append(": bad panel anchor, using top\n");
	}

	// TODO remove legacy right links
	private void handleLegacyRightLinkDirective(JeanineDirective dir, SB sb, int idx)
	{
		if (idx >= sb.length) {
			return;
		}
		int childId = 0;
		char c = 0;
		for (;;) {
			if (idx >= sb.length || (c = sb.value[idx++]) == ',' || c == '*') {
				RightLink rl = new RightLink();
				rl.childId = Integer.valueOf(childId);
				rl.logicalLine = this.logicalLine;
				rl.physicalLine = this.physicalLine;
				this.rightLinks.add(rl);
				if (idx >= sb.length || c != ',') {
					break;
				}
			} else if ('0' <= c && c <= '9') {
				childId = childId * 10 + c - '0';
			} else {
				break;
			}
		}
	}

	public void handleRightLinkDirective(JeanineDirective dir)
	{
		SB e = this.errors;
		// i: child id
		if (dir.isValidInt['i']) {
			RightLink rl = new RightLink();
			rl.childId = Integer.valueOf(dir.intValue['i']);
			rl.logicalLine = this.logicalLine;
			rl.physicalLine = this.physicalLine;
			this.rightLinks.add(rl);
		} else {
			e.append(this.physicalLine);
			e.append(": primary right link missing id in props\n");
		}
	}

	/**
	 * @return the link
	 */
	public int handleSecondaryLinkDirective(JeanineDirective dir)
	{
		SB e = this.errors;
		ScndryLink scnd = new ScndryLink();
		scnd.owner = this.panel;
		scnd.physicalLine = this.physicalLine;
		// i: child id
		if (!dir.isValidInt['i']) {
			e.append(this.physicalLine);
			e.append(": secondary link missing id in props\n");
			return PanelLink.INVALID_RIGHT;
		}
		scnd.childId = Integer.valueOf(dir.intValue['i']);
		this.secondaryLinks.add(scnd);
		// a: anchor
		if (dir.strLength['a'] == 1) {
			switch (dir.strValue['a'].charAt(0)) {
			case 't': scnd.link = PanelLink.TOP; return scnd.link;
			case 'b': scnd.link = PanelLink.BOTTOM; return scnd.link;
			case 'r':
				scnd.link = PanelLink.createRightLink(this.logicalLine);
				return scnd.link;
			}
		}
		scnd.link = PanelLink.TOP;
		e.append(this.physicalLine).append(": bad secondary link anchor, using top\n");
		return scnd.link;
	}

	public void interpretSource(Iterator<SB> lines)
	{
		SB e = this.errors;
		this.panel = this.root = new CodePanel(this.group, Integer.valueOf(0), 0, 0);
		this.panels.put(this.root.id, this.root);
		while (lines.hasNext()) {
			this.physicalLine++;
			SB sb = lines.next();
			int lineLength = sb.length;
			boolean isLogicalLine = true;
			int idx = sb.lastIndexOf("/*jeanine:");
			if (idx != -1) {
				lineLength = idx;
				idx += 10;
			}
			JeanineDirective dir;
			while (idx != -1) {
				dir = JeanineDirective.parse(sb, idx);
				// TODO remove legacy right links
				if (dir.directive == 'l') {
					// legacy right links have unexpected syntax, ignore errors
					dir.errors.length = 0;
				}
				if (dir.errors.length != 0) {
					e.append(this.physicalLine).append(
						": while parsing jeanine comment" +
						" directives:\n"
					).append(dir.errors);
				}
				switch (dir.directive) {
				case 'l':
					// TODO remove legacy right links
					this.handleLegacyRightLinkDirective(dir, sb, idx + 2);
					break;
				case 'p':
					this.handlePanelDirective(dir);
					// panel directives are defined on their own line
					isLogicalLine = false;
					break;
				case 'r':
					this.handleRightLinkDirective(dir);
					break;
				case 's':
					int link = this.handleSecondaryLinkDirective(dir);
					// top/bot secondary links are defined on their own line
					if (link == PanelLink.BOTTOM || link == PanelLink.TOP) {
						isLogicalLine = false;
					}
					break;
				default:
					e.append(this.physicalLine).append(
						": unknown directive: '" +
						dir.directive + "', skipping\n"
					);
					break;
				}
				idx = dir.nextDirectiveOffset;
			}
			if (isLogicalLine) {
				this.lines.add(new SB(sb.value, 0, lineLength));
				this.panel.lastline++;
				this.logicalLine++;
			}
		}
		// apply right links that were found (incoming primary links)
		for (RightLink rl : this.rightLinks) {
			CodePanel child = this.panels.get(rl.childId);
			if (child == null) {
				e.append(rl.physicalLine);
				e.append(": primary right link to nonexisting panel :");
				e.append(rl.childId.intValue()).lf();
				continue;
			}
			if (child == this.root) {
				// not allowed to be root, because root cannot have a parent
				e.append(rl.physicalLine);
				e.append(": primary right link from root panel, ignoring\n");
				continue;
			}
			char anchor = PanelLink.getAnchor(child.link);
			if (anchor != 'r') {
				e.append(rl.physicalLine);
				e.append(": primary right link found for panel ");
				e.append(child.id.intValue());
				e.append(" with non-right anchor: ");
				e.append(anchor);
				e.append(", overriding anchor to right\n");
			}
			if (child.link != PanelLink.INVALID_RIGHT) {
				e.append(rl.physicalLine);
				e.append(": overriding existing right link for panel ");
				e.append(child.id.intValue()).lf();
			}
			child.link = PanelLink.createRightLink(rl.logicalLine);
			// check if the logical line is in the range of the parent panel
		}
		int invalidLinkedPanels = 0;
		// reset right linked panels that didn't have an associated right link
		for (CodePanel pnl : this.panels.values()) {
			if (pnl.link == PanelLink.INVALID_RIGHT) {
				e.append("panel with id ");
				e.append(pnl.id.intValue());
				e.append(" has right anchor but no associated right link ");
				e.append("directive found, changing to top anchor\n");
				pnl.link = PanelLink.TOP;
				pnl.location.x = ++invalidLinkedPanels * 20.0f / this.j.fx;
				pnl.location.y = -100.0f / this.j.fy;
			}
		}
		// apply parent links
		for (Map.Entry<CodePanel, Integer> parentLink : this.parents.entrySet()) {
			CodePanel child = parentLink.getKey();
			Integer parentId = parentLink.getValue();
			child.parent = this.panels.get(parentId);
			if (child.parent == null) {
				e.append("parent panel ").append(parentId.intValue());
				e.append(" not found for panel ").append(child.id.intValue());
				e.append(", reparenting to root panel\n");
				child.parent = this.root;
				child.link = PanelLink.TOP;
				child.location.x = ++invalidLinkedPanels * 20.0f / this.j.fx;
				child.location.y = -100.0f / this.j.fy;
			}
		}
		// reparent right linked parents where the line linked is not in the parent
		for (CodePanel pnl : this.panels.values()) {
			if (pnl == this.root || PanelLink.getAnchor(pnl.link) != 'r') {
				continue; // not right link, ok
			}
			int line = PanelLink.getLine(pnl.link);
			if (pnl.parent.firstline <= line && line < pnl.parent.lastline) {
				continue; // ok
			}
			e.append("panel ").append(pnl.id.intValue());
			e.append(" is right linked but linked line does not belong to parent ");
			e.append(pnl.parent.id.intValue()).append(", reparenting\n");
			// find the correct parent
			pnl.parent = null;
			for (CodePanel p : this.panels.values()) {
				if (p.firstline <= line && line <= p.lastline) {
					pnl.parent = p;
					break;
				}
			}
			if (pnl.parent == pnl || pnl.parent == null) {
				e.append(" new parent is self or not found(??), " +
					"reparenting to root panel\n");
				pnl.parent = this.root;
				pnl.link = PanelLink.TOP;
				pnl.location.x = ++invalidLinkedPanels * 20.0f / this.j.fx;
				pnl.location.y = -100.0f / this.j.fy;
			}
		}
		// ensure no cyclic dependencies (only applies to primary links)
		for (CodePanel pnl : this.panels.values()) {
			HashSet<CodePanel> seen = new HashSet<>();
			seen.add(pnl);
			while (pnl != null) {
				pnl = pnl.parent;
				if (!seen.add(pnl)) {
					e.append("cyclic dependency for panel ");
					e.append(pnl.id.intValue());
					e.append(", reparenting to root panel\n");
					pnl.parent = this.root;
					break;
				}
			}
		}
		// apply secondary links
		for (ScndryLink s : this.secondaryLinks) {
			CodePanel child = this.panels.get(s.childId);
			if (child != null) {
				s.owner.secondaryLinks.add(new SecondaryLink(child, s.link));
			} else {
				e.append("can't find child for link at line ");
				e.append(s.physicalLine).append(", dropping\n");
			}
		}
	}

	private static class RightLink
	{
		Integer childId;
		int logicalLine;
		/**for error reporting usage only*/
		int physicalLine;
	}

	private static class ScndryLink
	{
		CodePanel owner;
		Integer childId;
		int link;
		/**for error reporting usage only*/
		int physicalLine;
	}
}
