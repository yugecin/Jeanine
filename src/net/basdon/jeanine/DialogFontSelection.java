package net.basdon.jeanine;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;

public class DialogFontSelection extends JeanineDialogState
{
	private final JeanineFrame jf;

	public DialogFontSelection(JeanineFrame jf)
	{
		this.jf = jf;
		ArrayList<String> lines = new ArrayList<>();
		lines.add("c Welcome to font selection.");
		lines.add("c Put the caret on a setting and press enter.");
		lines.add("c Settings can be persisted using the preferences editor");
		lines.add("c Exit by pressing ESC.");
		lines.add("/*jeanine:p:i:2;p:0;x:0;y:20;a:b*/");
		lines.add("c Font size:");
		lines.add("s 6");
		lines.add("s 7");
		lines.add("s 8");
		lines.add("s 9");
		lines.add("s 10");
		lines.add("s 11");
		lines.add("s 12");
		lines.add("s 13");
		lines.add("s 14");
		lines.add("s 15");
		lines.add("s 16");
		lines.add("s 17");
		lines.add("s 18");
		lines.add("s 19");
		lines.add("s 20");
		lines.add("/*jeanine:p:i:3;p:2;x:0;y:20;a:b*/");
		lines.add("c Font style:");
		lines.add("b bold");
		lines.add("p plain");
		lines.add("/*jeanine:p:i:1;p:0;x:20;y:0;a:t*/");
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fonts = ge.getAvailableFontFamilyNames();
		for (String font : fonts) {
			lines.add("f " + font);
		}
		CodeGroup group = new CodeGroup(this.jf);
		group.title = "Font selection";
		group.buffer.readonly = true;
		group.setContents(new Util.String2SBIter(lines.iterator()), true);
		group.buffer.carety = group.panels.get(Integer.valueOf(1)).firstline;
		group.activePanel = group.panelAtLine(group.buffer.carety);
		group.setLocationDontApply(0, 30);
		this.pushDialogState(jf, group, group);
	}

	/*LineSelectionListener*/
	@Override
	public void lineSelected(LineSelectionListener.Info info)
	{
		SB line = info.lineContent;
		if (line.length() > 3 && line.value[0] == 'f') {
			this.jf.j.fontFamily = new String(line.value, 2, line.length - 2);
		} else if (line.length > 2 && line.value[0] == 's') {
			int size = Integer.parseInt(new String(line.value, 2, line.length - 2));
			this.jf.j.fontSize = size;
		} else if (line.length > 0 && line.value[0] == 'b') {
			this.jf.j.fontFlags |= Font.BOLD;
		} else if (line.length > 0 && line.value[0] == 'p') {
			this.jf.j.fontFlags &= ~Font.BOLD;
		} else {
			return;
		}
		this.jf.updateFontKeepCursorFrozen();
	}
}
