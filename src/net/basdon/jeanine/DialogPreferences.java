package net.basdon.jeanine;

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

	private final JeanineFrame jf;
	private final CodeGroup instr, main;

	public DialogPreferences(JeanineFrame jf)
	{
		this.jf = jf;
		this.instr = new CodeGroup(this.jf);
		this.instr.title = "Information";
		this.instr.setContents(Preferences.instructionlines.iterator(), false);
		this.instr.setLocation(80, 30);
		this.instr.buffer.readonly = true;
		int y = this.instr.location.y + (this.instr.buffer.lines.size() + 4) * this.jf.j.fy;
		this.main = new CodeGroup(this.jf);
		this.main.title = "Preferences";
		this.main.setContents(Preferences.lines.iterator(), true);
		this.main.activePanel = this.main.panelAtLine(this.main.buffer.carety);
		this.main.setLocation(80, y);
		Preferences.lines = this.main.buffer.lines.lines;
		this.pushDialogState(jf, this.main, this.main, this.instr);
		if (Preferences.file == null) {
			new DialogSimpleMessage(jf, "Warning", WARN_PREFS_WONT_BE_SAVED);
		}
	}

	/*LineSelectionListener*/
	@Override
	public void lineSelected(SB line)
	{
		if (this.jf.activeGroup == this.instr) {
			BufferLines lines = this.main.buffer.lines;
			if (line.equals("light")) {
				lines.add(new SB());
				lines.add(new SB("/*light (default) colorscheme*/"));
				Colors.reset();
			} else if (line.equals("blue")) {
				lines.add(new SB());
				lines.add(new SB("/*blue colorscheme*/"));
				Colors.blue();
			} else {
				return;
			}
			Preferences.appendColorScheme(lines::add);
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
		Preferences.save();
	}
}
