package Visualisation;

import javax.swing.*;

import org.jfree.chart.*;
import org.jfree.data.category.*;

public class LineChart extends JFrame {
    String title;
    String xAxisTitle;
    String yAxisTitle;
    DefaultCategoryDataset dataset;

    public LineChart(String title, String xAxisTitle, String yAxisTitle, DefaultCategoryDataset dataset, JFrame frameCharts) {
        this.title = title;
        this.xAxisTitle = xAxisTitle;
        this.yAxisTitle = yAxisTitle;
        this.dataset = dataset;
        JFreeChart chart = ChartFactory.createLineChart(
                title,
                xAxisTitle,
                yAxisTitle,
                dataset
        );

        ChartPanel panel = new ChartPanel(chart);
        frameCharts.getContentPane().add(panel);
        frameCharts.pack();
        frameCharts.setVisible(true);
        frameCharts.isAlwaysOnTop();
    }

    @Override
    public String getTitle() {
        return title;
    }
}
