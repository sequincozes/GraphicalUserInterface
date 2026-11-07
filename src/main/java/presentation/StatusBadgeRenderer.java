package presentation;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
final class StatusBadgeRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 1L;
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String st = String.valueOf(value);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
            label.setBorder(new EmptyBorder(4, 10, 4, 10));

            if ("APROVADO".equals(st)) {
                label.setBackground(Color.decode("#D3F9D8"));
                label.setForeground(Color.decode("#1B5E20"));
            } else if ("SUBMETIDO".equals(st)) {
                label.setBackground(Color.decode("#FFF3BF"));
                label.setForeground(Color.decode("#9C6500"));
            } else {
                label.setBackground(Color.decode("#FFE3E3"));
                label.setForeground(Color.decode("#C92A2A"));
            }
            return label;
        }
    }
