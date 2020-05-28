package gradient;

import java.awt.*;
import java.awt.geom.Point2D;

public interface GradientFunction {
    public double forward(double x, double y);
    public Point2D.Double backward(double x, double y);
}
