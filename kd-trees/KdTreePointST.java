import edu.princeton.cs.algs4.MaxPQ;
import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

public class KdTreePointST<Value> implements PointST<Value> {
    private Node root;
    private int N;
    
    // 2d-tree (generalization of a BST in 2d) representation.
    private class Node {
        private Point2D p;   // the point
        private Value val;   // the symbol table maps the point to this value
        private RectHV rect; // the axis-aligned rectangle corresponding to 
                             // this node
        private Node lb;     // the left/bottom subtree
        private Node rt;     // the right/top subtree

        // Construct a node given the point, the associated value, and the 
        // axis-aligned rectangle corresponding to the node.
        Node(Point2D p, Value val, RectHV rect) {
            this.p = p;
            this.val = val;
            this.rect = rect;
        }
    }

    // Construct an empty symbol table of points.
    public KdTreePointST() {
        root = null;
        N = 0;
    }

    // Is the symbol table empty?
    public boolean isEmpty() { 
        return size() == 0;
    }

    // Number of points in the symbol table.
    public int size() {
        return N;
    }

    // Associate the value val with point p.
    public void put(Point2D p, Value val) {
        RectHV rectangle = new RectHV(Double.NEGATIVE_INFINITY,  
              Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
              Double.POSITIVE_INFINITY);
        root = put(root, p, val, rectangle, true);
    }

    // Helper for put(Point2D p, Value val).
    private Node put(Node x, Point2D p, Value val, RectHV rect, boolean lr) {
        if (x == null) {
            N++;
            return new Node(p, val, rect);
        }
        
        if (x.p.equals(p)) {
            x.val = val;
        }
        else if (lr && p.x() < x.p.x() || !lr && p.y() < x.p.y()) {
           RectHV sub = (lr)  
                 ? new RectHV(rect.xmin(), rect.ymin(), x.p.x(), rect.ymax()) 
                 : new RectHV(rect.xmin(), rect.ymin(), rect.xmax(), x.p.y());
           x.lb = put(x.lb, p, val, sub, !lr);   
        }    
        else {
            RectHV sub = (lr) 
                 ? new RectHV(x.p.x(), rect.ymin(), rect.xmax(), rect.ymax())
                 : new RectHV(rect.xmin(), x.p.y(), rect.xmax(), rect.ymax());
           x.rt = put(x.rt, p, val, sub, !lr);   
        }
        return x;
    }

    // Value associated with point p.
    public Value get(Point2D p) {
        return get(root, p, true);
    }

    // Helper for get(Point2D p).
    private Value get(Node x, Point2D p, boolean lr) {
        if (x == null) { return null; }
        if (x.p.equals(p)) {
            return x.val;
        } 
        else if (lr && p.x() < x.p.x() || !lr && p.y() < x.p.y()) {
         
           return get(x.lb, p, !lr);   
        }    
        else {
           
           return get(x.rt, p, !lr);   
        }
        
    }

    // Does the symbol table contain the point p?
    public boolean contains(Point2D p) {
        return get(p) != null;
    }

    // All points in the symbol table, in level order.
    public Iterable<Point2D> points() {
        Queue<Node> p = new Queue<Node>();
        Queue<Node> q = new Queue<Node>();
        q.enqueue(root);
        while (!q.isEmpty()) {
            Node x = q.dequeue();
            if (x == null) continue;
            p.enqueue(x);
            q.enqueue(x.lb);
            q.enqueue(x.rt);
        }
        Queue<Point2D> r = new Queue<Point2D>();
        while (!p.isEmpty()) {
            r.enqueue(p.dequeue().p);
        }
        return r;
   
    
    }

    // All points in the symbol table that are inside the rectangle rect.
    public Iterable<Point2D> range(RectHV rect) {
        Queue<Point2D> q = new Queue<Point2D>();
        range(root, rect, q);
        return q;
    }

    // Helper for public range(RectHV rect).
    private void range(Node x, RectHV rect, Queue<Point2D> q) {
        if (x == null) { return; }
      
        if (rect.contains(x.p)) { 
            q.enqueue(x.p);
        }
        if (!rect.intersects(x.rect)) {
            return;
        }
        range(x.lb, rect, q);
        range(x.rt, rect, q);
    }

    // A nearest neighbor to point p; null if the symbol table is empty.
    public Point2D nearest(Point2D p) {
        return nearest(root, p, null, 
            Double.POSITIVE_INFINITY, true);
    }
    
    // Helper for public nearest(Point2D p).
    private Point2D nearest(Node x, Point2D p, Point2D nearest, 
                            double nearestDistance, boolean lr) {
        Point2D closest = nearest;
        double closestDistance = nearestDistance;
        if (x == null) { return closest; }
        
        if (!x.p.equals(p) && x.p.distanceSquaredTo(p) < closestDistance) {
            closestDistance = x.p.distanceSquaredTo(p);
            closest = x.p;
        }
        
        Node first = x.rt;
        Node second = x.lb;
        
        if (lr && p.x() < x.p.x() || !lr && p.y() < x.p.y()) {
                first = x.lb;
                second = x.rt;
        }
        if (first != null 
             && first.rect.distanceSquaredTo(p) < closestDistance) {
            closest = nearest(first, p, closest, closestDistance,
                    !lr);
            closestDistance = closest.distanceSquaredTo(p);
        }
        if (second != null
                && second.rect.distanceSquaredTo(p) < closestDistance) {
            closest = nearest(second, p, closest, closestDistance,
                    lr);
        }
        return closest;
        
    }

    // k points that are closest to point p.
    public Iterable<Point2D> nearest(Point2D p, int k) {
        MaxPQ<Point2D> pq = new MaxPQ<Point2D>(p.distanceToOrder());
        nearest(root, p, k, pq, true);
        return pq;
    }

    // Helper for public nearest(Point2D p, int k).
    private void nearest(Node x, Point2D p, int k, MaxPQ<Point2D> pq, 
                         boolean lr) {
         
        if (x == null || pq.size() >= k
        && pq.max().distanceSquaredTo(p) < x.rect.distanceSquaredTo(p)) {
            return;
        }
        if (!x.p.equals(p)) {
            pq.insert(x.p);
        }
        if (pq.size() > k) {
            pq.delMax();    
        }
        boolean leftBottom;
        if (lr && p.x() < x.p.x() || !lr && p.y() < x.p.y()) {
            nearest(x.lb, p, k, pq, !lr);
            leftBottom = true;
        } else {
            nearest(x.rt, p, k, pq, !lr);
            leftBottom = false;
        }
        nearest(leftBottom ? x.rt : x.lb, p, k, pq, !lr);
    }
    

    // Test client. [DO NOT EDIT]
    public static void main(String[] args) {
        KdTreePointST<Integer> st = new KdTreePointST<Integer>();
        double qx = Double.parseDouble(args[0]);
        double qy = Double.parseDouble(args[1]);
        double rx1 = Double.parseDouble(args[2]);
        double rx2 = Double.parseDouble(args[3]);
        double ry1 = Double.parseDouble(args[4]);
        double ry2 = Double.parseDouble(args[5]);
        int k = Integer.parseInt(args[6]);
        Point2D query = new Point2D(qx, qy);
        RectHV rect = new RectHV(rx1, ry1, rx2, ry2);
        int i = 0;
        while (!StdIn.isEmpty()) {
            double x = StdIn.readDouble();
            double y = StdIn.readDouble();
            Point2D p = new Point2D(x, y);
            st.put(p, i++);
        }
        StdOut.println("st.empty()? " + st.isEmpty());
        StdOut.println("st.size() = " + st.size());
        StdOut.println("First " + k + " values:");
        i = 0;
        for (Point2D p : st.points()) {
            StdOut.println("  " + st.get(p));
            if (i++ == k) {
                break;
            }
        }
        StdOut.println("st.contains(" + query + ")? " + st.contains(query));
        StdOut.println("st.range(" + rect + "):");
        for (Point2D p : st.range(rect)) {
            StdOut.println("  " + p);
        }
        StdOut.println("st.nearest(" + query + ") = " + st.nearest(query));
        StdOut.println("st.nearest(" + query + ", " + k + "):");
        for (Point2D p : st.nearest(query, k)) {
            StdOut.println("  " + p);
            
        }
        
    }
}
