import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static void main( String[] args ) throws Exception {
        List<Point> points = getPoints();
        List<Point> convexHull = ConvexHull.build( points );
        double perimeter = calculatePerimeter( convexHull );
        submitPerimeter( perimeter );
    }

    private static double calculatePerimeter( List<Point> convexHull ) {
        double perimeter = 0;
        for (int i = 1; i < convexHull.size(); i++) {
            Point p1 = convexHull.get( i - 1 );
            Point p2 = convexHull.get( i );
            perimeter += Point.distance( p1, p2 );
        }
        Point first = convexHull.get( 0 );
        Point last = convexHull.get( convexHull.size() - 1 );
        perimeter += Point.distance( first, last );
        return perimeter;
    }

    private static List<Point> getPoints() throws Exception {
        List<Point> points = new LinkedList<Point>();

        Scanner sc = new Scanner( new File( "input.txt" ) );
        int count = sc.nextInt();
        for (int i = 0; i < count; i++) {
            int x = sc.nextInt();
            int y = sc.nextInt();
            points.add( new Point( x, y ) );
        }

        return points;
    }

    private static void submitPerimeter( double perimeter ) throws Exception {
        PrintWriter pw = new PrintWriter( "output.txt" );
        pw.println( String.format( Locale.US, "%.1f", perimeter ) );
        pw.close();
    }

}

class ConvexHull {

    public static class PointAngleComparator implements Comparator<Point> {

        private Map<Point, Double> cache = new HashMap<Point, Double>();

        @Override
        public int compare( Point p1, Point p2 ) {
            double angle1 = this.getAngle( p1 );
            double angle2 = this.getAngle( p2 );
            int compare = Double.compare( angle1, angle2 );
            if ( compare != 0 ) {
                return compare;
            }
            // if angles the same, compare by distance to point (0, 0)
            double dist1 = Point.distance( p1, Point.BASE );
            double dist2 = Point.distance( p2, Point.BASE );
            return Double.compare( dist1, dist2 );
        }

        private double getAngle( Point p ) {
            Double angle = this.cache.get( p );
            if ( angle == null ) {
                angle = Math.atan2( p.y, p.x );
                this.cache.put( p, angle );
            }
            return angle;
        }
    }

    public static List<Point> build( List<Point> points ) {

        Point leftLeast = getLeftLeastPoint( points );

        // remove left least point from list of points
        List<Point> remainderPoints = removePointFromList( points, leftLeast );

        // let left least point be a base
        List<Point> normalizedPoints = move( remainderPoints, -leftLeast.x, -leftLeast.y );

        Collections.sort( normalizedPoints, new PointAngleComparator() );

        LinkedList<Point> convexHull = new LinkedList<Point>();

        // initially convex hull is triangle
        convexHull.push( Point.BASE );
        convexHull.push( normalizedPoints.get( 0 ) );
        convexHull.push( normalizedPoints.get( 1 ) );

        for (int i = 2; i < normalizedPoints.size(); i++) {
            Point p = normalizedPoints.get( i );

            Point pA = convexHull.get( 1 ); // peek(head-1)
            Point pB = convexHull.get( 0 ); // peek(head)
            Point pC = p;

            Vector vAB = new Vector( pA, pB );
            Vector vBC = new Vector( pB, pC );

            int product = Vector.pseudoscalarProduct( vAB, vBC );

            while ((product <= 0) && (convexHull.size() > 2)) {
                convexHull.pop();

                pA = convexHull.get( 1 ); // peek(head-1)
                pB = convexHull.get( 0 ); // peek(head)

                vAB = new Vector( pA, pB );
                vBC = new Vector( pB, pC );

                product = Vector.pseudoscalarProduct( vAB, vBC );
            }

            convexHull.push( p );
        }

        return move( convexHull, leftLeast.x, leftLeast.y );
    }

    private static Point getLeftLeastPoint( List<Point> points ) {
        Point leftLeast = points.get( 0 );
        for (Point p : points) {
            if ( (p.y < leftLeast.y) || ((p.y == leftLeast.y) && (p.x < leftLeast.x)) ) {
                leftLeast = p;
            }
        }
        return leftLeast;
    }

    private static List<Point> removePointFromList( List<Point> points, Point pointToRemove ) {
        List<Point> ret = new ArrayList<Point>( points.size() );
        for (Point p : points) {
            if ( !pointToRemove.equals( p ) ) {
                ret.add( p );
            }
        }
        return ret;
    }

    private static List<Point> move( List<Point> points, int dx, int dy ) {
        List<Point> ret = new ArrayList<Point>( points.size() );
        for (Point p : points) {
            ret.add( p.move( dx, dy ) );
        }
        return ret;
    }

}

class Point {

    /**
     * Point with coordinates (x,y) = (0,0)
     */
    public static final Point BASE = new Point( 0, 0 );

    public final int x;

    public final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point move( int dx, int dy ) {
        return new Point( this.x + dx, this.y + dy );
    }

    @Override
    public int hashCode() {
        int result = 31 + this.x;
        result = (31 * result) + this.y;
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( this.getClass() != obj.getClass() ) {
            return false;
        }
        Point other = (Point) obj;
        if ( this.x != other.x ) {
            return false;
        }
        if ( this.y != other.y ) {
            return false;
        }
        return true;
    }

    public static double distance( Point p1, Point p2 ) {
        return Math.sqrt( sqr( p2.x - p1.x ) + sqr( p2.y - p1.y ) );
    }

    private static long sqr( int x ) {
        return (long) x * x;
    }
}

class Vector {

    public final int x;

    public final int y;

    public Vector(Point p1, Point p2) {
        this.x = p2.x - p1.x;
        this.y = p2.y - p1.y;
    }

    public static int pseudoscalarProduct( Vector v1, Vector v2 ) {
        return (v1.x * v2.y) - (v2.x * v1.y);
    }
}
