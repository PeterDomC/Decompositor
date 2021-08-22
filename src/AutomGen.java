/* Random Automata Generator */
import java.util.*;

public class AutomGen{
    
    int sigmaSize;
    int stateSize;
    int transSize;
    Autom Gen;

    AutomGen(int in_sigmaSize, int in_stateSize, int in_transSize){
        sigmaSize = in_sigmaSize;
        stateSize = in_stateSize;
        transSize = in_transSize;

        LinkedHashSet<Letter> Sigma = new LinkedHashSet<Letter>();
        for (int i = 0; i < sigmaSize; i++) {
            Letter A = new Letter("a" + i, 0);
            Sigma.add(A);
        }

        LinkedHashSet<State> Stateset = new LinkedHashSet<State>();
        for (int i = 0; i < stateSize; i++) {
            State Q = new State("P_" + i, false, false);
            Stateset.add(Q);
        }

        Gen = new Autom(Sigma, Stateset);
        for (int i = 0; i < transSize; i++) {
            Transition T = new Transition(getRandomState(Stateset), getRandomState(Stateset), getRandomLetter(Sigma));
            Gen.addTransition(T);
        }

        System.out.println("Generated random automaton with " + sigmaSize + " symbols, " + stateSize + " states, and " + Gen.transSize() + " transitions.");
    }

    State getRandomState(LinkedHashSet<State> MyStateSet){
        int size = MyStateSet.size();
        int item = new Random().nextInt(size);
        int i = 0;

        for (State Q : MyStateSet) {
            if (i == item) return Q;
            i++;
        }

        return null;
    }

    Letter getRandomLetter(LinkedHashSet<Letter> MyAlphabet){
        int size = MyAlphabet.size();
        int item = new Random().nextInt(size);
        int i = 0;

        for (Letter A : MyAlphabet) {
            if (i == item) return A;
            i++;
        }

        return null;
    }

    Autom getGen(){
        return Gen;
    }
}