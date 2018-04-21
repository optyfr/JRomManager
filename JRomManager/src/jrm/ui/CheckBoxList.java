package jrm.ui;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("serial")
public class CheckBoxList<E> extends JList<E>
{
	protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

	public CheckBoxList()
	{
		setCellRenderer(new CellRenderer());

		addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				int index = locationToIndex(e.getPoint());

				if(index != -1)
				{
					checkboxes[index].setSelected(!checkboxes[index].isSelected());
					for(ListSelectionListener l : getListSelectionListeners())
						l.valueChanged(new ListSelectionEvent(this, index, index, false));
					repaint();
				}
			}
		});
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	@Override
	public boolean isSelectedIndex(int index)
	{
		return checkboxes[index].isSelected();
	}
	
	JCheckBox checkboxes[] = null; 

	public class CellRenderer implements ListCellRenderer<E>
	{
		
		public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus)
		{
			if(checkboxes==null || checkboxes.length!=list.getModel().getSize()) checkboxes = new JCheckBox[list.getModel().getSize()];
			if(checkboxes[index]==null)
			{
				checkboxes[index] = new JCheckBox(); 
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
