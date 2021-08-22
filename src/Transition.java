/* Transition */
import java.util.*;

public class Transition{

    State source;
    State target;
    Letter label;
    int index;

    Transition(State in_source, State in_target, Letter in_label){
        source = in_source;
        target = in_target;
        label = in_label;
        index = 0;
    }

    State getSource(){
        return source;
    }

    State getTarget(){
        return target;
    }

    Letter getLabel(){
        return label;
    }

    int getIndex(){
        return index;
    }

    void setIndex(int in_index){
        index = in_index;
    }

    /* Override of equals */
    @Override
    public boolean equals(Object o){

        if (o == null) return false;
    
        if (o == this) return true;

        if (!(o instanceof Transition)) return false;

        Transition T = (Transition) o;

        return source.equals(T.getSource()) && target.equals(T.getTarget()) && label.equals(T.getLabel());
    }

    /* Override of hashCode */
    @Override
    public int hashCode(){
        return Objects.hash(source, target, label);
    }
}