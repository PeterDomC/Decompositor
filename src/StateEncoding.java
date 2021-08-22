/* Encoding of the States */
import java.util.*;
import java.io.*;
import gurobi.*;

public class StateEncoding{
    
    private final ArrayList<Integer> prime_factors; // Prime factors of the nr. of states, or only 2s in the case of a binary decomposition
    private final HashMap<Integer,Integer> exponents; // Exponents of prime factors 
    private final int factor_number; // Number of prime factors or number of bits needed to encode states

   // ArrayList<Integer> minDistEncoding;

    public StateEncoding(int n, String mode){
        // Constructor
        prime_factors = new ArrayList<Integer>();
        exponents = new HashMap<Integer,Integer>();
        initFactorization(n,mode);
        factor_number = getFactorNumber();
    }

    private void initFactorization(int num, String mode){
        // Stores the prime factors of num or the number of twos needed for a binary encoding of num
        int last_prime = 0;
        int exponent = 0;
        boolean prime_set = false;

        if (mode.equals("prime")){

            // A simple prime factorization
            for (int i = 2; i <= num; i++) {
                if (num % i == 0) {
                    prime_factors.add(i);

                    if (i == last_prime){
                        // Same prime found, increase exponent
                        exponent++;
                    } else {
                        // Found new prime factor, add exponent from last factor. But only if one prime was already found
                        if(prime_set) exponents.put(last_prime,exponent);
                        exponent = 1;
                        last_prime = i;
                        prime_set = true;
                    }

                    num /= i;
                    i--;
                }
            }

            // Add exponent of last prime
            exponents.put(last_prime,exponent);

        } else {

            // Bits needed for the binary representation, only one factor: 2
            int k = Integer.SIZE-Integer.numberOfLeadingZeros(num-1);
            for (int i = 0; i < k; i++) {
                prime_factors.add(2);
            }
            exponents.put(2,k);
        }
    }

    public void initEncoding(Autom B){
        // Generate an enumeration of the states
        LinkedHashSet<State> Stateset = B.getStates();
        int i = 0;

        for (State Q : Stateset) {
            Q.setEnumerator(i);
            i++;
        }
    }

    public void stdEncoding(Autom B){
        // Standard Encoding is in insertion order
        LinkedHashSet<State> Stateset = B.getStates();
        int i = 0;

        for (State Q : Stateset) {
            Q.setIndex(i);
            i++;
        }
    }

    public void greedyDistanceEncoding(Autom B){
        LinkedHashSet<State> Stateset = B.getStates();
        int s = Stateset.size();
        HashMap<State,Integer> CurrentEnc = new HashMap<State,Integer>(s);
        HashMap<State,Integer> minEnc = null;
        State minState = null;
        int local_min = -1;
        int currentDist;
        int global_min = -1;
        
        for (State Q : Stateset) {
            // Start by assigning a state the value 0
            CurrentEnc.put(Q,0);

            for (int index = 1; index < s; index++) {
                minState = null;

                for (State P : Stateset) {
                    if (minState == null && CurrentEnc.get(P) == null) {
                        // P is the first state that was not assigned a value in the current encoding
                        CurrentEnc.put(P,index);
                        local_min = partialDistance(B,CurrentEnc);
                        minState = P;
                        continue;
                    }

                    if (CurrentEnc.get(P) == null) {
                        // P was not assigned a value in the current encoding
                        // Replace minState by P in the encoding and test

                        CurrentEnc.put(minState,null);
                        CurrentEnc.put(P,index);
                        currentDist = partialDistance(B,CurrentEnc);

                        if (currentDist < local_min) {
                            // Better State found for distance of encoding
                            minState = P;
                            local_min = currentDist;

                        } else {
                            // State is not better, undo changes
                            CurrentEnc.put(P,null);
                            CurrentEnc.put(minState,index);

                        }
                    }
                }
            }

            // Particular encoding found
            if (minEnc == null) {
                // Minimal encoding not set yet
                minEnc = new HashMap<State,Integer>(CurrentEnc);
                global_min = partialDistance(B,minEnc);
            }

            if (partialDistance(B,CurrentEnc) < global_min) {
                // Compare distances
                minEnc = new HashMap<State,Integer>(CurrentEnc);
                global_min = partialDistance(B,minEnc);
            }

            System.out.println("The current min is " + global_min);

            CurrentEnc = new HashMap<State,Integer>(s);
        }

        applyEncoding(B,minEnc);
    }

    private int partialDistance(Autom B, HashMap<State,Integer> Enc){
        // Calculate the partial distance of the given encoding
        LinkedHashSet<State> Stateset = B.getStates();
        LinkedHashSet<Transition> Trans = B.getTransitions();
        int dist = 0;

        for (Transition T : Trans) {
            for (Transition R : Trans) {
                dist += partialDistanceTransitions(T,R,Enc);
            }
        }

        return dist;
    }

    private int partialDistanceTransitions(Transition T, Transition R, HashMap<State,Integer> Enc){
        // Returns the distance between two transitions under the given encoding
        if (!T.getLabel().equals(R.getLabel())) return 0;
        int dist = 0;
        State Ts = T.getSource();
        State Rs = R.getSource();
        State Tt = T.getTarget();
        State Rt = R.getTarget();

        if (Enc.get(Ts) == null || Enc.get(Rs) == null || Enc.get(Tt) == null || Enc.get(Rt) == null) {
            return 0;
        } else {
            return partialDistanceOr(Enc.get(Ts), Enc.get(Rs), Enc.get(Tt), Enc.get(Rt));
        }
    }

    private int partialDistanceOr(int ts, int rs, int tt, int rt){
        // Returns the number of positions i, where ts_i != rs_i OR tt_i != rt_i
        int dist = 0;
        int j,prime,exp;
        for (int i = 0; i < factor_number; i++) {
            j = getComponent(i);
            prime = getPrime(i);
            exp = getExponent(prime);
            if ((getEntry(ts,j,prime,exp) != getEntry(rs,j,prime,exp)) || (getEntry(tt,j,prime,exp) != getEntry(rt,j,prime,exp))) dist++;
        }

        return dist;
    }

    private void applyEncoding(Autom B, HashMap<State,Integer> Enc){
        LinkedHashSet<State> Stateset = B.getStates();
        for (State Q : Stateset) {
            //System.out.println(Q.getName());
            Q.setIndex(Enc.get(Q));
        }
    }

    public void minDistEncoding(Autom B){
        // Create ILP for finding a minimal distance encoding
        try{

            System.out.println("\n############################################################");
            System.out.println("############################################################\n");
            System.out.println("Set up Gurobi Model...");

            GRBEnv environ = new GRBEnv("decomposition.log");
            GRBModel decomp_model = new GRBModel(environ);

            LinkedHashSet<State> Stateset = B.getStates();
            LinkedHashSet<Letter> Sigma = B.getAlphabet();
            LinkedHashSet<Transition> Trans;
            int s = Stateset.size();

            // ############################################################
            // ############################################################

            // Add variables to the ILP
            System.out.println("\nAdding variables...");
            GRBVarSet VarSet = new GRBVarSet(s,factor_number);

            // X-Variables
            for (int i = 0; i < s; i++) {
                for (int j = 0; j < factor_number; j++) {
                    GRBVar x = decomp_model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x[" + i + "][" + j + "]");
                    VarSet.addX(i,j,x);
                }
            }

            // Z-Variables
            for (int i = 0; i < s; i++) {
                for (int ipr = 0; ipr < s; ipr++) {
                    if (ipr > i) {

                        for (int j = 0; j < factor_number; j++) {
                            GRBVar z = decomp_model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "z[" + i + "][" + ipr + "][" + j + "]");
                            VarSet.addZ(i,ipr,j,z);
                        }
                    }
                }
            }

            // Y-Variables
            int i_max, i_min, l_max, l_min;
            for (Letter A : Sigma) {
                Trans = B.projectTransitions(A);

                for (Transition T : Trans) {
                    for (Transition U : Trans) {
                        if (!T.equals(U)) {

                            i_max = Math.max(T.getSource().getEnumerator(),U.getSource().getEnumerator());
                            i_min = Math.min(T.getSource().getEnumerator(),U.getSource().getEnumerator());
                            l_max = Math.max(T.getTarget().getEnumerator(),U.getTarget().getEnumerator());
                            l_min = Math.min(T.getTarget().getEnumerator(),U.getTarget().getEnumerator());

                            if (!VarSet.containsY(i_min,i_max,l_min,l_max)) {
                                ArrayList<GRBVar> col = new ArrayList<GRBVar>(factor_number);

                                for (int j = 0; j < factor_number; j++) {
                                    GRBVar y = decomp_model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "y[" + i_min + "][" + i_max + "][" + l_min + "][" + l_max + "][" + j + "]");
                                    col.add(j,y);
                                }

                                VarSet.addY(i_min,i_max,l_min,l_max,col);
                            }
                        }
                    }
                }
            }

            decomp_model.update();

            // ############################################################
            // ############################################################

            // Constraints
            System.out.println("\nAdding constraints...");

            // Absolute Value Constraints
            GRBLinExpr constr_one, constr_two, constr_thr, constr_fou;
            GRBVar x,xpr,z,L;
            for (int i = 0; i < s; i++) {
                for (int ipr = 0; ipr < s; ipr++) {

                    if (ipr > i) {
                        for (int j = 0; j < factor_number; j++) {

                            x = VarSet.getX(i,j);
                            xpr = VarSet.getX(ipr,j);
                            z = VarSet.getZ(i,ipr,j);

                            constr_one = new GRBLinExpr();
                            constr_one.addTerm(1.0,x);
                            constr_one.addTerm(1.0,xpr);
                            constr_one.addTerm(-1.0,z);
                            decomp_model.addConstr(constr_one, GRB.GREATER_EQUAL, 0.0, "");

                            constr_two = new GRBLinExpr();
                            constr_two.addTerm(1.0,x);
                            constr_two.addTerm(-1.0,xpr);
                            constr_two.addTerm(-1.0,z);
                            decomp_model.addConstr(constr_two, GRB.LESS_EQUAL, 0.0, "");

                            constr_thr = new GRBLinExpr();
                            constr_thr.addTerm(-1.0,x);
                            constr_thr.addTerm(1.0,xpr);
                            constr_thr.addTerm(-1.0,z);
                            decomp_model.addConstr(constr_thr, GRB.LESS_EQUAL, 0.0, "");

                            constr_fou = new GRBLinExpr();
                            constr_fou.addTerm(-1.0,x);
                            constr_fou.addTerm(-1.0,xpr);
                            constr_fou.addTerm(-1.0,z);
                            decomp_model.addConstr(constr_fou, GRB.GREATER_EQUAL, -2.0, "");
                        }
                    }
                }
            }

            // Injective Mapping Constraints
            GRBLinExpr constr_inj;
            for (int i = 0; i < s; i++) {
                for (int ipr = 0; ipr < s; ipr++) {

                    if(ipr > i) {
                        constr_inj = new GRBLinExpr();

                        for (int j = 0; j < factor_number; j++) {
                            constr_inj.addTerm(1.0, VarSet.getZ(i,ipr,j));
                        }

                        decomp_model.addConstr(constr_inj, GRB.GREATER_EQUAL, 1.0, "");
                    }
                }
            }

            // OR Constraints
            GRBVar y,zs,zt;
        //    GRBVar[] zs;
            for (Letter A : Sigma) {
                Trans = B.projectTransitions(A);

                for (Transition T : Trans) {
                    for (Transition U : Trans) {
                        
                        if (!T.equals(U)) {

                            i_max = Math.max(T.getSource().getEnumerator(),U.getSource().getEnumerator());
                            i_min = Math.min(T.getSource().getEnumerator(),U.getSource().getEnumerator());
                            l_max = Math.max(T.getTarget().getEnumerator(),U.getTarget().getEnumerator());
                            l_min = Math.min(T.getTarget().getEnumerator(),U.getTarget().getEnumerator());

                            for (int j = 0; j < factor_number; j++) {
                                
                                y = VarSet.getY(i_min,i_max,l_min,l_max,j);
                            //    zs = new GRBVar[2];
                            //    zs[0] = VarSet.getZ(i_min,i_max,j);
                            //    zs[1] = VarSet.getZ(l_min,l_max,j);

                            //    decomp_model.addGenConstrOr(y,zs,"");

                                zs = VarSet.getZ(i_min,i_max,j);
                                zt = VarSet.getZ(l_min,l_max,j);
                                
                                constr_one = new GRBLinExpr();
                                constr_one.addTerm(1.0,zs);
                                constr_one.addTerm(1.0,zt);
                                constr_one.addTerm(-1.0,y);
                                decomp_model.addConstr(constr_one, GRB.GREATER_EQUAL, 0.0, "");

                                constr_two = new GRBLinExpr();
                                constr_two.addTerm(1.0,zs);
                                constr_two.addTerm(-1.0,y);
                                decomp_model.addConstr(constr_two, GRB.LESS_EQUAL, 0.0, "");

                                constr_thr = new GRBLinExpr();
                                constr_thr.addTerm(1.0,zt);
                                constr_thr.addTerm(-1.0,y);
                                decomp_model.addConstr(constr_thr, GRB.LESS_EQUAL, 0.0, "");
                            }
                        }
                    }
                }
            }

            // ############################################################
            // ############################################################

            // Objective Function
            System.out.println("\nAdding objective function...");
            GRBLinExpr objective = new GRBLinExpr();

            // Distance between transitions
            for (Letter A : Sigma) {
                Trans = B.projectTransitions(A);

                for (Transition T : Trans) {
                    for (Transition U : Trans) {
                        
                        if (!T.equals(U)) {

                            i_max = Math.max(T.getSource().getEnumerator(),U.getSource().getEnumerator());
                            i_min = Math.min(T.getSource().getEnumerator(),U.getSource().getEnumerator());
                            l_max = Math.max(T.getTarget().getEnumerator(),U.getTarget().getEnumerator());
                            l_min = Math.min(T.getTarget().getEnumerator(),U.getTarget().getEnumerator());

                            for (int j = 0; j < factor_number; j++) {
                                objective.addTerm(0.5, VarSet.getY(i_min,i_max,l_min,l_max,j));
                            }
                        }
                    }
                }
            }

            decomp_model.setObjective(objective, GRB.MINIMIZE);

            // ############################################################
            // ############################################################

            // Set parameters and solve
            System.out.println("\n############################################################");
            System.out.println("############################################################\n");

            // decomp_model.set(GRB.IntParam.Method,3);
            // decomp_model.set(GRB.DoubleParam.TimeLimit,10.0);
            // decomp_model.write("debug.lp");
            // decomp_model.set(GRB.DoubleParam.TuneTimeLimit,100.0);
            // decomp_model.tune();
            // decomp_model.getTuneResult(decomp_model.get(GRB.IntAttr.TuneResultCount)-1);

            // decomp_model.set(GRB.DoubleParam.,100.0);
            decomp_model.optimize();

            System.out.println("\n############################################################");
            System.out.println("############################################################\n");

            System.out.println("Found encoding of distance " + decomp_model.get(GRB.DoubleAttr.ObjVal) + ".");

            // ############################################################
            // ############################################################

            // Draw encoding out of solution
            ArrayList<Integer> minDistEncoding = extractEncoding(s,VarSet);

            // Apply encoding to the automaton
            for (State Q : Stateset) {
                Q.setIndex(minDistEncoding.get(Q.getEnumerator()));
            }

            decomp_model.dispose();
            environ.dispose();
        }
        catch (GRBException e) {
            System.out.println(e.getMessage());
        }
    }

    public void minDistEncodingINST(Autom B){
        // Create ILP for finding a minimal distance encoding
        try{

            System.out.println("\n############################################################");
            System.out.println("############################################################\n");
            System.out.println("Set up Gurobi Model...");

            GRBEnv environ = new GRBEnv("decomposition.log");
            GRBModel decomp_model = new GRBModel(environ);

            LinkedHashSet<State> Stateset = B.getStates();
            LinkedHashSet<Letter> Sigma = B.getAlphabet();
            LinkedHashSet<Transition> Trans;
            int s = Stateset.size();

            // ############################################################
            // ############################################################

            // Add variables to the ILP
            System.out.println("\nAdding variables...");
            GRBVarSet VarSet = new GRBVarSet(s,factor_number);

            // X-Variables
            for (int i = 0; i < s; i++) {
                for (int j = 0; j < factor_number; j++) {
                    GRBVar x = decomp_model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x[" + i + "][" + j + "]");
                    VarSet.addX(i,j,x);
                }
            }

            // Z-Variables
            for (int i = 0; i < s; i++) {
                for (int ipr = 0; ipr < s; ipr++) {
                    if (ipr > i) {

                        for (int j = 0; j < factor_number; j++) {
                            GRBVar z = decomp_model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "z[" + i + "][" + ipr + "][" + j + "]");
                            VarSet.addZ(i,ipr,j,z);
                        }
                    }
                }
            }

            // M-Variables
            for (Letter A : Sigma) {
                GRBVar m = decomp_model.addVar(0.0, 160.0, 1.0, GRB.CONTINUOUS, "M[" + A.getSymb() + "]");
                VarSet.addM(A,m);
            }

            decomp_model.update();

            // ############################################################
            // ############################################################

            // Constraints
            System.out.println("\nAdding constraints...");

            // Absolute Value Constraints
            GRBLinExpr constr_one, constr_two, constr_thr, constr_fou;
            GRBVar x,xpr,z,L;
            for (int i = 0; i < s; i++) {
                for (int ipr = 0; ipr < s; ipr++) {

                    if (ipr > i) {
                        for (int j = 0; j < factor_number; j++) {

                            x = VarSet.getX(i,j);
                            xpr = VarSet.getX(ipr,j);
                            z = VarSet.getZ(i,ipr,j);

                            constr_one = new GRBLinExpr();
                            constr_one.addTerm(1.0,x);
                            constr_one.addTerm(1.0,xpr);
                            constr_one.addTerm(-1.0,z);
                            decomp_model.addConstr(constr_one, GRB.GREATER_EQUAL, 0.0, "");

                            constr_two = new GRBLinExpr();
                            constr_two.addTerm(1.0,x);
                            constr_two.addTerm(-1.0,xpr);
                            constr_two.addTerm(-1.0,z);
                            decomp_model.addConstr(constr_two, GRB.LESS_EQUAL, 0.0, "");

                            constr_thr = new GRBLinExpr();
                            constr_thr.addTerm(-1.0,x);
                            constr_thr.addTerm(1.0,xpr);
                            constr_thr.addTerm(-1.0,z);
                            decomp_model.addConstr(constr_thr, GRB.LESS_EQUAL, 0.0, "");

                            constr_fou = new GRBLinExpr();
                            constr_fou.addTerm(-1.0,x);
                            constr_fou.addTerm(-1.0,xpr);
                            constr_fou.addTerm(-1.0,z);
                            decomp_model.addConstr(constr_fou, GRB.GREATER_EQUAL, -2.0, "");
                        }
                    }
                }
            }

            // Injective Mapping Constraints
            GRBLinExpr constr_inj;
            for (int i = 0; i < s; i++) {
                for (int ipr = 0; ipr < s; ipr++) {

                    if(ipr > i) {
                        constr_inj = new GRBLinExpr();

                        for (int j = 0; j < factor_number; j++) {
                            constr_inj.addTerm(1.0, VarSet.getZ(i,ipr,j));
                        }

                        decomp_model.addConstr(constr_inj, GRB.GREATER_EQUAL, 1.0, "");
                    }
                }
            }

            // OR Constraints
            GRBVar zs,zt;
            GRBLinExpr constr_bound;
            int i_min, i_max, l_min, l_max;
            for (Letter A : Sigma) {
                Trans = B.projectTransitions(A);
                constr_bound = new GRBLinExpr();

                for (Transition T : Trans) {
                    for (Transition U : Trans) {
                        
                        if (!T.equals(U)) {

                            for (int j = 0; j < factor_number; j++) {

                                i_min = Math.min(T.getSource().getEnumerator(),U.getSource().getEnumerator());
                                i_max = Math.max(T.getSource().getEnumerator(),U.getSource().getEnumerator());
                                l_min = Math.min(T.getTarget().getEnumerator(),U.getTarget().getEnumerator());
                                l_max = Math.max(T.getTarget().getEnumerator(),U.getTarget().getEnumerator());

                                zs = VarSet.getZ(i_min,i_max,j);
                                zt = VarSet.getZ(l_min,l_max,j);

                                constr_bound.addTerm(1.0,zs);
                                constr_bound.addTerm(1.0,zt);
                            }
                        }
                    }
                }

                constr_bound.addTerm(-1.0,VarSet.getM(A));
                decomp_model.addConstr(constr_bound, GRB.LESS_EQUAL, 0.0, "");
            }

            // ############################################################
            // ############################################################

            GRBExpr obj = decomp_model.getObjective();
            decomp_model.setObjective(obj, GRB.MINIMIZE);

            // ############################################################
            // ############################################################

            // Set parameters and solve
            System.out.println("\n############################################################");
            System.out.println("############################################################\n");

            // decomp_model.set(GRB.IntParam.Method,3);
            // decomp_model.set(GRB.DoubleParam.TimeLimit,10.0);
            // decomp_model.write("debug.lp");
            // decomp_model.set(GRB.DoubleParam.TuneTimeLimit,100.0);
            // decomp_model.tune();
            // decomp_model.getTuneResult(decomp_model.get(GRB.IntAttr.TuneResultCount)-1);

            //decomp_model.set(GRB.IntParam.SimplexPricing,2);
            //decomp_model.set(GRB.IntParam.MIPFocus,3);

            // decomp_model.set(GRB.DoubleParam.,100.0);
            decomp_model.optimize();

            System.out.println("\n############################################################");
            System.out.println("############################################################\n");

            System.out.println("Found encoding of distance " + decomp_model.get(GRB.DoubleAttr.ObjVal) + ".");

            // ############################################################
            // ############################################################

            // Draw encoding out of solution
            ArrayList<Integer> minDistEncoding = extractEncoding(s,VarSet);

            // Apply encoding to the automaton
            for (State Q : Stateset) {
                Q.setIndex(minDistEncoding.get(Q.getEnumerator()));
            }

            decomp_model.dispose();
            environ.dispose();
        }
        catch (GRBException e) {
            System.out.println(e.getMessage());
        }
    }

    public void minDistEncodingMAXSAT(Autom B){
        // Create ILP for finding a minimal distance encoding
        try{

            PrintWriter out = new PrintWriter("minDistance.wcnf");

            System.out.println("\n############################################################");
            System.out.println("############################################################\n");
            System.out.println("Set up Formula...");

            // Header of the File
            out.println("c *************************************");
            out.println("c * MaxSAT query for minimal distance *");
            out.println("c *************************************");

            // Specification of MaxSAT query
            int s = B.getStateSize();
            int subs = (int) Math.pow(2,factor_number);

            int nbvar = s*factor_number;
            int nbclauses = ((s-1)*s / 2)*subs;
            LinkedHashSet<Transition> Trans = B.getTransitions();
            for (Transition T : Trans) {
                for (Transition U : Trans) {
                    if (T.getLabel().equals(U.getLabel())) nbclauses++;
                }
            }

            int top = nbclauses + 1;
            out.println("p wcnf " + nbvar + " " + nbclauses + " " + top);

            // Hard clauses
            for (int i = 0; i < s; i++) {
                for (int ipr = 0; ipr < s; ipr++) {
                    if (ipr > i) {
                        
                        for (int J = 0; J < subs; J++) {
                            //System.out.println("This is J: " + J);
                            // Weight of the clause
                            out.print(top);

                            for (int j = 0; j < factor_number; j++) {
                                //System.out.println("The " + j + "th bit of " + J + " is: " + ((J & (1 << j)) >> j));

                                if (((J & (1 << j)) >> j) == 1) {
                                    // j is in J, add (x_ij, x_iprj)
                                    out.print(" " + (i*factor_number + j + 1) + " " + (ipr*factor_number + j + 1));
                                } else {
                                    // j is not in J, add (neg x_ij, neg x_iprj)
                                    out.print(" -" + (i*factor_number + j + 1) + " -" + (ipr*factor_number + j + 1));
                                }
                            }

                            out.print(" 0\n");
                        }
                    }
                }
            }

            // Soft clauses
            int i,ipr,l,lpr;
            for (Transition T : Trans) {
                for (Transition U : Trans) {
                    if (T.getLabel().equals(U.getLabel())) {

                        i = T.getSource().getEnumerator();
                        ipr = U.getSource().getEnumerator();
                        l = T.getTarget().getEnumerator();
                        lpr = U.getTarget().getEnumerator();

                        for (int j = 0; j < factor_number; j++) {
                            
                            out.println("1 -" + (i*factor_number + j + 1) + " " + (ipr*factor_number + j + 1) + " 0");
                            out.println("1 " + (i*factor_number + j + 1) + " -" + (ipr*factor_number + j + 1) + " 0");
                            out.println("1 -" + (l*factor_number + j + 1) + " " + (lpr*factor_number + j + 1) + " 0");
                            out.println("1 " + (l*factor_number + j + 1) + " -" + (lpr*factor_number + j + 1) + " 0");
                        }
                    }
                }
            }

            out.close();
        }

        catch (IOException e) {
            System.err.println("Something bad happened!");
        }
    }


    private ArrayList<Integer> extractEncoding(int s, GRBVarSet Solution) throws GRBException{

        ArrayList<Integer> encoding = new ArrayList<Integer>(s);
        int enc;
        for (int i = 0; i < s; i++) {
            enc = 0;
            for (int j = 0; j < factor_number; j++) {
                enc += Solution.getX(i,j).get(GRB.DoubleAttr.X)*Math.pow(2,j);
            }

            encoding.add(i,enc);
        }

        return encoding;
    }

    public int getFactorNumber(){
        // Returns the number of factors stored
        return prime_factors.size();
    }

    public int getComponent(int index){
        // Each index of a factor corresponds to a unique position in the encoding. The method returns that position
        return index - prime_factors.indexOf(prime_factors.get(index));
    }

    public int getPrime(int index){
        // Returns the demanded prime from the list of primes
        return prime_factors.get(index);
    }

    public int getExponent(int prime){
        // Returns the exponent belonging to the given prime
        return exponents.get(prime);
    }

    public static int getEntry(int index, int entry, int base, int exp){
        // Returns the "entry"-th component of (index modulo base^exp) over base "base"
        int num = index % (int) Math.pow(base,exp);
        for (int i = 0; i < entry; i++) {
            num /= base;
        }
        return (num % base);
    }

    public int distance(Autom A){
        // Calculate the distance of the encoding
        LinkedHashSet<State> Stateset = A.getStates();
        LinkedHashSet<Transition> Trans = A.getTransitions();
        int dist = 0;

        for (Transition T : Trans) {
            for (Transition R : Trans) {
                dist += distanceTransitions(T,R);
            }
        }

        return dist / 2;
    }

    private int distanceStates(int p, int q){
        // Returns the distance between two states
        int dist = 0;
        int j,prime,exp;
        for (int i = 0; i < factor_number; i++) {
            j = getComponent(i);
            prime = getPrime(i);
            exp = getExponent(prime);
            if (getEntry(p,j,prime,exp) != getEntry(q,j,prime,exp)) dist++;
        }

        // Integer.bitCount(~((~p|q) & (~q|p)));
        return dist;
    }

    private int distanceEquivalentPositions(int p1, int q1, int p2, int q2){
        // Returns the number of positions i, where p1_i != q1_i AND p2_i != q2_i
        int dist = 0;
        int j,prime,exp;
        for (int i = 0; i < factor_number; i++) {
            j = getComponent(i);
            prime = getPrime(i);
            exp = getExponent(prime);
            if ((getEntry(p1,j,prime,exp) != getEntry(q1,j,prime,exp)) && (getEntry(p2,j,prime,exp) != getEntry(q2,j,prime,exp))) dist++;
        }

        // Integer.bitCount((~((~p1|p2) & (~p2|p1))) & (~((~q1|q2) & (~q2|q1))));
        return dist;
    }

    private int distanceTransitions(Transition T, Transition R){
        // Returns the distance between two transitions
        if (!T.getLabel().equals(R.getLabel())) return 0;
        return distanceStates(T.getSource().getIndex(),R.getSource().getIndex()) 
                + distanceStates(T.getTarget().getIndex(),R.getTarget().getIndex())
                - distanceEquivalentPositions(T.getSource().getIndex(),R.getSource().getIndex(),T.getTarget().getIndex(),R.getTarget().getIndex());
    }

    public void minConflictGreedy(Autom B){
        LinkedHashSet<State> Stateset = B.getStates();
        int s = Stateset.size();
        HashMap<State,Integer> CurrentEnc = new HashMap<State,Integer>(s);
        HashMap<State,Integer> minEnc = null;
        State minState = null;
        int local_min = -1;
        int global_min = -1;
        int currentConfCount;
        
        for (State Q : Stateset) {
            // Start by assigning a state the value 0
            CurrentEnc.put(Q,0);

            for (int index = 1; index < s; index++) {
                minState = null;

                for (State P : Stateset) {
                    if (minState == null && CurrentEnc.get(P) == null) {
                        // P is the first state that was not assigned a value in the current encoding
                        CurrentEnc.put(P,index);
                        local_min = partialConfCount(B,CurrentEnc);
                        minState = P;
                        continue;
                    }

                    if (CurrentEnc.get(P) == null) {
                        // P was not assigned a value in the current encoding
                        // Replace minState by P in the encoding and test

                        CurrentEnc.put(minState,null);
                        CurrentEnc.put(P,index);
                        currentConfCount = partialConfCount(B,CurrentEnc);

                        if (currentConfCount < local_min) {
                            // Better State found for distance of encoding
                            minState = P;
                            local_min = currentConfCount;

                        } else {
                            // State is not better, undo changes
                            CurrentEnc.put(P,null);
                            CurrentEnc.put(minState,index);
                        }
                    }
                }
            }

            // Particular encoding found
            if (minEnc == null) {
                // Minimal encoding not set yet
                minEnc = new HashMap<State,Integer>(CurrentEnc);
                global_min = partialConfCount(B,minEnc);
            }

            if (partialDistance(B,CurrentEnc) < global_min) {
                // Compare distances
                minEnc = new HashMap<State,Integer>(CurrentEnc);
                global_min = partialConfCount(B,minEnc);
            }

            System.out.println("The current min is " + global_min);

            CurrentEnc = new HashMap<State,Integer>(s);
        }

        applyEncoding(B,minEnc);
    }

    private int partialConfCount(Autom B, HashMap<State,Integer> Enc){
        ArrayList<ConcretFunc> Conc_Func = new ArrayList<ConcretFunc>();

        for (int i = 0; i < factor_number; i++) {
            ConcretFunc Gamma = new ConcretFunc(getPrime(i),B.getAlphabetSize());
            Conc_Func.add(Gamma);
        }

        LinkedHashSet<Transition> Ground_Trans = B.getTransitions();
        LinkedHashSet<Letter> Sigma = B.getAlphabet();
        int prime = -1;
        int l = 0;
        int j, exp, source_entry, target_entry;

        for (Letter A : Sigma) {
            A.setIndex(l);
            l++;
        }

        for (int i = 0; i < factor_number; i++){
            j = getComponent(i);
            prime = getPrime(i);
            exp = getExponent(prime);

            for (Transition T : Ground_Trans) {
                if (Enc.get(T.getSource()) == null || Enc.get(T.getTarget()) == null) {
                    // Not assigned an encoding yet
                } else {
                    source_entry = getEntry(Enc.get(T.getSource()),j,prime,exp);
                    target_entry = getEntry(Enc.get(T.getTarget()),j,prime,exp);
                    Conc_Func.get(i).add(T, source_entry, target_entry);

                 //   System.out.print("Adding Transition (" + T.getSource().getName() + "," + T.getLabel().getSymb() + "," + T.getTarget().getName() + ") to function ");
                 //   System.out.print("Gamma_" + source_entry + "_" + target_entry + "_" + T.getLabel().getIndex() + " in prime " + prime + ", occurrance " + j + ". \n");
                }
            }
        }

        int n = 1;
        int count = 0;

        for(int i = 0; i < factor_number; i++) {
            n *= getPrime(i);
        }

        // Go through each potential A-transition in the product and check for spuriousness
        for (Letter A : Sigma) {
            for (int j_s = 0; j_s < n; j_s++) {
                for (int j_t = 0 ; j_t < n; j_t++) {
                    if (isPartialSpurious(Conc_Func,A,j_s,j_t)) {
                        count++;
                    }
                }             
            }
        }

        return count;
    }

    private boolean isPartialSpurious(ArrayList<ConcretFunc> Conc_Func, Letter A, int source, int target){

        LinkedHashSet<Transition> Gamma;
        LinkedHashSet<Transition> Intersection = new LinkedHashSet<Transition>();
        int prime, j, exp, source_entry, target_entry;

        for (int i = 0; i < factor_number; i++) {
            j = getComponent(i);
            prime = getPrime(i);
            exp = getExponent(prime);

            // Select j-th entry of (source mod prime^exp) in base prime
            source_entry = getEntry(source,j,prime,exp);
            target_entry = getEntry(target,j,prime,exp);

            // Select the correct concretization function from i-th factor
            Gamma = Conc_Func.get(i).get(source_entry,target_entry,A);
            if (Gamma.isEmpty()) return false;

            // Intersect
            if (i == 0) {
                Intersection = new LinkedHashSet<Transition>(Gamma);
            } else {
                Intersection.retainAll(Gamma);
            }
        }

        return Intersection.isEmpty();
    }




}