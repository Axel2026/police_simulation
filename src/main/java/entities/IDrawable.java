package entities;

import org.jxmapviewer.JXMapViewer;

import java.awt.*;

/**
 * Represents element that can be drawn in GUI.
 */
public interface IDrawable {

    void drawSelf(Graphics2D g, JXMapViewer mapViewer);

}
