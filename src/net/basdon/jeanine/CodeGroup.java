package net.basdon.jeanine;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.SwingUtilities;

public class CodeGroup
{
	public final JeanineFrame jf;
	public final HashMap<Integer, CodeFrame> frames;
	public final Point location;

	public CodeFrame rootFrame;

	public File ownerFile;
	public CodeFrame activeFrame;

	public CodeGroup(JeanineFrame jf)
	{
		this.jf = jf;
		this.frames = new HashMap<>();
		this.location = new Point();
	}

	public void readFile(File file)
	{
		this.ownerFile = file;
	}

	public void setContents(String text)
	{
		this.frames.clear();
		Integer id = Integer.valueOf(0);
		this.rootFrame = new CodeFrame(this.jf, this, id, text);
		this.frames.put(id, this.rootFrame);
		this.position(this.rootFrame);
		this.jf.add(this.rootFrame);
		this.rootFrame.setVisible(true);
	}

	public void setLocation(int x, int y)
	{
		this.location.x = x;
		this.location.y = y;
		if (this.rootFrame != null) {
			this.position(this.rootFrame);
		}
	}

	public void position(CodeFrame frame)
	{
		int x = this.location.x;
		int y = this.location.y;
		if (frame.parent != null) {
			CodeFrame parent = this.frames.get(frame.parent.id);
			if (parent != null) {
				Rectangle bounds = parent.getBounds();
				switch (frame.anchor) {
				case 't':
					break;
				case 'r':
					break;
				case 'b':
					x = bounds.x;
					y = bounds.y + bounds.height;
					break;
				case 'l':
					break;
				}
			}
		}
		x += frame.location.x;
		y += frame.location.y;
		frame.suppressNextMovedEvent = true;
		frame.setLocation(x, y);
		this.framePositionChanged(frame);
	}

	public void framePositionChanged(CodeFrame frame)
	{
		for (CodeFrame child : this.frames.values()) {
			if (child.parent == frame) {
				this.position(child);
			}
		}
	}

	public void split()
	{
		if (this.activeFrame == null) {
			this.jf.setError("can't split, no code panel focused");
			return;
		}
		if (this.activeFrame.buffer.mode != EditBuffer.SELECT_LINE_MODE) {
			this.jf.setError("can't split, need to be in 'select line' mode (ctrl+v)");
			return;
		}

		EditBuffer buffer = this.activeFrame.buffer;
		int len = buffer.lineselectto - buffer.lineselectfrom;
		StringBuilder[] linesIn = new StringBuilder[len];
		len = buffer.lines.size() - buffer.lineselectto;
		StringBuilder[] linesAfter = new StringBuilder[len];
		int i;
		for (i = buffer.lineselectfrom; i < buffer.lineselectto; i++) {
			linesIn[i - buffer.lineselectfrom] = buffer.lines.get(i);
		}
		for (; i < buffer.lines.size(); i++) {
			linesAfter[i - buffer.lineselectto] = buffer.lines.get(i);
		}
		Util.setArrayListSize(buffer.lines, buffer.lineselectfrom);
		buffer.mode = EditBuffer.NORMAL_MODE;
		if (buffer.lineselectfrom == 0) {
			buffer.lines.add(new StringBuilder());
			buffer.carety = 0;
		} else {
			buffer.carety = buffer.lineselectfrom - 1;
		}
		buffer.caretx = 0;
		this.activeFrame.codepanel.recheckMaxLineLength();
		this.activeFrame.codepanel.ensureCodeViewSize();

		Integer nextId = Integer.valueOf(this.findMaxId() + 1);
		this.activeFrame = this.add(nextId, linesIn, this.activeFrame, 'b', 0, 10);

		if (linesAfter.length != 0) {
			nextId = Integer.valueOf(nextId.intValue() + 1);
			this.add(nextId, linesAfter, this.activeFrame, 'b', 0, 10);
		}

		SwingUtilities.invokeLater(this.activeFrame.codepanel::requestFocusInWindow);
	}

	private CodeFrame add(
		Integer id,
		StringBuilder[] code,
		CodeFrame parent,
		char anchor,
		int posX,
		int posY)
	{
		CodeFrame cf = new CodeFrame(this.jf, this, id, null);
		this.frames.put(id, cf);
		cf.parent = parent;
		cf.anchor = (byte) anchor;
		cf.location.x = posX;
		cf.location.y = posY;
		cf.codepanel.buffer.lines.clear();
		for (int i = 0; i < code.length; i++) {
			cf.codepanel.buffer.lines.add(code[i]);
		}
		cf.codepanel.recheckMaxLineLength();
		cf.codepanel.ensureCodeViewSize();
		this.position(cf);
		cf.setVisible(true);
		this.jf.add(cf);
		return cf;
	}

	public void requestFocusInWindow()
	{
		if (this.activeFrame != null) {
			this.activeFrame.codepanel.requestFocusInWindow();
		} else {
			Iterator<CodeFrame> iter = this.frames.values().iterator();
			if (iter.hasNext()) {
				(this.activeFrame = iter.next()).codepanel.requestFocusInWindow();
			}
		}
		this.jf.activeGroup = this;
	}

	/**
	 * Call from codepanel when focus is gained.
	 *
	 * @return {@code false} if the request is denied
	 */
	public boolean focusGained(CodeFrame frame)
	{
		if (this.activeFrame != null &&
			this.activeFrame != frame &&
			this.activeFrame.buffer.mode != EditBuffer.NORMAL_MODE)
		{
			return false;
		}
		this.activeFrame = frame;
		return true;
	}

	public void mouseDragged(int dX, int dY)
	{
		Point loc = null;
		for (CodeFrame frame : this.frames.values()) {
			loc = frame.getLocation(loc);
			frame.setLocation(loc.x + dX, loc.y + dY);
		}
	}

	public int findMaxId()
	{
		int max = 0;
		for (Integer i : this.frames.keySet()) {
			if (i.intValue() > max) {
				max = i.intValue();
			}
		}
		return max;
	}

	public void dispose()
	{
	}
}
