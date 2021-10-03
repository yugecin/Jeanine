package net.basdon.jeanine;

import java.awt.Font;
import java.awt.Point;
import java.util.function.Consumer;

public class FontSelectionLineConsumer implements Consumer<SB>
{
	public final Jeanine j;
	public final JeanineFrame jf;

	public FontSelectionLineConsumer(Jeanine j, JeanineFrame jf)
	{
		this.j = j;
		this.jf = jf;
	}

	@Override
	public void accept(SB line)
	{
		Point oldcursorpos = this.jf.findCursorPosition();
		if (line.length() > 3 && line.value[0] == 'f') {
			this.j.fontFamily = new String(line.value, 2, line.length - 2);
		} else if (line.length > 2 && line.value[0] == 's') {
			int size = Integer.parseInt(new String(line.value, 2, line.length - 2));
			this.j.fontSize = size;
		} else if (line.length > 0 && line.value[0] == 'b') {
			this.j.fontFlags |= Font.BOLD;
		} else if (line.length > 0 && line.value[0] == 'p') {
			this.j.fontFlags &= ~Font.BOLD;
		} else {
			return;
		}
		this.j.updateFont();
		for (CodeGroup group : this.jf.codegroups) {
			group.fontChanged();
		}
		this.jf.moveToGetCursorAtPosition(oldcursorpos);
	}
}
