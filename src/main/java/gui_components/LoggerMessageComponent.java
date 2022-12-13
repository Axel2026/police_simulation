package gui_components;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class LoggerMessageComponent extends JPanel {

    private static DateTimeFormatter dateFormat = new DateTimeFormatterBuilder().appendPattern("HH:mm:ss dd MM yyyy").toFormatter();

    public LoggerMessageComponent(String message, LocalDateTime realWorldDate, long simulationTime) {
        setPreferredSize(new Dimension(800, 50));
        setMaximumSize(new Dimension(2000, 50));
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        var messageLabel = new JLabel(message);
        var realWorldDateLabel = new JLabel(dateFormat.format(realWorldDate), SwingConstants.RIGHT);
        var simulationTimeLabel = new JLabel(Long.toString(simulationTime));

        var timePanel = new JPanel();
        timePanel.setLayout(new GridLayout(0, 2));
        timePanel.add(simulationTimeLabel);
        timePanel.add(realWorldDateLabel);

        add(messageLabel);
        add(timePanel);
    }

}
