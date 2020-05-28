package gradient;

import java.awt.*;
import java.awt.geom.Point2D;

public class Circle2 implements GradientFunction {

    private double centerX1;
    private double centerY1;

    private double centerX2;
    private double centerY2;

    private double multA = 2;
    private double multB = 1;

    public Circle2(double x1, double y1, double x2, double y2) {
        centerX1 = x1;
        centerY1 = y1;
        centerX2 = x2;
        centerY2 = y2;
    }

    private double distToCenter(double x, double y, double centerX, double centerY)
    {
        double diffX = centerX - x;
        double diffY = centerY - y;
        return Math.sqrt(diffX * diffX + diffY * diffY);
    }

    @Override
    public double forward(double x, double y) {
        return distToCenter(x, y, centerX1, centerY1) * multA + distToCenter(x, y, centerX2, centerY2) * multB;
    }

    @Override
    public Point2D.Double backward(double x, double y) {
        double diffX1 = x - centerX1;
        double diffY1 = y - centerY1;
        double diffX2 = x - centerX2;
        double diffY2 = y - centerY2;
        double dist1 = distToCenter(x, y, centerX1, centerY1);
        double dist2 = distToCenter(x, y, centerX2, centerY2);
        return new Point2D.Double(diffX1 * multA / dist1 + diffX2 * multB / dist2,
                diffY1 * multA / dist1 + diffY2 * multB / dist2);
    }
}
