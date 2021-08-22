/* Conflict */
import java.util.*;

public class Conflict{

    final Letter A; // The conflict is restricted to a certain letter
    private final LinkedHashSet<Transition> Trans; // Transitions with letter A
    private final ArrayList<ArrayList<Integer>> Matrix; // A collection of conflicts is an Arraylist<Arraylist<Integer>> : Transition_index, Conflict_index -> Column, as an int
    private int n,s,k; // Number of rows /(transitions), of conflict matrices, and of factors
    private final HashMap<Integer,Letter> Solution; // Solution to the renaming problem: Transition_index -> new letter
    
    Conflict(Letter in_A, LinkedHashSet<Transition> in_Trans, int in_k){
        A = in_A;
        Trans = in_Trans;
        
        n = Trans.size();
        k = in_k;
        s = 0;

        Matrix = new ArrayList<ArrayList<Integer>>(n);
        initMatrixAndIndex();
        Solution = new HashMap<Integer,Letter>(n);
    }

    private void initMatrixAndIndex(){
        int i = 0;
        for (Transition T : Trans) {
            T.setIndex(i);
            i++;

            ArrayList<Integer> Rows = new ArrayList<Integer>();
            Matrix.add(Rows);
        }
    }

    public HashMap<Integer,Letter> getSolution(){
        // Return the solution
        return Solution;
    }

    public void addConflict(Decomposition Decomp, StateEncoding Enc, int source, int target){
        // A conflict occurs between the states given by source and target - Add a new matrix to the conflict
        // The given decomposition allows access to the adressing functions
        int prime, j, exp, source_entry, target_entry;
        s++;

        for (Transition T : Trans) {
            Integer row = 0;

            for (int i = 0; i < k; i++) {
                j = Enc.getComponent(i);
                prime = Enc.getPrime(i);
                exp = Enc.getExponent(prime);

                // Select j-th entry of (source mod prime^exp) in base prime
                source_entry = Enc.getEntry(source,j,prime,exp);
                target_entry = Enc.getEntry(target,j,prime,exp);

                // A 1 occurs in the i-th column if the transition is in the corresponding gamma
                row += ((Decomp.getConcretFunc(i).contains(T,source_entry,target_entry) ? 1 : 0) << (k-1-i));
            }

            Matrix.get(T.getIndex()).add(row);
        }
    }

    public void resolve(String mode){
        if (mode.equals("greedy")) resolve_greedy();
        if (mode.equals("ILP")) resolve_ILP();
    }

    // Make more pretty
    private void resolve_greedy(){
        if (s == 0) {
            // If there is no conflict just keep the symbol with flag 1
            Letter new_A = new Letter(A.getSymb() + "_1");
            for (Transition T : Trans) { 
                Solution.put(T.getIndex(),new_A);
            }
            return;
        }

        ArrayList<Integer> current_result = new ArrayList<Integer>(s);
        int new_letter_count = 0;
        int t,u;

        for (Transition T : Trans) {
            current_result.clear();
            t = T.getIndex();            

            if (isRowNull(t)) continue; // Transition already used in relabeling

            // Initialize current result
            for (int l = 0; l < s; l++) {
                // Store the row in the solution
                current_result.add(getRow(t,l));
            }

            // Remove the row from all matrices
            setRowNull(t);

            // Add the Transition to the renaming and define new letter
            new_letter_count++;
            Letter new_A = new Letter(A.getSymb() + "_" + new_letter_count);
            Solution.put(t,new_A);

            // Add other Transitions until the result spans all columns
            for (Transition U : Trans) {
                u = U.getIndex();

                // If Transition was already used in another relabeling, all rows are null
                if (isRowNull(u)) continue;

                // Cannot use the transition, since with the current result, it spans all columns in some matrix
                if (spansAllColumns(current_result, Matrix.get(u))) continue;
                
                for (int l = 0; l < s; l++) {
                    // Add the rows of the transition to the current result
                    current_result.set(l, addRows(current_result.get(l), getRow(u,l)));
                }

                // Remove row from conflict
                setRowNull(u);

                // Add the Transition to the renaming with the new letter
                Solution.put(u,new_A);
            }
        }
    }

    private void resolve_ILP(){
        // if (s == 0) is not needed. If this is the case, all rows will be seen as zero rows
        ConflictILP Ilp = new ConflictILP(this);
    }

    private static Integer addRows(Integer row_1, Integer row_2){
        // Add two rows
        return row_1 | row_2;
    }

    private boolean spansAllColumns(ArrayList<Integer> rows_1, ArrayList<Integer> rows_2){
        for (int l = 0; l < s; l++) {
            // Add rows_1 and rows_2 in each component and check whether it spans all columns in any of these
            if (addRows(rows_1.get(l), rows_2.get(l)) == (Math.pow(2,k)-1)) return true;
        }
        return false;
    }

    public void printSolution(){
        String out = "";
        for (Transition T : Trans) {
            out = out + "(" + T.getSource().getName() + "," + T.getLabel().getSymb() + "," + T.getTarget().getName() + ") -> " + Solution.get(T).getSymb() + " \n";
        }
        System.out.println(out);
    }

    public int getNrConf(){
        // Return Number of conflicts
        return s;
    }

    public int getNrCols(){
        // Return number of columns
        return k;
    }

    public int getNrRows(){
        // Return number of rows
        return n;
    }

    public int getRow(int i, int l){
        /* Return i-th row in l-th matrix */
        return Matrix.get(i).get(l);
    }

    public int getEntry(int i, int l, int j){
        // Return the j-th entry of the i-th row in the l-th matrix
        return (Matrix.get(i).get(l) & 1 << k-1-j) >> k-1-j;
    }

    public void setRowNull(int i){
        /* Set i-th row in all matrices to null */
        Matrix.set(i,null);
    }

    public boolean isRowNull(int i){
        /* Test whether row has already been eliminated */
        if (Matrix.get(i) == null) return true;
        return false;
    }

    public boolean isRowZero(int i){
        // Test whether the row has only 0-entries in all matrices
        // Note: If s == 0 the row is seen as zero
        for (int l = 0; l < s; l++) {
            if (getRow(i,l) != 0) return false;
        }
        return true;
    }

    public Transition getTransByIndex(int i){
        /* Get transition T with index i */
        for (Transition T : Trans) {
            if (T.getIndex() == i) return T;
        }
        return null;
    }

    public void print(){
        String out = "";
        String out_row = "";
        Integer row;

        for (int l = 0; l < s; l++) {
            out = "";

            for (Transition T : Trans) {
                out_row = "";

                out_row = "(" + T.getSource().getName() + "," + T.getLabel().getSymb() + "," + T.getTarget().getName() + ") : ";
                row = Matrix.get(T.getIndex()).get(l);

                if (row == null) {
                    out_row += "null";
                } else {
                    out_row += Integer.toBinaryString(row);
                }

                out = out + out_row + "\n";
            }

            System.out.println(out);
        }
    }
}

















