/* Letter */
import java.util.*;

public class Letter{
    
    private final String symb;
    private int index;

    Letter(String in_symb){
        /* Constructor */
        symb = in_symb;
        index = 0;
    }

    public String getSymb(){
        /* Return symbol of the letter */
        return symb;
    }

    public int getIndex(){
        /* Return index */
        return index;
    }
    
    public void setIndex(int in_index){
        /* Set index to given integer */
        index = in_index;
    }

    @Override
    public boolean equals(Object o){
        /* Override of equals */

        if (o == null) return false;
    
        if (o == this) return true;

        if (!(o instanceof Letter)) return false;

        Letter C = (Letter) o;

        return symb.equals(C.getSymb());
    }

    @Override
    public int hashCode(){
        /* Override of hashCode */
        return Objects.hash(symb);
    }
}
