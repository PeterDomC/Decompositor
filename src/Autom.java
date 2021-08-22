/* Automaton */
import java.util.*;

public class Autom{

    private final LinkedHashSet<Letter> Sigma;
    private final LinkedHashSet<State> Stateset;
    private final LinkedHashSet<Transition> Trans;

    public Autom(LinkedHashSet<Letter> in_sigma, LinkedHashSet<State> in_stateset){
        // Constructor
        Sigma = in_sigma;
        Stateset = in_stateset;
        Trans = new LinkedHashSet<Transition>();
    }

    public State getStateByName(String name){
        // Returns the state with the given name
        for (State Q : Stateset) {
            if (Q.getName().equals(name)) return Q;
        }

        return null;
    }

    public Letter getLetterBySymb(String symb){
        // Returns the letter with the given symbol
        for (Letter A : Sigma) {
            if (A.getSymb().equals(symb)) return A;
        }

        return null;
    }

    public LinkedHashSet<Letter> getAlphabet(){
        // Returns the alphabet
        return Sigma;
    }

    public LinkedHashSet<State> getStates(){
        // Returns the set of states
        return Stateset;
    }

    public LinkedHashSet<Transition> getTransitions(){
        // Returns the set of transitions
        return Trans;
    }

    public int getAlphabetSize(){
        // Returns the size of the alphabet
        return Sigma.size();
    }

    public int getStateSize(){
        // Returns the nmber of states
        return Stateset.size();
    }

    public int getTransSize(){
        // Returns the number of transitions
        return Trans.size();
    }

    public LinkedHashSet<Transition> projectTransitions(Letter A){
        // Returns the transitions projected to the given letter
        // Do not change the actual set of transitions, only work on a copy
        LinkedHashSet<Transition> out = new LinkedHashSet<Transition>(Trans);
        for (Iterator<Transition> it = out.iterator(); it.hasNext();) {
            Transition T = it.next();
            if (!A.equals(T.getLabel())) it.remove();
        }

        return out;
    }

    public void addLetter(Letter A){
        // Add the given letter to the automaton
        Sigma.add(A);
    }

    public void addState(State Q){
        // Add the given state to the automaton
        Stateset.add(Q);
    }

    public void addTransition(Transition T){
        // Adds a given transition if the source, the target state, and the label are stored in the automaton
        if (Stateset.contains(T.getSource()) && Stateset.contains(T.getTarget()) && Sigma.contains(T.getLabel())) Trans.add(T);
    }

    public void removeLetter(Letter A){
        // Remove the given letter from the automaton
        Sigma.remove(A);
    }

    public void removeTransition(Transition T){
        // Remove the given transition from the automaton
        Trans.remove(T);
    }

    public void setFinal(State P){
        // Set the given state to be final
        for (State Q : Stateset) {
            if (Q.equals(P)) Q.setFinal();
        }
    }

    public void setInit(State P){
        // Set the given state to be initial
        for (State Q : Stateset) {
            if (Q.equals(P)) Q.setInit();
        }
    }
    /* Maybe change input to HashMap(State,Integer) to avoid confusion */
    public void permuteStates(ArrayList<Integer> Perm){
        // Permutes the states by the given encoding
        int i = 0;
        for (State Q : Stateset) {
            Q.setIndex(Perm.get(i));
            i++;
        }
    }

    public void print(){
        // Simple print method
        String out = String.format("%-5s", "");
        String out_row = "";
        String out_initFin = "";
        int N = 5*Sigma.size() + 1;

        for (State Q : Stateset) {
            out_initFin = "";
            if (Q.isInit()) out_initFin = "(I)";
            if (Q.isFinal()) out_initFin = out_initFin + "(F)";
            out = out + String.format("%-" + N + "s", Q.getName() + out_initFin);
        }

        out = out + "\n";

        for (State Q : Stateset) {

            out = out + String.format("%-5s", Q.getName());

            for (State P : Stateset) {

                for (Transition T : Trans) {
                    if (T.getSource().equals(Q) && T.getTarget().equals(P)) out_row = out_row  + T.getLabel().getSymb() + " ";
                }

                out_row = String.format("%-" + N + "s", out_row);
                out = out + out_row;
                out_row = "";
            }

            out = out + "\n";
        }

        System.out.println(out);
    }

    public Autom copy(){
        // Creates a copy of the automaton
        LinkedHashSet<Letter> Gamma = new LinkedHashSet<Letter>(Sigma);
        LinkedHashSet<State> Q = new LinkedHashSet<State>(Stateset);
        Autom B = new Autom(Gamma,Q);
        for (Transition T : Trans) {
            B.addTransition(T);
        }

        return B;
    }
}



