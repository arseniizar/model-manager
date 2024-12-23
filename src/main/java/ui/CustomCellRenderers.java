package ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;

public class CustomCellRenderers {

    public static class ModelCellRenderer extends DefaultListCellRenderer {
        private final Icon classIcon = new FlatSVGIcon(UI.class.getResource("/svgs/class/classLevelWatch_dark.svg"));

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setFont(new Font("SansSerif", Font.PLAIN, 16));
            label.setIcon(classIcon);
            return label;
        }
    }

    public static class ScriptCellRenderer extends DefaultListCellRenderer {
        private final Icon configIcon = new FlatSVGIcon(UI.class.getResource("/svgs/config/config_dark.svg"));

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setFont(new Font("SansSerif", Font.PLAIN, 16));
            label.setIcon(configIcon);
            return label;
        }
    }

    public static class DataCellRenderer extends DefaultListCellRenderer {
        private final Icon textIcon = new FlatSVGIcon(UI.class.getResource("/svgs/text/text_dark.svg"));

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setFont(new Font("SansSerif", Font.PLAIN, 16));
            label.setIcon(textIcon);
            return label;
        }
    }
}
