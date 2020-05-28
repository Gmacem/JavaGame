package gradient;

import java.awt.geom.Point2D;

public class Oval implements GradientFunction {

    private double centerX;
    private double centerY;

    private double aVal = 1;
    private double bVal = 4;

    public Oval(double x, double y) {
        centerX = x;
        centerY = y;
    }

    @Override
    public double forward(double x, double y) {
        double diffX = centerX - x;
        double diffY = centerY - y;

        return aVal * diffX * diffX + bVal * diffY * diffY;
    }

    @Override
    public Point2D.Double backward(double x, double y) {
        double diffX = x - centerX;
        double diffY = y - centerY;

        return new Point2D.Double(2 * diffX / aVal, 2 * diffY / bVal);
    }
}
