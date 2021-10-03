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
			String fontName = new String(line.value, 2, line.length - 2);
			this.j.setFont(new Font(fontName, Font.BOLD, 14));
		} else if (line.length > 2 && line.value[0] == 's') {
			int size = Integer.parseInt(new String(line.value, 2, line.length - 2));
			this.j.setFont(this.j.font.deriveFont((float) size));
		} else if (line.length > 0 && line.value[0] == 'b') {
			this.j.setFont(this.j.font.deriveFont(Font.BOLD));
		} else if (line.length > 0 && line.value[0] == 'p') {
			this.j.setFont(this.j.font.deriveFont(Font.PLAIN));
		} else {
			return;
		}
		for (CodeGroup group : this.jf.codegroups) {
			group.fontChanged();
		}
		this.jf.moveToGetCursorAtPosition(oldcursorpos);
	}
}
