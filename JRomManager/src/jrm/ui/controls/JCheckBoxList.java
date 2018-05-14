package jrm.ui.controls;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("serial")
public class JCheckBoxList<E> extends JList<E>
{
	protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

	public JCheckBoxList()
	{
		setCellRenderer(new CellRenderer());

		addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				int index = locationToIndex(e.getPoint());

				if(index != -1 && e.getButton()==MouseEvent.BUTTON1)
				{
					checkboxes[index].setSelected(!checkboxes[index].isSelected());
					for(ListSelectionListener l : getListSelectionListeners())
						l.valueChanged(new ListSelectionEvent(this, index, index, false));
					repaint();
				}
				super.mousePressed(e);
			}
		});
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	@Override
	public void setModel(ListModel<E> model)
	{
		super.setModel(model);
		setEnabled(model.getSize()>0);
	}
	
	@Override
	public boolean isSelectedIndex(int index)
	{
		return checkboxes[index].isSelected();
	}
	
	public void selectAll()
	{
		for(JCheckBox checkbox : checkboxes)
			checkbox.setSelected(true);
		for(ListSelectionListener l : getListSelectionListeners())
			l.valueChanged(new ListSelectionEvent(this, 0, checkboxes.length-1, false));
		repaint();
	}
	
	public void selectNone()
	{
		for(JCheckBox checkbox : checkboxes)
			checkbox.setSelected(false);
		for(ListSelectionListener l : getListSelectionListeners())
			l.valueChanged(new ListSelectionEvent(this, 0, checkboxes.length-1, false));
		repaint();
	}
	
	public void selectInvert()
	{
		for(JCheckBox checkbox : checkboxes)
			checkbox.setSelected(!checkbox.isSelected());
		for(ListSelectionListener l : getListSelectionListeners())
			l.valueChanged(new ListSelectionEvent(this, 0, checkboxes.length-1, false));
		repaint();
	}
	
	JTristateCheckBox checkboxes[] = null; 

	public class CellRenderer implements ListCellRenderer<E>
	{
		
		public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus)
		{
			if(checkboxes==null || checkboxes.length!=list.getModel().getSize()) checkboxes = new JTristateCheckBox[list.getModel().getSize()];
			if(checkboxes[index]==null)
			{
				checkboxes[index] = new JTristateCheckBox(); 
				checkboxes[index].setFont(getFont());
				checkboxes[index].setFocusPainted(false);
				checkboxes[index].setBorderPainted(true);
			}
			checkboxes[index].setText(value.toString());
			checkboxes[index].setEnabled(isEnabled());
			checkboxes[index].setBackground(isSelected ? getSelectionBackground() : getBackground());
			checkboxes[index].setForeground(isSelected ? getSelectionForeground() : getForeground());
			checkboxes[index].setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
			return checkboxes[index];
		}
	}
}
