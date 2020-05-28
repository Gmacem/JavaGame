package gradient;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

public class GradientVisualizer extends JPanel
{
    private final Timer m_timer = initTimer();

    private static Timer initTimer()
    {
        Timer timer = new Timer("events generator", true);
        return timer;
    }

    private volatile double m_robotPositionX = 0;
    private volatile double m_robotPositionY = 0;
    private volatile double m_robotDirection = 0;

    private volatile GradientFunction gradient;
    private double learningRate;
    private BufferedImage surface;

    private final static double minWave = 380;
    private final static double maxWave = 781;

    private ArrayList<Point> trace = new ArrayList<>();

    public GradientVisualizer(GradientFunction strategy, double rate)
    {
        gradient = strategy;
        surface = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
        CalcSurface();
        learningRate = rate;
        m_timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                onRedrawEvent();
            }
        }, 0, 500);
        m_timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                onModelUpdateEvent();
            }
        }, 0, 10);

        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                setStartPosition(e.getPoint());
                repaint();
            }
        });
        setDoubleBuffered(true);
    }

    protected void setStartPosition(Point p) {
        m_robotPositionX = p.getX();
        m_robotPositionY = p.getY();
        trace.clear();
    }

    protected void CalcSurface() {
        double min = 1e9;
        double max = -1e9;
        for (int i = 0; i < surface.getWidth(); ++i) {
            for (int j = 0; j < surface.getHeight(); ++j) {
                min = Math.min(min, -gradient.forward(i, j));
                max = Math.max(max, -gradient.forward(i, j));
            }
        }

        for (int i = 0; i < surface.getWidth(); ++i) {
            for (int j = 0; j < surface.getHeight(); ++j) {
                double val = -gradient.forward(i, j);
                surface.setRGB(i, j, waveLengthToRGB(getWave(val, min, max)));
            }
        }
    }

    protected double getWave(double value, double min, double max) {
        double diffWave = maxWave - minWave;
        return (value - min) * diffWave / (max - min) + minWave;
    }

    protected void onRedrawEvent()
    {
        EventQueue.invokeLater(this::repaint);
    }

    private static double distance(double x1, double y1, double x2, double y2)
    {
        double diffX = x1 - x2;
        double diffY = y1 - y2;
        return Math.sqrt(diffX * diffX + diffY * diffY);
    }

    private static double angleTo(double fromX, double fromY, double toX, double toY)
    {
        double diffX = toX - fromX;
        double diffY = toY - fromY;

        return asNormalizedRadians(Math.atan2(diffY, diffX));
    }

    protected void onModelUpdateEvent()
    {
        Point2D.Double grad = gradient.backward(m_robotPositionX, m_robotPositionY);
        double newX = m_robotPositionX - grad.getX() * learningRate;
        double newY = m_robotPositionY - grad.getY() * learningRate;
        m_robotDirection = angleTo(m_robotPositionX, m_robotPositionY, newX, newY);
        m_robotPositionX = newX;
        m_robotPositionY = newY;
    }

    private static double asNormalizedRadians(double angle)
    {
        while (angle < 0)
        {
            angle += 2*Math.PI;
        }
        while (angle >= 2*Math.PI)
        {
            angle -= 2*Math.PI;
        }
        return angle;
    }

    private static int round(double value)
    {
        return (int)(value + 0.5);
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        Graphics2D g2d = (Graphics2D)g;
        drawSurface(g2d);
        drawTrace(g2d);
        drawRobot(g2d, round(m_robotPositionX), round(m_robotPositionY), m_robotDirection);
    }

    private static void fillOval(Graphics g, int centerX, int centerY, int diam1, int diam2)
    {
        g.fillOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private static void drawOval(Graphics g, int centerX, int centerY, int diam1, int diam2)
    {
        g.drawOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private void drawRobot(Graphics2D g, int x, int y, double direction)
    {
        int robotCenterX = round(m_robotPositionX);
        int robotCenterY = round(m_robotPositionY);
        addPointToTrace(robotCenterX, robotCenterY);
        AffineTransform t = AffineTransform.getRotateInstance(direction, robotCenterX, robotCenterY);
        g.setTransform(t);
        g.setColor(Color.MAGENTA);
        fillOval(g, robotCenterX, robotCenterY, 30, 10);
        g.setColor(Color.BLACK);
        drawOval(g, robotCenterX, robotCenterY, 30, 10);
        g.setColor(Color.WHITE);
        fillOval(g, robotCenterX  + 10, robotCenterY, 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, robotCenterX  + 10, robotCenterY, 5, 5);
    }

    private void addPointToTrace(int x, int y) {
        if (trace.isEmpty()) {
            trace.add(new Point(x, y));
        } else {
            Point last = trace.get(trace.size() - 1);
            if (distance(x, y, last.x, last.y) > 1) {
                trace.add(new Point(x, y));
            }
        }
    }

    private void drawSurface(Graphics2D g)
    {
        g.drawImage(surface, 0, 0, null);
    }

    private void drawTrace(Graphics2D g) {
        for (Point point : trace) {
            g.setColor(Color.WHITE);
            fillOval(g, round(point.getX()), round(point.getY()), 10, 10);
        }
        for (int i = 0; i + 1 < trace.size(); ++i) {
            g.setColor(Color.WHITE);
            g.drawLine(round(trace.get(i).getX()), round(trace.get(i).getY()),
                    round(trace.get(i + 1).getX()),round(trace.get(i + 1).getY()));
        }
    }

    static private double Gamma = 0.80;
    static private double IntensityMax = 255;

    public static int waveLengthToRGB(double Wavelength){
        double factor;
        double Red,Green,Blue;

        if((Wavelength >= 380) && (Wavelength<440)){
            Red = -(Wavelength - 440) / (440 - 380);
            Green = 0.0;
            Blue = 1.0;
        }else if((Wavelength >= 440) && (Wavelength<490)){
            Red = 0.0;
            Green = (Wavelength - 440) / (490 - 440);
            Blue = 1.0;
        }else if((Wavelength >= 490) && (Wavelength<510)){
            Red = 0.0;
            Green = 1.0;
            Blue = -(Wavelength - 510) / (510 - 490);
        }else if((Wavelength >= 510) && (Wavelength<580)){
            Red = (Wavelength - 510) / (580 - 510);
            Green = 1.0;
            Blue = 0.0;
        }else if((Wavelength >= 580) && (Wavelength<645)){
            Red = 1.0;
            Green = -(Wavelength - 645) / (645 - 580);
            Blue = 0.0;
        }else if((Wavelength >= 645) && (Wavelength<781)){
            Red = 1.0;
            Green = 0.0;
            Blue = 0.0;
        }else{
            Red = 0.0;
            Green = 0.0;
            Blue = 0.0;
        };

        if((Wavelength >= 380) && (Wavelength<420)){
            factor = 0.3 + 0.7*(Wavelength - 380) / (420 - 380);
        }else if((Wavelength >= 420) && (Wavelength<701)){
            factor = 1.0;
        }else if((Wavelength >= 701) && (Wavelength<781)){
            factor = 0.3 + 0.7*(780 - Wavelength) / (780 - 700);
        }else{
            factor = 0.0;
        };


        int[] rgb = new int[3];
        rgb[0] = Red==0.0 ? 0 : (int) Math.round(IntensityMax * Math.pow(Red * factor, Gamma));
        rgb[1] = Green==0.0 ? 0 : (int) Math.round(IntensityMax * Math.pow(Green * factor, Gamma));
        rgb[2] = Blue==0.0 ? 0 : (int) Math.round(IntensityMax * Math.pow(Blue * factor, Gamma));

        return new Color(rgb[0], rgb[1], rgb[2]).getRGB();
    }
}
