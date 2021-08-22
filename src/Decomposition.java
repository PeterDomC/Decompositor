/* Decomposition */
import java.util.*;

public class Decomposition{

    final Autom B; // Automaton to decompose
    final LinkedHashSet<Letter> Ground_Alphabet; // Initially, the alphabet of B. But the alphabet of B gets changed later
    final StateEncoding Enc; // Encoding of the states
    final ArrayList<ConcretFunc> Conc_Func; // Concretization Functions, one for each factor
    final ArrayList<Autom> Factors; // Factors that decompose B

    Decomposition(Autom A, String mode){
        B = A;
        Ground_Alphabet = new LinkedHashSet<Letter>(B.getAlphabet());

        Enc = new StateEncoding(B.getStateSize(),mode);

        int factor_number = Enc.getFactorNumber();
        Conc_Func = new ArrayList<ConcretFunc>(factor_number);
        allocateConcretFunction();

        Factors = new ArrayList<Autom>(factor_number);
    }

    public void decompose(String mode,boolean optimizeDist){
        Enc.initEncoding(B);
        
        if (optimizeDist) {
            Enc.minDistEncodingINST(B);
        } else {
            Enc.stdEncoding(B);
        }
        System.out.println("Found Encoding of Distance " + Enc.distance(B));

        initConcretFunction();
        System.out.println("Concretization Functions set up.\n");

        System.out.println("Finding spurious transitions and relabeling.\n");
        int done = 0;
        
        initBar(Ground_Alphabet.size());

        int conf_count = 0;
        for (Letter A : Ground_Alphabet) {

            Conflict Conf = createConflict(A);
            conf_count += Conf.getNrConf();

            relabel(Conf,A,mode);

            done++;
            increaseBar(Ground_Alphabet.size(),done);
        }
        System.out.println("Number of conflicts is " + conf_count);

        System.out.println("Relabeling complete.\n");

        System.out.println("Building factors.\n");
        initFactors();

        System.out.println("Decomposition complete.\n");
    }

    private void allocateConcretFunction(){
        int sigma_size = B.getAlphabetSize();
        int factor_number = Enc.getFactorNumber();

        for (int i = 0; i < factor_number; i++) {
            ConcretFunc Gamma = new ConcretFunc(Enc.getPrime(i),sigma_size);
            Conc_Func.add(Gamma);
        }
    }

    private void initConcretFunction(){
        LinkedHashSet<Transition> Ground_Trans = B.getTransitions();
        int factor_number = Enc.getFactorNumber();
        int prime = -1;
        int l = 0;
        int j, exp, source_entry, target_entry;

        for (Letter A : Ground_Alphabet) {
            A.setIndex(l);
            l++;
        }

        for (int i = 0; i < factor_number; i++){
            j = Enc.getComponent(i);
            prime = Enc.getPrime(i);
            exp = Enc.getExponent(prime);

            for (Transition T : Ground_Trans) {
                // Add transitions to the correct concretization functions
                source_entry = Enc.getEntry(T.getSource().getIndex(),j,prime,exp);
                target_entry = Enc.getEntry(T.getTarget().getIndex(),j,prime,exp);
                Conc_Func.get(i).add(T, source_entry, target_entry);
                //System.out.print("Adding Transition (" + T.getSource().getName() + "," + T.getLabel().getSymb() + "," + T.getTarget().getName() + ") to function ");
                //System.out.print("Gamma_" + source_entry + "_" + target_entry + "_" + T.getLabel().getIndex() + " in prime " + prime + ", occurrance " + j + ". \n");
            }
        }       
    }

    public ConcretFunc getConcretFunc(int index){
        //
        return Conc_Func.get(index);
    }

    private Conflict createConflict(Letter A){
        Conflict Conf = new Conflict(A,B.projectTransitions(A),Enc.getFactorNumber());
        int factor_number = Enc.getFactorNumber();
        int count = 0;

        // The number of potential states in the product of the factors
        int n = 1; 
        for(int i = 0; i < factor_number; i++) {
            n *= Enc.getPrime(i);
        }

        // Go through each potential A-transition in the product and check for spuriousness
        for (int j_s = 0; j_s < n; j_s++) {
            for (int j_t = 0 ; j_t < n; j_t++) {
                if (isSpurious(A,j_s,j_t)) {
                    Conf.addConflict(this,Enc,j_s,j_t);
                    count++;
                }
            }             
        }

        //    System.out.println("Conflicts for " + A.getSymb() + " set up -> " + count + " many.");
        return Conf;
    }

    private boolean isSpurious(Letter A, int source, int target){
        LinkedHashSet<Transition> Gamma;
        LinkedHashSet<Transition> Intersection = new LinkedHashSet<Transition>();
        int factor_number = Enc.getFactorNumber();
        int prime, j, exp, source_entry, target_entry;

        for (int i = 0; i < factor_number; i++) {
            j = Enc.getComponent(i);
            prime = Enc.getPrime(i);
            exp = Enc.getExponent(prime);

            // Select j-th entry of (source mod prime^exp) in base prime
            source_entry = Enc.getEntry(source,j,prime,exp);
            target_entry = Enc.getEntry(target,j,prime,exp);

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

    private void relabel(Conflict Conf, Letter A, String mode){
        HashMap<Integer,Letter> Relabel;
        LinkedHashSet<Transition> Trans_A;

        Conf.resolve(mode);
        Relabel = Conf.getSolution();

        //    System.out.println("Conflicts for " + A.getSymb() + " resolved");
        //    System.out.println("Applying relabeling for " + A.getSymb() + ".");

        Trans_A = new LinkedHashSet<Transition>(B.projectTransitions(A));
        B.removeLetter(A);

        for (Transition T : Trans_A) {
            B.addLetter(Relabel.get(T.getIndex()));
            B.removeTransition(T);

            Transition T_relabel = new Transition(T.getSource(), T.getTarget(), Relabel.get(T.getIndex()));
            B.addTransition(T_relabel);
        }
    }

    private void initFactors(){
        LinkedHashSet<Letter> Sigma = B.getAlphabet();
        LinkedHashSet<Transition> Trans = B.getTransitions();
        int factor_number = Enc.getFactorNumber();
        int prime, j, exp, source_entry, target_entry;
        int count = 0;

        for(int i = 0; i < factor_number; i++) {
            prime = Enc.getPrime(i);
            j = Enc.getComponent(i);
            exp = Enc.getExponent(prime);

            LinkedHashSet<State> Factor_States = new LinkedHashSet<State>(prime);
            for (int l = 0; l < prime; l++) {
                State q = new State(Integer.toString(l),false,false); //TODO: Initial & Final state
                Factor_States.add(q);
            }
            Autom Factor = new Autom(Sigma,Factor_States);

            for (Transition T : Trans) {
                source_entry = Enc.getEntry(T.getSource().getIndex(),j,prime,exp);
                target_entry = Enc.getEntry(T.getTarget().getIndex(),j,prime,exp);
                Transition Factor_Trans = new Transition(Factor.getStateByName(Integer.toString(source_entry)),Factor.getStateByName(Integer.toString(target_entry)),T.getLabel());
                Factor.addTransition(Factor_Trans);
            }

            Factors.add(Factor);
            System.out.println("Factor " + i + " has " + Factor.getStateSize() + " states and " + Factor.getTransSize() + " transitions.");
            //Factor.print();
            count += Factor.getTransSize();
            
        }
        System.out.println("We have to store " + count + " transitions in total.");
    }

    private static void initBar(int n){
        String bar = "Progress: [" + String.format("%-" + n + "s", "") + "]\r";
        System.out.print(bar);
    }

    private static void increaseBar(int n, int done){
        String bar;

        if (done == n) {
            bar = "Progress: [" + String.format("%-" + n + "s","").replace(' ','=') + "]\n";
        } else {
            bar = "Progress: [" + String.format("%-" + done + "s","").replace(' ','=') + String.format("%-" + (n-done) + "s", "") + "]\r";
        }
        System.out.print(bar);
    }

/*
    // TESTED
    // Binary State is initial if it encodes an initial state
    boolean isInit(int i, int bit, LinkedHashSet<State> Stateset){
        for (State Q : Stateset) {
            if (Q.isInit() && Q.getBit(i) == bit) return true;
        }
        return false;
    }

    // TESTED
    // Binary State is final if it encodes a final state
    boolean isFinal(int i, int bit, LinkedHashSet<State> Stateset){
        for (State Q : Stateset) {
            if (Q.isFinal() && Q.getBit(i) == bit) return true;
        }
        return false;
    }

    void printFactorSizes(){
        int n = Factors.size();
        for (int i = 0; i < n; i++) {
            System.out.println("Factor " + i + " has " + Factors.get(i).stateSize() + " many states and " + Factors.get(i).transSize() + " transitions.");
        }
    }

/*
    void printRelabel(){
        String out = "";
        LinkedHashSet<Transition> Trans = B.getTransitions();
        for (Transition T : Trans) {
            out = out + "(" + T.getSource().getName() + "," + T.getLabel().getSymb() + "," + T.getTarget().getName() + ") -> (" + Relabel.get(T).getSymb() + "," + Relabel.get(T).getFlag() + ") \n";
        }
        System.out.println(out);
    }
*/
/*
    int maxFactor(){
        int out = 0;

        for (int i = 0; i < k; i++) {
            out = Math.max(out,Factors.get(i).transSize());
        }

        return out;
    }
*/
}


















