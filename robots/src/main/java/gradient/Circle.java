package gradient;

import java.awt.*;
import java.awt.geom.Point2D;

public class Circle implements GradientFunction {

    private double centerX;
    private double centerY;

    public Circle(double x, double y) {
        centerX = x;
        centerY = y;
    }

    private double distToCenter(double x, double y)
    {
        double diffX = centerX - x;
        double diffY = centerY - y;
        return Math.sqrt(diffX * diffX + diffY * diffY);
    }

    @Override
    public double forward(double x, double y) {
        return distToCenter(x, y);
    }

    @Override
    public Point2D.Double backward(double x, double y) {
        double diffX = x - centerX;
        double diffY = y - centerY;
        double dist = distToCenter(x, y);
        return new Point2D.Double(diffX / dist, diffY / dist);
    }
}
