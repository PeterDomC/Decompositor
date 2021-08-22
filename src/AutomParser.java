/* Parser for vtf files */
import java.util.*;
import java.io.*;

public class AutomParser{
    
    Autom A;

    AutomParser(String data_name){
        try{
            BufferedReader in = new BufferedReader(new FileReader(data_name + ".vtf"));
            String line;

            // First row is always @NFA
            line = in.readLine();
            if (!line.equals("@NFA")) {
                System.out.println("File not in right format.");
                return;
            }

            LinkedHashSet<State> Stateset = new LinkedHashSet<State>();
            LinkedHashSet<Letter> Sigma = new LinkedHashSet<Letter>();
            A = new Autom(Sigma,Stateset);

            // #################################################################################
            // #################################################################################

            // Read first two symbol of next row, it has to be %S (for States)
            char a = (char) in.read();
            char b = (char) in.read();
            if (a != '%' || b != 'S') {
                System.out.println("File not in right format.");
                return;
            }

            // Skip 6 characters to arrive at the first state
            in.skip(6);
            String state_name = "";
            while ((a = (char) in.read()) != '%') {
                if (a == ' ') {
                    // Add state to set of states
                    //System.out.println(state_name);
                    State q = new State(state_name,false,false);
                    A.addState(q);
                    state_name = "";
                    continue;
                }

                if (a == '\n') {
                    // End of line reached, add last state and break
                    //System.out.println(state_name);
                    State q = new State(state_name,false,false);
                    A.addState(q);
                    state_name = "";
                    break;
                }

                state_name = state_name + String.valueOf(a);
            }
            
            // #################################################################################
            // #################################################################################

            // Read first two symbol of next row, it has to be %F (for Final)
            a = (char) in.read();
            b = (char) in.read();
            if (a != '%' || b != 'F') {
                System.out.println("File not in right format.");
                return;
            }

            // Skip 5 characters to arrive at the final state
            in.skip(5);
            state_name = "";
            while ((a = (char) in.read()) != '\n') {
                if(a == ' '){
                    // Ignore all final states that come after the first
                    break;
                }

                state_name = state_name + String.valueOf(a);
            }
            
            A.setFinal(A.getStateByName(state_name));

            // #################################################################################
            // #################################################################################

            // Read first two symbol of next row, it has to be %A (for Alphabet)
            a = (char) in.read();
            b = (char) in.read();
            if (a != '%' || b != 'A') {
                System.out.println("File not in right format.");
                return;
            }

            // Skip 8 characters to arrive at first letter
            in.skip(8);
            String letter_symb = "";
            while ((a = (char) in.read()) != '%') {
                if (a == 's') {
                    // Begin of the letter is always s0
                    letter_symb = "s0";
                    continue;
                }

                if (a == ':') {
                    // Add the letter to the alphabet
                    Letter B = new Letter(letter_symb);
                    A.addLetter(B);
                    letter_symb = "";

                    // Ignore the colon and the next symbol (the 1)
                    in.skip(1);
                    continue;
                }

                if (65 <= a && a <= 90) {
                    // Reading a capital letter, add it to the letter symbol but with the same small letter
                    letter_symb = letter_symb + String.valueOf((char)(a + 32));
                }

                if (48 <= a && a <= 57) {
                    // Reading a number, add it to the letter symbol
                    letter_symb = letter_symb + String.valueOf(a);
                }

                if (a == ' ') {
                    // Whitespaces are ignored
                    continue;
                }

                if (a == 't') {
                    // Ignore start:0
                    letter_symb = "";
                    in.skip(5);
                }

                // End of line reached
                if (a == '\n') break;
            }

            // #################################################################################
            // #################################################################################

            // Read first two symbol of next row, it has to be %I (for Initial)
            a = (char) in.read();
            b = (char) in.read();
            if (a != '%' || b != 'I') {
                System.out.println("File not in right format.");
                return;
            }

            // Skip 7 characters to arrive at initial state
            in.skip(7);
            state_name = "";
            while ((a = (char) in.read()) != '\n') {
                state_name = state_name + String.valueOf(a);
            }
            
            A.setInit(A.getStateByName(state_name));
            
            // #################################################################################
            // #################################################################################

            // The line before the transitions is empty
            line = in.readLine();
            if (!line.equals("")) {
                System.out.println("File not in right format.");
                return;
            }

            // Read all transitions, one in each line
            String source,target,letter;
            while ((line = in.readLine()) != null) {
                // Ignore empty line
                if(line.equals("")) continue;

                source = line.substring(0,line.indexOf(" "));
                target = line.substring(line.lastIndexOf(" ")+1);
                letter = line.substring(line.indexOf(" ")+1,line.lastIndexOf(" "));
                Transition t = new Transition(A.getStateByName(source), A.getStateByName(target), A.getLetterBySymb(letter));
                A.addTransition(t);
            }

            in.close();
        }

        catch (FileNotFoundException e) {
            System.err.println("File not found!");
        }
        catch (IOException e) {
            System.err.println("Parsing causes IOException!");
        }
    }

    public Autom getAutom(){
        //
        return A;
    }

}
