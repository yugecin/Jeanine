package net.basdon.jeanine;

import java.awt.Font;
import java.awt.Rectangle;
import java.util.ArrayList;

public class DialogPreferences extends JeanineDialogState
{
	private static final String[] WARN_PREFS_WONT_BE_SAVED = {
		"Set either an environment variable or",
		"jvm arg with name " + Preferences.FILENAME_PROPERTY,
		"",
		"Preferences will not persist unless this is set.",
		"",
		"Press ENTER or ESC to continue."
	};
	public static final ArrayList<SB> instructionlines;

	static
	{
		instructionlines = new ArrayList<>();
		instructionlines.add(new SB("Preferences will be saved when"));
		instructionlines.add(new SB("exiting (with ESC)"));
		instructionlines.add(new SB());
		instructionlines.add(new SB("Press ENTER to apply the settings"));
		instructionlines.add(new SB());
		instructionlines.add(new SB("The actions in the panel to the right"));
		instructionlines.add(new SB("can be invoked by pressing ENTER or"));
		instructionlines.add(new SB("double cliking whilst on the line"));
		instructionlines.add(new SB("/*jeanine:p:i:1;p:0;a:b;y:30*/"));
		instructionlines.add(new SB("Append default light colorscheme"));
		instructionlines.add(new SB("Append default blue colorscheme"));
		instructionlines.add(new SB("Append current font settings"));
		instructionlines.add(new SB("Exit without saving preferences"));
	}

	private final JeanineFrame jf;
	private final CodeGroup instr, main;

	private boolean discardChanges;

	public DialogPreferences(JeanineFrame jf)
	{
		this.jf = jf;
		this.instr = new CodeGroup(jf);
		this.instr.title = "Information";
		this.instr.setContents(instructionlines.iterator(), true);
		this.instr.setLocation(80, 30);
		this.instr.buffer.readonly = true;
		Rectangle rect = new Rectangle();
		this.instr.getBounds(rect);
		this.main = new CodeGroup(jf);
		this.main.title = "Preferences";
		this.main.setContents(Preferences.lines.iterator(), true);
		this.main.activePanel = this.main.panelAtLine(this.main.buffer.carety);
		this.main.setLocation(rect.x + rect.width, 30);
		Preferences.lines = this.main.buffer.lines.lines;
		this.pushDialogState(jf, this.main, this.main, this.instr);
		if (Preferences.file == null) {
			new DialogSimpleMessage(jf, "Warning", WARN_PREFS_WONT_BE_SAVED);
		}
	}

	/*LineSelectionListener*/
	@Override
	public void lineSelected(LineSelectionListener.Info info)
	{
		if (info.group == this.instr && info.panel.id.intValue() == 1) {
			// TODO: just adding to lines makes it not undoable...
			BufferLines lines = this.main.buffer.lines;
			switch (info.lineNumber - info.panel.firstline) {
			case 0: // append default light colorscheme
				lines.add(new SB());
				lines.add(new SB("/*light (default) colorscheme*/"));
				Colors.reset();
				Preferences.appendColorScheme(lines::add);
				break;
			case 1: // append default blue colorscheme
				lines.add(new SB());
				lines.add(new SB("/*blue colorscheme*/"));
				Colors.blue();
				Preferences.appendColorScheme(lines::add);
				break;
			case 2: // append current font settings
				lines.add(new SB());
				lines.add(new SB("font.family " + this.jf.j.fontFamily));
				lines.add(new SB("font.size " + this.jf.j.fontSize));
				SB sb = new SB("font.flags ");
				if ((this.jf.j.fontFlags & Font.BOLD) != 0) {
					sb.append("BOLD ");
				}
				if ((this.jf.j.fontFlags & Font.ITALIC) != 0) {
					sb.append("ITALIC ");
				}
				if (sb.value[sb.length - 1] == ' ') {
					sb.length--;
				}
				lines.add(sb);
				break;
			case 3: // exit without changing settings
				this.discardChanges = true;
				this.jf.popState();
				return;
			}
			this.main.revalidateSizesAndReposition();
			this.main.buffer.carety = lines.size() - 1;
			this.main.buffer.caretx = 0;
			this.jf.activeGroup = this.main;
		}
		Preferences.interpretAndApply(this.jf);
		this.jf.ensureCaretInView();
	}

	/*Runnable*/
	@Override
	public void run()
	{
		Preferences.interpretAndApply(this.jf);
		if (!this.discardChanges) {
			Preferences.save();
		}
	}
}
