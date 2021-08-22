/* State */
import java.util.*;

public class State{

    private final String name;
    private boolean init;
    private boolean fin;
    private int index;
    private int enumerator;

    State(String in_name, boolean in_init, boolean in_fin){
        // Constructor
        name = in_name;
        init = in_init;
        fin = in_fin;
        index = 0;
    }

    public String getName(){
        // Return the name of the state
        return name;
    }

    public boolean isInit(){
        // Return whether state is initial or not
        return init;
    }

    public boolean isFinal(){
        // Return whether state is final or not
        return fin;
    }

    public int getIndex(){
        // Return index
        return index;
    }

    public int getEnumerator(){
        // Return enumerator
        return enumerator;
    }

    public void setInit(){
        // Set state to be initial
        init = true;
    }

    public void setFinal(){
        // Set state to be final
        fin = true;
    }

    public void setIndex(int in_index){
        // Set index to given integer
        index = in_index;
    }

    public void setEnumerator(int in_enum){
        // Set enumerator to given integer
        enumerator = in_enum;
    }

    @Override
    public boolean equals(Object o){
        // Override of equals
        
        if (o == null) return false;
    
        if (o == this) return true;

        if (!(o instanceof State)) return false;

        State Q = (State) o;

        return name.equals(Q.getName());
    }

    @Override
    public int hashCode(){
        // Override of hashCode
        return Objects.hash(name);
    }
}