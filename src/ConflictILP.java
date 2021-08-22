/* Class to set up and solve the ILP */
import java.util.*;
import java.io.*;

public class ConflictILP{

    final LinkedHashSet<Transition> Zero_Trans; // Collects all transitions that do not contribute to the conflict
    final LinkedHashSet<Integer> Zero_Rows; // Indices of the zero rows
    ArrayList<ArrayList<LinkedHashSet<Integer>>> Cols; // Models the sets cols_l/(i) = {j \in [1..k] | (A_l)_ij = 1}
    int n,s,k; // Number of rows/colors, of matrices, of columns
    
    ConflictILP(Conflict C){

        n = C.getNrRows();
        s = C.getNrConf();
        k = C.getNrCols();

        // Remove all Transitions that have only 0-row entries in all Conflict matrices and store them. These will be added to the first color later
        // This also includes the case if no conflict is present
        Zero_Trans = new LinkedHashSet<Transition>();
        Zero_Rows = new LinkedHashSet<Integer>();
        extractZeroRows(C);
        if (Zero_Trans.size() == n) return;

        // Construct the collumn sets
        Cols = new ArrayList<ArrayList<LinkedHashSet<Integer>>>(n);
        initCols(C);

        // Create the ILP in an lp-file
        initLPFile(C);

        // Solve ILP via calling lp_solve
        // Note: We use depth-first branch & bound (why is this so efficient?) Or: -Bf (best, so far) -BB -Br -Bd -BG


    }

    private void extractZeroRows(Conflict C){
        // Collect the rows that do not contribute to the conflict
        for (int i = 0; i < n; i++) {
            if (C.isRowZero(i)) {
                Zero_Trans.add(C.getTransByIndex(i));
                Zero_Rows.add(i);
                C.setRowNull(i);
            }
        }
    }

    private void initCols(Conflict C){
        // Construct the sets cols_l/(i)
        for (int i = 0; i < n; i++) {

            if (Zero_Rows.contains(i)) {
                Cols.add(null);

            } else {
                ArrayList<LinkedHashSet<Integer>> col_i = new ArrayList<LinkedHashSet<Integer>>(s);
                
                for (int l = 0; l < s; l++) {
                    LinkedHashSet<Integer> col_i_l = new LinkedHashSet<Integer>();

                    for (int j = 0; j < k; j++) {
                        if (C.getEntry(i,l,j) == 1) col_i_l.add(j);
                    }

                    col_i.add(col_i_l);
                }

                Cols.add(col_i);
                C.setRowNull(i);
            }
        }
    }

    private void initLPFile(Conflict C){
        try{
            // Init the file
            PrintWriter out = new PrintWriter("decomposition_ilp" + C.A.getSymb() + ".lp");
            out.println("\\ Integer Linear Program for Decomposing a Finite Automaton");
            out.println();

            // Write the objectrive function
            System.out.println("Write Objective Function.");
            writeObjectiveFunction(out);
            writeSeparator(out);

            // Write the equality constraints - same color/label within one row
            System.out.println("Write Equality Constraints.");
            writeEqualityConstraints(out);
            writeSeparator(out);

            // Write the constraints for exactly one color per row
            System.out.println("Write Constraints for Exactly one Color");
            writeExactlyOneColorPerRowConstraints(out);
            writeSeparator(out);

            // Write the constraints for the color indicator variables - they guarantee a conflict free coloring
            System.out.println("Write Constraints for Color Indicator");
            writeColorIndicatorConstraints(out);
            writeSeparator(out); 

            // Write the constraints for the characterization of colors via the y-variables
            System.out.println("Write Constraints for Characterization via Y Variables");
            writeColorCharacterizationConstraints(out);
            writeSeparator(out);

            // Write the constraints for counting colors via the z-variables
            System.out.println("Write Constraints for Characterization via Z Variables");
            writeColorCountingCharacterizationContraints(out);
            writeSeparator(out);

            // Write variables in the ILP
            out.println("Binary");
            System.out.println("Write Variables");
            writeVariables(out);
            writeSeparator(out);

            // Write SOS information
            System.out.println("Write SOS Information");
            writeSOS(out);
            out.close();
        }

        catch (IOException e) {
            System.err.println("Writing ILP causes IOException!");
        }
    }

    private int getFirstMatrix(int i){
        // Return the index of the first matrix where row i "hits" a column
        // i should not be a zero row: in this case -1 is returned
        for (int l = 0; l < n; l++) {
            if (Cols.get(i).get(l).isEmpty()) continue;
            return l;
        }

        return -1;
    }

    private int getFirstCol(int i, int l){
        // Return the first "hit" column of row i in matrix l
        // The set of columns should not be empty
        return Cols.get(i).get(l).iterator().next();
    }

    private void writeSeparator(PrintWriter out){
        out.println("\\ " + String.format("%-50s","").replace(" ","#"));
        out.println("\\ " + String.format("%-50s","").replace(" ","#"));
        out.println();
    }

    // TODO: Remove Expensive String objects and operations!

    private void writeObjectiveFunction(PrintWriter out){
        out.println("\\ Objective Function");
        out.println();

        String obj_function = "Minimize\n";
        for (int c = 0; c < n-1; c++) {
            obj_function += "z_" + c + " + ";
        }
        obj_function += "z_" + (n-1);

        out.println(obj_function);
        out.println();
    }

    private void writeEqualityConstraints(PrintWriter out){
        out.println("\\ Equality Contraints");
        out.println("Subject To");
        out.println();

        String lhs,rhs = "";

        for (int i = 0; i < n; i++) {
            if (Zero_Rows.contains(i)) continue;

            for (int c = 0; c < n; c++) {
                out.println("\\ Row " + i + ", Color " + c);
                
                for (int l = 0; l < s; l++) {
                    for (Integer j : Cols.get(i).get(l)) {
                        lhs = rhs;
                        rhs = "x[" + i + "][" + l + "][" + j + "][" + c + "]";
                        if (lhs.equals("")) continue;

                        out.println(lhs + " - " + rhs + " = 0");
                    }
                    
                }

                rhs = "";
                out.println();
            }
        }
    }

    private void writeExactlyOneColorPerRowConstraints(PrintWriter out){
        out.println("\\ Exactly One Color per Row");
        out.println();

        String sum = "";
        for (int i = 0; i < n; i++) {
            if (Zero_Rows.contains(i)) continue;

            out.println("\\ Row " + i);
            int l = getFirstMatrix(i);
            int j = getFirstCol(i,l);

            for (int c = 0; c < n; c++) {
                if (sum.equals("")) {
                    sum = "x[" + i + "][" + l + "][" + j + "][" + c + "]";
                } else {
                    sum += " + x[" + i + "][" + l + "][" + j + "][" + c + "]";
                }
            }

            sum += " = 1";
            out.println(sum);
            out.println();
            sum = "";
        }
    }

    private void writeColorIndicatorConstraints(PrintWriter out){
        out.println("\\ Color Indicator Constraints");
        out.println();

        String sum = "";
        for (int l = 0; l < s; l++) {
            for (int c = 0; c < n; c++) {
                out.println("\\ Matrix " + l + ", Color " + c);

                for (int j = 0; j < k; j++) {
                    if (sum.equals("")) {
                        sum = "y[" + l + "][" + j + "][" + c + "]";
                    } else {
                        sum += " + y[" + l + "][" + j + "][" + c + "]";
                    }
                }

                sum += " <= " + (k-1);
                out.println(sum);
                out.println();
                sum = "";
            }
        }
    }

    private void writeColorCharacterizationConstraints(PrintWriter out){
        out.println("\\ Color Characterization Constraints");
        out.println();

        String constr = "";
        for (int l = 0; l < s; l++) {
            for (int j = 0; j < k; j++) {
                for (int c = 0; c < n; c++) {
                    out.println("\\ Characterization: Matrix " + l + ", Column " + j + ", Color " + c);

                    for (int i = 0; i < n; i++) {
                        if (Zero_Rows.contains(i)) continue;
                        if (!Cols.get(i).get(l).contains(j)) continue;

                        // Such an index i definitley exists for each combination l,j
                        if (constr.equals("")) {
                            constr = "x[" + i + "][" + l + "][" + j + "][" + c + "]";
                        } else {
                            constr += " + x[" + i + "][" + l + "][" + j + "][" + c + "]";
                        }
                    }

                    constr += " - " + n + " y[" + l + "][" + j + "][" + c + "]";
                    out.println(constr + " <= 0");
                    out.println(constr + " >= " + (1-n));
                    out.println();
                    constr = "";
                }
            }
        }
    }

    private void writeColorCountingCharacterizationContraints(PrintWriter out){
        out.println("\\ Color Counting Characterization Constraints");
        out.println();

        String constr = "";
        int l,j;
        for (int c = 0; c < n; c++) {
            out.println("\\ Color " + c);

            for (int i = 0; i < n; i++) {
                if (Zero_Rows.contains(i)) continue;

                l = getFirstMatrix(i);
                j = getFirstCol(i,l);
                if (constr.equals("")) {
                    constr = "x[" + i + "][" + l + "][" + j + "][" + c + "]";
                } else {
                    constr += " + x[" + i + "][" + l + "][" + j + "][" + c + "]";
                }
            }

            constr += " - " + n + " z_" + c;
            out.println(constr + " <= 0");
            out.println(constr + " >= " + (1-n));
            out.println();
            constr = "";
        }
    }

    private void writeVariables(PrintWriter out){
        out.println("\\ Variables");
        out.println();

        boolean first = true;
        for (int i = 0; i < n; i++) {
            out.println("\\ X Variables Row " + i);
            if (Zero_Rows.contains(i)) continue;
            //System.out.println("Row " + i);

            for (int l = 0; l < s; l++) {
                for (Integer j : Cols.get(i).get(l)) {
                    for (int c = 0; c < n; c++) {
                        if (!first) {
                            out.print(" x[" + i + "][" + l + "][" + j + "][" + c + "]");
                        } else {
                            out.print("x[" + i + "][" + l + "][" + j + "][" + c + "]");
                            first = false;
                        } 
                    }
                }
            }

            out.println();
            out.println();
            first = true;
        }

        out.println("\\ Y Variables");
        first = true;
        for (int l = 0; l < s; l++) {
            for (int j = 0; j < k; j++) {
                for (int c = 0; c < n; c++) {
                    if (!first) {
                        out.print(" y[" + l + "][" + j + "][" + c + "]");
                    } else {
                        out.print("y[" + l + "][" + j + "][" + c + "]");
                        first = false;
                    }
                }
            }
        }

        out.println();
        out.println();
        first = true;

        out.println("\\ Z Variables");
        for (int c = 0; c < n; c++) {
            if (!first) {
                out.print(" z_" + c);
            } else {
                out.print("z_" + c);
                first = false;
            }
        }

        out.println();
        out.println();
    }

    private void writeSOS(PrintWriter out){
        out.println("\\ Special Orderes Sets");
        out.println("SOS");
        out.println();

        boolean first = true;
        LinkedHashSet<Integer> row_matrix = null;
        for (int i = 0; i < n; i++) {
            out.println("\\ Row " + i);
            if (Zero_Rows.contains(i)) continue;

            for (int l = 0; l < s; l++) {

                for (Integer j : Cols.get(i).get(l)) {
                    out.print("sos_" + i + "_" + l + "_" + j + ": S1 :: ");

                    for (int c = 0; c < n; c++) {
                        if (!first) {
                            out.print(" x[" + i + "][" + l + "][" + j + "][" + c + "]:1");
                        } else {
                            out.print("x[" + i + "][" + l + "][" + j + "][" + c + "]:1");
                            first = false;
                        }
                    }

                    out.println();
                    first = true;
                }
            }

            out.println();
        }
    }
}















