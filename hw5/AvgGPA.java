// AvgGPA.java: Reads from standard input a list of letter grades and computes
// and prints the average GPA (the average of the numbers corresponding to
// the grades).

import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

public class AvgGPA {
    public static void main(String[] args) {
        ArrayST<String, Double> grades = new ArrayST<String, Double>();
        grades.put("A",  4.00);
        grades.put("B",  3.00);
        grades.put("C",  2.00);
        grades.put("D",  1.00);
        grades.put("F",  0.00);
        grades.put("A+", 4.33);
        grades.put("B+", 3.33);
        grades.put("C+", 2.33);
        grades.put("A-", 3.67);
        grades.put("B-", 2.67);
        grades.put("C-", 1.67);
        
        int n = 0;
        double total = 0.0;
        while (!StdIn.isEmpty()) {
           String a = StdIn.readString();
           double b = grades.get(a);
           total += b;
           n++;
        }
        
        StdOut.println(total/ (double) n);
   }
}
