package Visualisation;

import javax.swing.*;
import java.awt.*;

public class SettingSeparator extends JFrame {

    JSeparator jSeparator;

    public SettingSeparator(int orientation, Color backgroundColor, Color foregroundColor, Dimension preferredSize) {
        this.jSeparator = new JSeparator();
        jSeparator.setOrientation(orientation);
        jSeparator.setBackground(backgroundColor);
        jSeparator.setForeground(foregroundColor);
        jSeparator.setPreferredSize(preferredSize);
    }

    public JSeparator getJSeparator() {
        return jSeparator;
    }

}
