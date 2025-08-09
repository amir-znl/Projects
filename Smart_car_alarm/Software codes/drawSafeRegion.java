
/*
 * Author: Amir Zeinali
 * Project : smart gps tracking, Embedded sytems class.
 * This is the class where the red safe region is drawn.
 */



package smartGPS;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.painter.Painter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

public class drawSafeRegion  implements Painter<JXMapViewer> {
    private final GeoPosition center;
    private final double radiusMeters;
    private final JXMapViewer mapViewer;

    public drawSafeRegion(GeoPosition center, double radiusMeters, JXMapViewer mapViewer) {
        this.center = center;
        this.radiusMeters = radiusMeters;
        this.mapViewer = mapViewer;
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int width, int height) {

        Point2D centerPoint =  map.convertGeoPositionToPoint(center);
        double pixelRadius = radiusMeters / metersPerPixel(center.getLatitude(),(int)Math.round( mapViewer.getZoom()));

        g.setColor(new Color(255, 0, 0, 100)); // Semi-transparent red
        g.fill(new Ellipse2D.Double(centerPoint.getX() - pixelRadius, centerPoint.getY() - pixelRadius, 
        		pixelRadius * 2, pixelRadius * 2));
    }
    
    double metersPerPixel(double latitude, int zoomLevel) {
    	double correctedZoom = 19 - zoomLevel;
        double earthCircumference = 40075017; // Earth's circumference in meters
        double latitudeRadians = Math.toRadians(latitude);
        return earthCircumference * Math.cos(latitudeRadians) / Math.pow(2, correctedZoom + 8);
    }

}






