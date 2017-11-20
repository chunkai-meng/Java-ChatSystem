import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class CheckboxListCellRenderer extends JCheckBox implements ListCellRenderer {
	private 	ArrayList<Integer> blockUserList = new ArrayList<Integer>();
	
	public Component getListCellRendererComponent(JList list, Object value, int index, 
            boolean isSelected, boolean cellHasFocus) {
		

        setComponentOrientation(list.getComponentOrientation());
        setFont(list.getFont());
//        System.out.println("block user: " + blockUser + "select user: " + selectedUser);
        setBackground(blockUserList.contains(index) ? Color.GRAY : list.getBackground() );
        setForeground(list.getForeground());
        setSelected(isSelected);
        setEnabled(list.isEnabled());
        setText(value == null ? "" : value.toString());  
        return this;
    }
	
	public void blockUser (ArrayList<Integer> l) {
		blockUserList = l;
		
		System.out.println(blockUserList);
	}
}
