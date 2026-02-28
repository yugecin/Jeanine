package net.basdon.jeanine;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class DialogBufferlist extends JeanineDialogState
{
	private final JeanineFrame jf;
	private final HashMap<Integer, Object> lineTargets = new HashMap<>();

	public DialogBufferlist(JeanineFrame jf, List<CodeGroup> groups, CodeGroup activeGroup)
	{
		this.jf = jf;
		ArrayList<String> lines = new ArrayList<>();
		lines.add("This is a list of currently open code groups and their panels");
		lines.add("");
		lines.add("Navigate to a row and press ENTER, or double click a row, to nagivate");
		if (groups.isEmpty()) {
			lines.add("");
			lines.add("(no groups open currently)");
		}
		SB sbName = new SB(300);
		sbName.append("  ");
		int lineToPutCaretAt = 0;
		int id = 0;
		for (CodeGroup group : groups) {
			if (group == activeGroup) {
				lineToPutCaretAt = lines.size() - 1;
			}
			String title = group.title == null ? "<unnamed group>" : group.title;
			if (id == 0) {
				lines.add("/*jeanine:p:p:0;i:" + (id + 1) + ";x:0;y:20;a:b;n:" + title.replace(';', ':') + ";*/");
			} else {
				lines.add("/*jeanine:p:p:" + id + ";i:" + (id + 1) + ";x:20;y:0;a:t;n:" + title.replace(';', ':') + ";*/");
			}
			id++;
			lines.add(title);
			lineTargets.put(Integer.valueOf(lines.size() - 1), group);
			for (CodePanel panel : group.panels.values()) {
				if (activeGroup != null && activeGroup.activePanel == panel) {
					lineToPutCaretAt = lines.size() - 1;
				}
				panel.getNameForBufferlist(sbName);
				lines.add(sbName.toString());
				lineTargets.put(Integer.valueOf(lines.size() - 1), panel);
				sbName.length = 2;
			}
		}
		CodeGroup group = new CodeGroup(this.jf);
		group.title = "Buffer list";
		group.buffer.readonly = true;
		group.setContents(new Util.String2SBIter(lines.iterator()), true);
		group.buffer.carety = lineToPutCaretAt;
		group.activePanel = group.panelAtLine(group.buffer.carety);
		group.setLocationDontApply(0, 30);
		this.pushDialogState(jf, group, group);
		this.jf.centerCaret(true);
	}

	/*LineSelectionListener*/
	@Override
	public void lineSelected(LineSelectionListener.Info info)
	{
		int physLine = info.lineNumber;
		{
			CodePanel parent = info.panel.parent;
			while (parent != null) {
				physLine++;
				parent = parent.parent;
			}
		}
		Object target = this.lineTargets.get(Integer.valueOf(physLine));
		CodeGroup group = null;
		CodePanel panel = null;
		if (target instanceof CodeGroup) {
			group = ((CodeGroup) target);
			Iterator<CodePanel> x = group.panels.values().iterator();
			if (x.hasNext()) {
				panel = x.next();
			}
		} else if (target instanceof CodePanel) {
			panel = (CodePanel) target;
			group = panel.group;
		}
		if (panel != null && group != null) {
			this.jf.popState();
			this.jf.activeGroup = group;
			this.jf.activeGroup.activePanel = panel;
			panel.buffer.caretx = 0;
			panel.buffer.virtualCaretx = 0;
			panel.buffer.carety = panel.firstline;
			this.jf.centerCaret(false);
		}
	}
}
