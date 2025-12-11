package ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class GroupedListCellRenderer extends DefaultListCellRenderer {

    private final JLabel headerLabel;
    private final Icon fileIcon;

    public GroupedListCellRenderer() {
        headerLabel = new JLabel();
        headerLabel.setOpaque(true);
        headerLabel.setBackground(new Color(60, 63, 65));
        headerLabel.setForeground(new Color(187, 187, 187));
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        headerLabel.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 1, 0, Color.DARK_GRAY),
                new EmptyBorder(4, 10, 4, 10)
        ));

        fileIcon = new FlatSVGIcon(UI.class.getResource("/svgs/text/text_dark.svg"));
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        String text = (value == null) ? "" : value.toString();
        if (text.startsWith("---HEADER_")) {
            if (text.contains("OS")) {
                headerLabel.setText("Data from Filesystem");
            } else if (text.contains("DB")) {
                headerLabel.setText("Data from Database");
            }
            return headerLabel;
        }

        JLabel itemLabel = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        itemLabel.setIcon(fileIcon);
        itemLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        return itemLabel;
    }
}
