package net.basdon.jeanine;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class Preferences
{
	private static final String HEADER = "/*Preferences are evaluated from top to bottom.*/";

	public static final String FILENAME_PROPERTY = "JEANINE_PREFERENCES_FILE";
	public static final ArrayList<SB> instructionlines;

	static
	{
		instructionlines = new ArrayList<>();
		instructionlines.add(new SB("Preferences will be saved when"));
		instructionlines.add(new SB("exiting (with ESC)"));
		instructionlines.add(new SB());
		instructionlines.add(new SB("Default colorschemes:"));
		instructionlines.add(new SB("(Press ENTER to append to your settings)"));
		instructionlines.add(new SB("light"));
		instructionlines.add(new SB("blue"));
		instructionlines.add(new SB());
		instructionlines.add(new SB("Press ENTER to apply the settings"));
	}

	public static Jeanine j;
	public static File file;
	public static ArrayList<SB> lines;

	public static void load(Jeanine _j)
	{
		j = _j;
		lines = new ArrayList<>();
		String filepath = System.getenv(FILENAME_PROPERTY);
		if (filepath == null) {
			filepath = System.getProperty(FILENAME_PROPERTY);
		}
		if (filepath != null) {
		file = new File(filepath);
			try {
				if (file.exists() && Files.size(file.toPath()) > 0) {
					List<String> lns = Files.readAllLines(file.toPath());
					if (lns.size() != 0) {
						if (!HEADER.equals(lns.get(0))) {
							lines.add(new SB(HEADER));
						}
						for (String line : lns) {
							lines.add(new SB(line));
						}
						return;
					}
				}
			} catch (Exception e) {
				// TODO
			}
		}
		lines.add(new SB(HEADER));
		lines.add(new SB());
		lines.add(new SB("font.family " + j.fontFamily));
		lines.add(new SB("font.size " + j.fontSize));
		if ((j.fontFlags & (Font.BOLD | Font.ITALIC)) != 0) {
			SB sb = new SB("font.flags");
			if ((j.fontFlags & Font.BOLD) != 0) {
				sb.append(" BOLD");
			}
			if ((j.fontFlags & Font.ITALIC) != 0) {
				sb.append(" ITALIC");
			}
			lines.add(sb);
		}
		lines.add(new SB());
		appendColorScheme(lines);
	}

	private static void appendColorScheme(List<SB> lines)
	{
		if (Colors.bg.col == null) {
			lines.add(new SB(Colors.bg.name + " gradient"));
		}
		for (Colors c : Colors.ALL) {
			if (c.col != null) {
				int col = (c.col.getRed() << 16) |
					(c.col.getGreen() << 8) |
					(c.col.getBlue());
				lines.add(new SB(c.name + " " + String.format("%06X", col)));
			}
		}
	}

	public static void updateAfterChangingFont()
	{
		boolean haveFontFamily = false;
		boolean haveFontSize = false;
		boolean haveFontFlags = false;
		for (int i = lines.size(); i > 0; ) {
			SB line = lines.get(--i);
			if (!haveFontFamily && line.startsWith("font.family ")) {
				haveFontFamily = true;
				line.length = 12;
				line.append(j.fontFamily);
			} else if (!haveFontSize && line.startsWith("font.size ")) {
				haveFontSize = true;
				line.length = 10;
				line.append(j.fontSize);
			} else if (!haveFontFlags && line.startsWith("font.flags ")) {
				haveFontSize = true;
				line.length = 10;
				if ((j.fontFlags & Font.BOLD) != 0) {
					line.append(" BOLD");
				}
				if ((j.fontFlags & Font.ITALIC) != 0) {
					line.append(" ITALIC");
				}
			}
		}
		if (!haveFontFamily) {
			lines.add(new SB("font.family " + j.fontFamily));
		}
		if (!haveFontSize) {
			lines.add(new SB("font.size " + j.fontSize));
		}
		if (!haveFontFlags) {
			SB sb = new SB("font.flags");
			if ((j.fontFlags & Font.BOLD) != 0) {
				sb.append(" BOLD");
			}
			if ((j.fontFlags & Font.ITALIC) != 0) {
				sb.append(" ITALIC");
			}
			lines.add(sb);
		}
	}

	public static void save()
	{
		if (file != null) {
			try (FileOutputStream fos = new FileOutputStream(file, false)) {
				OutputStreamWriter writer;
				writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
				for (SB line : lines) {
					writer.write(line.value, 0, line.length);
					writer.write('\n');
				}
				writer.flush();
			} catch (Exception e) {
				e.printStackTrace();
				// TODO
			}
		}
	}

	public static void interpretAndApply(JeanineFrame jf)
	{
		HashMap<String, String> properties = new HashMap<>();
		for (int i = lines.size(); i > 0; ) {
			SB line = lines.get(--i);
			if (line.startsWith("/*")) {
				continue;
			}
			int spaceIdx = line.indexOf(' ', 0);
			if (spaceIdx <= 0) {
				continue;
			}
			String key = new String(line.value, 0, spaceIdx);
			if (properties.containsKey(key)) {
				continue;
			}
			int from = spaceIdx + 1;
			String val = new String(line.value, from, line.length - from);
			properties.put(key, val);
		}
		Colors.reset();
		for (Colors col : Colors.ALL) {
			String val = properties.get(col.name);
			if (val != null) {
				try {
					col.col = new Color(Integer.parseInt(val, 16));
				} catch (Exception e) {}
			}
		}
		if ("gradient".equals(properties.get(Colors.bg.name))) {
			Colors.bg.col = null;
		}
		String fontFamily = properties.get("font.family");
		String fontFlagss = properties.get("font.flags");
		String fontSizes = properties.get("font.size");
		int fontSize = j.fontSize;
		try {
			fontSize = Integer.parseInt(fontSizes);
		} catch (Exception e) {}
		int fontFlags = Font.PLAIN;
		if (fontFlagss != null) {
			if (fontFlagss.contains("BOLD")) {
				fontFlags |= Font.BOLD;
			}
			if (fontFlagss.contains("ITALIC")) {
				fontFlags |= Font.ITALIC;
			}
		}
		if (!j.fontFamily.equals(fontFamily) ||
			j.fontSize != fontSize ||
			j.fontFlags != fontFlags)
		{
			j.fontFamily = fontFamily;
			j.fontSize = fontSize;
			j.fontFlags = fontFlags;
			jf.updateFontKeepCursorFrozen();
		}
		jf.repaint();
	}

	public static class LineSelectionListener implements Consumer<SB>
	{
		private final CodeGroup instr, group;
		private final JeanineFrame jf;

		public LineSelectionListener(JeanineFrame jf, CodeGroup instr, CodeGroup group)
		{
			this.jf = jf;
			this.instr = instr;
			this.group = group;
		}

		@Override
		public void accept(SB line)
		{
			if (this.jf.activeGroup == this.instr) {
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
				appendColorScheme(lines);
				group.setContents(new ArrayList<>(lines).iterator(), true);
				Preferences.lines = group.buffer.lines.lines;
				this.jf.activeGroup = group;
				group.buffer.carety = group.buffer.lines.size() - 1;
				group.buffer.caretx = 0;
				interpretAndApply(this.jf);
				this.jf.ensureCaretInView();
			} else {
				interpretAndApply(this.jf);
			}
		}
	}
}
