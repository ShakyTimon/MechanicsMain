package me.deecaad.core.effects.shapes;

import me.deecaad.core.utils.DebugUtil;
import me.deecaad.core.utils.NumberUtils;
import me.deecaad.core.utils.VectorUtils;
import org.bukkit.util.Vector;

import java.util.Iterator;

public class Circle implements Shape {

    private Point[] points;
    private Vector b;
    private Vector c;

    /**
     * Constructs a circle with the given number of points and
     * the given amplitude (which is basically radius)
     *
     * @param points The number of points to track on a circle
     * @param amplitude How big of a circle to draw
     */
    public Circle(int points, double amplitude) {
        this(points, amplitude, 0);
    }

    /**
     * Constructs a circle with the given number of points and
     * the given amplitude (which is basically radius)
     *
     * @param points The number of points to track on a circle
     * @param amplitude How big of a circle to draw
     * @param startAngle The angle, in radians, to start at
     */
    public Circle(int points, double amplitude, double startAngle) {
        this.points = new Point[points];
        double period = VectorUtils.PI_2;

        for (int i = 0; i < points; i++) {
            double radian = VectorUtils.normalizeRadians(period / points * i + startAngle);
            double cos = amplitude * Math.cos(radian);
            double sin = amplitude * Math.sin(radian);
            this.points[i] = new Point(sin, cos);
        }
    }

    /**
     * Returns the number of points on this <code>Circle</code>
     *
     * @return Number of points
     */
    public int getPoints() {
        return points.length;
    }
    
    /**
     * Sets the axis to draw the circle on. Vectors
     * The circle is formed on the plane formed by
     * the b and c vectors. Since b and c are
     * perpendicular to a (and), the circle is formed
     * around vector a.
     *
     * If what you are drawing a circle around is changing
     * it's yaw and pitch,
     *
     * @param dir Vector to draw circle around
     */
    @Override
    public void setAxis(Vector dir) {
        Vector a = dir.clone().normalize();

        // This double checks to make sure we do not
        // produce a vector of length 0, which causes
        // issues with NaN during normalization.
        //b = new Vector(0, 0, 0);
        //while (NumberUtils.equals(b.length(), 0.0)) {
        //    Vector vector = Vector.getRandom();
        //    b = a.clone().crossProduct(vector);
        //}
        b = VectorUtils.getPerpendicular(a).normalize();
        c = a.clone().crossProduct(b).normalize();

        double dot1 = a.dot(b);
        double dot2 = b.dot(c);
        double dot3 = a.dot(c);

        // This is a resource consuming debug message, not really needed
        DebugUtil.assertTrue(NumberUtils.equals(dot1, 0.0), "A is not perpendicular to B");
        DebugUtil.assertTrue(NumberUtils.equals(dot2, 0.0), "B is not perpendicular to C");
        DebugUtil.assertTrue(NumberUtils.equals(dot3, 0.0), "A is not perpendicular to C");
    }

    @Override
    public Iterator<Vector> iterator() {
        return new CircleIterator(points, b, c);
    }

    private static class Point {
        
        private double sin;
        private double cos;
        
        private Point(double sin, double cos) {
            this.sin = sin;
            this.cos = cos;
        }
    }

    private static class CircleIterator implements Iterator<Vector> {

        private int index;
        private Point[] points;
        private Vector b;
        private Vector c;

        private CircleIterator(Point[] points, Vector b, Vector c) {
            this.points = points;
            this.b = b;
            this.c = c;
        }

        @Override
        public boolean hasNext() {
            return index + 1 < points.length;
        }

        @Override
        public Vector next() {
            Point current = points[index++];

            double x = current.cos * b.getX() + current.sin * c.getX();
            double y = current.cos * b.getY() + current.sin * c.getY();
            double z = current.cos * b.getZ() + current.sin * c.getZ();

            return new Vector(x, y, z);
        }
    }
}