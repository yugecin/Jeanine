package net.basdon.jeanine;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Point;

import javax.swing.JInternalFrame;

public class CodeFrame extends JInternalFrame
{
	private final Jeanine j;

	private int rows, cols;

	public Point locationBeforeDrag;

	public CodeFrame(JeanineFrame jf)
	{
		super("hi", false, false, false, false);
		this.j = jf.j;
		this.locationBeforeDrag = new Point();
		this.setFrameIcon(null);
		this.setContentPane(new CodePanel(this, jf));
		this.setVisible(true);
		this.setFocusable(false);
		this.setCodeViewSize(1, 30);
		this.setFocusTraversalPolicy(null);
		this.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
		this.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
	}

	public void ensureCodeViewSize(int rows, int cols)
	{
		if (cols < 30) {
			cols = 30;
		}
		if (this.rows != rows || this.cols != cols) {
			this.setCodeViewSize(rows, cols);
		}
	}

	public void setCodeViewSize(int rows, int cols)
	{
		this.rows = rows;
		this.cols = cols;
		Dimension size = new Dimension(2 + cols * this.j.fx, /*padding*/ 2 + rows * this.j.fy);
		Container container = this.getContentPane();
		container.setMinimumSize(size);
		container.setMaximumSize(size);
		container.setPreferredSize(size);
		this.pack();
	}
}
