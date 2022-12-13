package utils;

import csv_export.ExportChangingStateDetails;
import entities.Patrol;
import gui_components.LoggerPanel;
import world.World;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;

public class Logger {

    private static final String LOGS_DIRECTORY_PATH = "logs";
    private static Logger instance;
    private final File logFile;
    private final List<LoggerPanel> loggingPanels = new ArrayList<>();
    private final DateTimeFormatter dateFormat = new DateTimeFormatterBuilder().appendPattern("dd-MM-yyyy_HH-mm-ss").toFormatter();

    private Logger() {
        File logsDirectory = new File(LOGS_DIRECTORY_PATH);
        if (!(logsDirectory.exists() && logsDirectory.isDirectory())) {
            logsDirectory.mkdir();
        }

        logFile = new File(LOGS_DIRECTORY_PATH, dateFormat.format(LocalDateTime.now()) + ".log");
        try {
            if (!logFile.createNewFile()) {
                throw new IOException("Unable to create file");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Logger getInstance() {
        // Result variable here may seem pointless, but it's needed for DCL (Double-checked locking).
        var result = instance;
        if (instance != null) {
            return result;
        }
        synchronized (Logger.class) {
            if (instance == null) {
                instance = new Logger();
            }
            return instance;
        }
    }

    public boolean addLoggingPanel(LoggerPanel panel) {
        return loggingPanels.add(panel);
    }

    public boolean removeLoggingPanel(LoggerPanel panel) {
        return loggingPanels.remove(panel);
    }

    public void clearLoggingPanels() {
        loggingPanels.clear();
    }

    public void logNewMessageChangingState(Patrol patrol, String previousState, String currentState) {
        ExportChangingStateDetails.getInstance().writeToCsvFile(patrol, previousState, currentState);
        String message;
        if (patrol.getAction() != null) {
            message = patrol + " state set from " + previousState + " to " + currentState + "; action: " + patrol.getAction().getClass().toString() + "; target: " + patrol.getAction().getTarget().toString();
        } else {
            message = patrol + " state set from " + previousState + " to " + currentState;
        }
        logNewMessage(message);
    }

    public void logNewOtherMessage(String message) {
        logNewMessage(message);
    }

    private void logNewMessage(String message) {
        var realDate = LocalDateTime.now();
        var simulationTime = World.getInstance().getSimulationTimeLong();

        var messageBuilder = new StringBuilder();
        messageBuilder.append(realDate.format(dateFormat));
        messageBuilder.append(" ");
        messageBuilder.append(simulationTime);
        messageBuilder.append(" ");
        messageBuilder.append(message);
        messageBuilder.append("\n");
        try {
            Files.write(logFile.toPath(), messageBuilder.toString().getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (var loggerPanel : loggingPanels) {
            loggerPanel.showNewLogMessage(message, realDate, simulationTime);
        }
    }

}
