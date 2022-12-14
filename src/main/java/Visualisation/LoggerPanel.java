package Visualisation;

import Visualisation.LoggerMessageComponent;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LoggerPanel {

    private static final int MAX_NUMBER_OF_COMPONENTS = 1000;
    private final JPanel frame;
    private final JPanelWithComponentsOrder scrollContent = new JPanelWithComponentsOrder();
    private final JScrollPane scrollPane = new JScrollPane(scrollContent);

    public LoggerPanel(JPanel panel) {
        this.frame = panel;
    }

    public void createWindow() {

        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));

        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        scrollPane.setSize(800, 400);
        frame.add(scrollPane);

        frame.setVisible(true);
    }

    public void showNewLogMessage(String message, LocalDateTime realWorldDate, long simulationTime) {
        var messageComponent = new LoggerMessageComponent(message, realWorldDate, simulationTime);
        scrollContent.add(messageComponent);

        removeExcessiveComponents();
        frame.revalidate();

        var scrollBar = scrollPane.getVerticalScrollBar();
        scrollBar.setValue(scrollBar.getMaximum());
    }

    private void removeExcessiveComponents() {
        if (scrollContent.getComponentCount() >= MAX_NUMBER_OF_COMPONENTS) {
            var components = scrollContent.getChildrenComponentsInOrder(100);
            for (var component : components) {
                scrollContent.remove(component);
            }
        }
    }

    private class JPanelWithComponentsOrder extends JPanel {

        private final List<Component> componentList = new ArrayList<>();

        @Override
        public void remove(Component component) {
            componentList.remove(component);
            super.remove(component);
        }

        @Override
        public void removeAll() {
            componentList.clear();
            super.removeAll();
        }

        @Override
        public void remove(int index) {
            var component = super.getComponent(index);
            componentList.remove(component);
            super.remove(index);
        }

        @Override
        public Component add(Component component) {
            componentList.add(component);
            return super.add(component);
        }

        public List<Component> getChildrenComponentsInOrder(int quantity) {
            return getChildrenComponentsInOrder(quantity, true);
        }

        public List<Component> getChildrenComponentsInOrder(int quantity, boolean fromOldest) {
            var result = new ArrayList<Component>();
            if (fromOldest) {
                for (int i = 0; i < quantity; i++) {
                    result.add(componentList.get(i));
                }
            } else {
                for (int i = 0; i < quantity; i++) {
                    result.add(componentList.get(componentList.size() - 1 - i));
                }
            }
            return result;
        }
    }
}
