/* Concretization Functions */
import java.util.*;

public class ConcretFunc{

    ArrayList<ArrayList<ArrayList<LinkedHashSet<Transition>>>> Conc_Func;

    ConcretFunc(int base, int sigma_size){
        // Initialize the Array
        Conc_Func = new ArrayList<ArrayList<ArrayList<LinkedHashSet<Transition>>>>(base);

        for (int i = 0; i < base; i++) {
            ArrayList<ArrayList<LinkedHashSet<Transition>>> target_letter_arr = new ArrayList<ArrayList<LinkedHashSet<Transition>>>(base);
            
            for(int j = 0; j < base; j++) {
                ArrayList<LinkedHashSet<Transition>> letter_arr = new ArrayList<LinkedHashSet<Transition>>(sigma_size);

                for (int k = 0; k < sigma_size; k++) {
                    LinkedHashSet<Transition> trans_set = new LinkedHashSet<Transition>();
                    letter_arr.add(trans_set);
                }

                target_letter_arr.add(letter_arr);
            }

            Conc_Func.add(target_letter_arr);
        }   
    }

    void add(Transition T, int source, int target){
        Conc_Func.get(source).get(target).get(T.getLabel().getIndex()).add(T);
    }

    boolean contains(Transition T, int source, int target){
        return Conc_Func.get(source).get(target).get(T.getLabel().getIndex()).contains(T);
    }

    LinkedHashSet<Transition> get(int source, int target, Letter A){
        return Conc_Func.get(source).get(target).get(A.getIndex());
    }

    void print(int source, int target, Letter A){
        for (Transition T : Conc_Func.get(source).get(target).get(A.getIndex())) {
            System.out.println(" (" + T.getSource().getName() + "," + T.getLabel().getSymb() + "," + T.getTarget().getName() + ") ");
        }
    }
}