/* Examples */
import java.util.*;

public class Example {
    
    Autom Automaton;

    public Autom getAutom(){
        return Automaton;
    }

    Example(String shape, int index, int length, int opt_1, int opt_2){
        if (shape.equals("random")) {
            /* Random Automaton */
        }

        if (shape.equals("cycle")) {
            /* Cycle Automaton */
            switch (index) {
                case 1: Automaton = cycle_example_1(length); break;
                case 2: Automaton = cycle_example_2(length); break;
                case 3: Automaton = cycle_example_3(length,opt_1); break;
                default: break;
            }
        }

        if (shape.equals("counter")) {
            /* Counter Automaton */
            switch (index) {
                case 1: Automaton = counter_example_1(length); break;
                case 2: Automaton = counter_example_2(length,opt_1); break;
                case 3: Automaton = counter_example_3(length); break;
                case 4: Automaton = counter_example_4(length); break;
                default: break;
            }
        }

        if (shape.equals("misc")) {
            /* Misc Automaton */
            switch (index) {
                case 1: Automaton = misc_example_1(); break;
                default: break;
            }
        }
    }

    private Autom cycle_example_1(int length){
        // Simple Cycle
        if (length < 2) {
            return null;
        }

        int i;
        LinkedHashSet<Letter> Sigma = new LinkedHashSet<Letter>();
        LinkedHashSet<State> Q = new LinkedHashSet<State>();
        Autom NFA = new Autom(Sigma,Q);

        Letter A = new Letter("a");
        NFA.addLetter(A);

        State init = new State("p0",true,true);
        NFA.addState(init);

        for (i = 1; i < length; i++) {
            State q = new State("p" + i,false,false);
            NFA.addState(q);
        }

        for (i = 0; i < length-1; i++) {
            Transition t = new Transition(NFA.getStateByName("p" + i),NFA.getStateByName("p" + (i+1)),A);
            NFA.addTransition(t);
        }
        Transition u = new Transition(NFA.getStateByName("p" + (length-1)),NFA.getStateByName("p0"),A);
        NFA.addTransition(u);

        return NFA;
    }

    private Autom cycle_example_2(int length){
        /* Cycle with two letters */
        if (length < 2) {
            return null;
        }

        length = 2*length;
        int i;
        LinkedHashSet<Letter> Sigma = new LinkedHashSet<Letter>();
        LinkedHashSet<State> Q = new LinkedHashSet<State>();
        Autom NFA = new Autom(Sigma,Q);

        Letter A = new Letter("a");
        Letter B = new Letter("b");
        NFA.addLetter(A);
        NFA.addLetter(B);

        State init = new State("p0",true,true);
        NFA.addState(init);

        for (i = 1; i < length; i++) {
            State q = new State("p" + i,false,false);
            NFA.addState(q);
        }

        for (i = 0; i < length; i = i+2) {
            Transition t = new Transition(NFA.getStateByName("p" + i),NFA.getStateByName("p" + (i+1)),A);
            Transition u = new Transition(NFA.getStateByName("p" + (i+1)),NFA.getStateByName("p" + (i+2)),B);
            NFA.addTransition(t);
            NFA.addTransition(u);
        }
        Transition u = new Transition(NFA.getStateByName("p" + (length-1)),NFA.getStateByName("p0"),B);
        NFA.addTransition(u);

        return NFA;
    }

    private Autom cycle_example_3(int length, int symbols){
        // Cycle with randomly distributed symbols
        if (length < 2) {
            return null;
        }

        int i,rflag;
        LinkedHashSet<Letter> Sigma = new LinkedHashSet<Letter>();
        LinkedHashSet<State> Q = new LinkedHashSet<State>();
        Autom NFA = new Autom(Sigma,Q);

        for (i = 0; i < symbols; i++) {
            Letter A = new Letter("a_" + i);
            NFA.addLetter(A);
        }

        State init = new State("p0",true,true);
        NFA.addState(init);

        for (i = 1; i < length; i++) {
            State q = new State("p" + i,false,false);
            NFA.addState(q);
        }

        for (i = 0; i < length-1; i++) {
            rflag = new Random().nextInt(symbols);
            Transition t = new Transition(NFA.getStateByName("p" + i),NFA.getStateByName("p" + (i+1)),NFA.getLetterBySymb("a_" + rflag));
            NFA.addTransition(t);
        }
        rflag = new Random().nextInt(symbols);
        Transition u = new Transition(NFA.getStateByName("p" + (length-1)),NFA.getStateByName("p0"),NFA.getLetterBySymb("a_" + rflag));
        NFA.addTransition(u);

        return NFA;
    }

    private Autom counter_example_1(int length){
        // Simple counter language
        if (length < 1) {
            return null;
        }

        int i;
        LinkedHashSet<Letter> Sigma = new LinkedHashSet<Letter>();
        LinkedHashSet<State> Q = new LinkedHashSet<State>();
        Autom NFA = new Autom(Sigma,Q);

        Letter A = new Letter("a");
        NFA.addLetter(A);

        State init = new State("p0",true,false);
        NFA.addState(init);

        for (i = 1; i < length; i++) {
            State q = new State("p" + i,false,false);
            NFA.addState(q);
        }

        State fin = new State("p" + i,false,true);
        NFA.addState(fin);

        for (i = 0; i < length; i++) {
            Transition t = new Transition(NFA.getStateByName("p" + i),NFA.getStateByName("p" + (i+1)),A);
            NFA.addTransition(t);
        }

        return NFA;
    }

    private Autom counter_example_2(int length, int symbols){
        // Counter with random symbols
        if (length < 1) {
            return null;
        }

        int i,rflag;
        LinkedHashSet<Letter> Sigma = new LinkedHashSet<Letter>();
        LinkedHashSet<State> Q = new LinkedHashSet<State>();
        Autom NFA = new Autom(Sigma,Q);

        for (i = 0; i < symbols; i++) {
            Letter A = new Letter("a_" + i);
            NFA.addLetter(A);
        }

        State init = new State("p0",true,false);
        NFA.addState(init);

        for (i = 1; i < length; i++) {
            State q = new State("p" + i,false,false);
            NFA.addState(q);
        }

        State fin = new State("p" + i,false,true);
        NFA.addState(fin);

        for (i = 0; i < length; i++) {
            rflag = new Random().nextInt(symbols);
            Transition t = new Transition(NFA.getStateByName("p" + i),NFA.getStateByName("p" + (i+1)),NFA.getLetterBySymb("a_" + rflag));
            NFA.addTransition(t);
        }

        return NFA;
    }

    private Autom counter_example_3(int length){
        /* Forked double counter with one letter */
        if (length < 1) {
            return null;
        }
        int length_1 = length + 1;
        int length_2 = length/2 + 3;

        int i;
        LinkedHashSet<Letter> Sigma = new LinkedHashSet<Letter>();
        LinkedHashSet<State> Q = new LinkedHashSet<State>();
        Autom NFA = new Autom(Sigma,Q);

        Letter A = new Letter("a");
        NFA.addLetter(A);

        State init = new State("p0",true,false);
        NFA.addState(init);

        for (i = 1; i < length_1; i++) {
            State p = new State("p" + i,false,false);
            NFA.addState(p);
        }

        State fin_p = new State("p" + i,false,true);
        NFA.addState(fin_p);

        for (i = 1; i < length_2; i++) {
            State q = new State("q" + i,false,false);
            NFA.addState(q);
        }

        State fin_q = new State("q" + i,false,true);
        NFA.addState(fin_q);

        for (i = 0; i < length_1; i++) {
            Transition t = new Transition(NFA.getStateByName("p" + i),NFA.getStateByName("p" + (i+1)),A);
            NFA.addTransition(t);
        }

        Transition pq = new Transition(NFA.getStateByName("p0"),NFA.getStateByName("q1"),A);
        NFA.addTransition(pq);

        for (i = 1; i < length_2; i++) {
            Transition t = new Transition(NFA.getStateByName("q" + i),NFA.getStateByName("q" + (i+1)),A);
            NFA.addTransition(t);
        }

        return NFA;
    }

    private Autom counter_example_4(int length){
        /* Forked double counter with two letters */
        if (length < 1) {
            return null;
        }
        int length_1 = length + 1;
        int length_2 = length/2 + 3;

        int i;
        LinkedHashSet<Letter> Sigma = new LinkedHashSet<Letter>();
        LinkedHashSet<State> Q = new LinkedHashSet<State>();
        Autom NFA = new Autom(Sigma,Q);

        Letter A = new Letter("a");
        Letter B = new Letter("b");
        NFA.addLetter(A);
        NFA.addLetter(B);

        State init = new State("p0",true,false);
        NFA.addState(init);

        for (i = 1; i < length_1; i++) {
            State p = new State("p" + i,false,false);
            NFA.addState(p);
        }

        State fin_p = new State("p" + i,false,true);
        NFA.addState(fin_p);

        for (i = 1; i < length_2; i++) {
            State q = new State("q" + i,false,false);
            NFA.addState(q);
        }

        State fin_q = new State("q" + i,false,true);
        NFA.addState(fin_q);

        for (i = 0; i < length_1; i++) {
            Transition t = new Transition(NFA.getStateByName("p" + i),NFA.getStateByName("p" + (i+1)),A);
            NFA.addTransition(t);
        }

        Transition pq = new Transition(NFA.getStateByName("p0"),NFA.getStateByName("q1"),B);
        NFA.addTransition(pq);

        for (i = 1; i < length_2; i++) {
            Transition t = new Transition(NFA.getStateByName("q" + i),NFA.getStateByName("q" + (i+1)),B);
            NFA.addTransition(t);
        }

        return NFA;
    }

    private Autom misc_example_1(){
        /* Small example with spuriousness */
        LinkedHashSet<Letter> Sigma = new LinkedHashSet<Letter>();
        LinkedHashSet<State> Q = new LinkedHashSet<State>();
        Autom NFA = new Autom(Sigma,Q);

        Letter A = new Letter("a");
        Letter B = new Letter("b");

        NFA.addLetter(A);
        NFA.addLetter(B);

        State p0 = new State("p0",true,false);
        State p1 = new State("p1",false,false);
        State p2 = new State("p2",false,false);
        State p3 = new State("p3",false,true);

        NFA.addState(p0);
        NFA.addState(p1);
        NFA.addState(p2);
        NFA.addState(p3);

        Transition t1 = new Transition(p0,p1,A);
        Transition t2 = new Transition(p1,p3,A);
        Transition t3 = new Transition(p3,p1,A);
        Transition t4 = new Transition(p0,p2,B);

        NFA.addTransition(t1);
        NFA.addTransition(t2);
        NFA.addTransition(t3);
        NFA.addTransition(t4);

        return NFA;
    }

}