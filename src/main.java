/* Main */
import java.util.*;
import java.io.*;

public class main{

    public static void main(String[] args) {
    	
        decompositor();
        
        Example E = new Example("counter",2,15,2,0);
        Autom A = E.getAutom();
        //utomParser in = new AutomParser("http-attacks");
        //Autom A = in.getAutom();
        //A.print();
        System.out.println("Automaton A has " + A.getAlphabetSize() + " letters, " + A.getStateSize() + " states and " + A.getTransSize() + " transitions.");

        /* GRAY CODE ENCODING!*/

        Decomposition Dec = new Decomposition(A,"binary");
        Dec.decompose("greedy",true);
        
        for (Autom B : Dec.Factors) {
        //    B.print();
        }
    }

    static void decompositor(){
        String out = "";
        out = String.format("%77s",out).replace(' ', '*');
        System.out.println("");
        System.out.println(out);
        System.out.println(out);
        out = "";
        
        out += "                _____     __     _____  _____     ____       _______       \n";
        out += "               /  ___/   |  |   /___  ||  ___/   /    \\     |   __  \\    \n";
        out += "              /  /       |  |       | || |      /  /\\  \\    |  |  \\  \\ \n";
        out += "              \\  \\__     |  |       | || |     |  /  \\  |   |  |__/  /  \n";
        out += "      decompo ";
        out += " \\__  \\    |  |       | || |     |  |  |  |   |      _/    \n";
        out += "                  \\  \\   |  |       | || |     |  \\  /  |   |  |\\  \\  \n";
        out += "               ___/  /   |  |       | || |      \\  \\/  /    |  | \\  \\  \n";
        out += "              /_____/    |__|       |_||_|       \\____/     |__|  \\__\\  \n";
        System.out.println(out);

        out = "";
        out = String.format("%77s",out).replace(' ', '*');
        System.out.println(out);
        System.out.println(out);
        System.out.println("");
    }
}
















