
/*
 *  Copyright ©, 2020, Joseph E. Hand
 *
 *  This file is part of PhysiCalc.
 *
 *  PhysiCalc is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  PhysiCalc is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with PhysiCalc.  If not, see <https://www.gnu.org/licenses/>.
 */

package jehand.physicalc;

import java.util.*;

/**
 * Main class for executing the app.
 *
 * Handles input and output, expression evaluation, and help pages.
 */

public class Main {

    /**
     * The prefix appended to any output.
     */
    private static final String printPrefix =   "";

    /**
     * The string used tp prompt the user for input.
     */
    private static final String commandPrompt = ">>>: ";


    public static void main(String[] args) throws Exception{

        new jehand.physicalc.Main().run();
    }

    /**
     * HashMap for storing any information text, such as license information and help.
     * Start elements with a exclamation point (!) to prevent command access.
     */
    private Map<String, String> infoText = ResourceLoader.loadTextResources("/assets/info.txt");

    /**
     * HashMap for storing labelled values.
     */
    private Map<String, UncertainValue> values = new HashMap<>();


    /**
     * Value used by the "ans" keyword.
     */
    private UncertainValue storedAnswer = new UncertainValue(0, 0, Units.parse(""));


    /**
     * Method containing main loop of program.
     */
    public void run(){

        // Mathematical constants
        values.put("pi", new UncertainValue(Math.PI, 0.000000000000000001, Units.parse("")));

        // Physical Constants

        values.put("c", new UncertainValue(299792458.0, 0.0, Units.parse("m/s")));
        values.put("h", new UncertainValue(0.000000000000000000000000000000000662607015, 0.0, Units.parse("Js")));
        values.put("G", new UncertainValue(0.000000000066743015, 0.000022, Units.parse("m^3/kgs^2"), true, false));
        values.put("L", new UncertainValue(602214076000000000000000.0, 0.0, Units.parse("")));
        values.put("e", new UncertainValue(0.0000000000000000001602176634, 0.0, Units.parse("C")));
        values.put("k", new UncertainValue(16021766340000000000.0, 0.0, Units.parse("J/K")));
        values.put("m_u", new UncertainValue(0.000000000000000000000001660539066605, 0.0000000003, Units.parse("g"), true, false));

        System.out.println("PhysiCalc 1.0.1\n");
        System.out.println(infoText.get("!onStart"));

        Scanner scan = new Scanner(System.in);

        while(true){

            System.out.print(commandPrompt);

            String line = scan.nextLine();

            if(infoText.containsKey(line) && !line.startsWith("!")){
                System.out.println(infoText.get(line));
                continue;
            }


            // list command: lists all stored variables
            if(line.equals("list")){

                System.out.println(printPrefix + "List of all values:");

                for (String label : values.keySet()) {
                    System.out.println("\t\t" + label + " : " + values.get(label).toString());
                }

                continue;
            }

            String[] args = line.split(" ");

            try {

                // is command: assigns a new variable
                if (args.length >= 3 && args[1].equals("is")) {

                    UncertainValue result = evaluateExpression(Arrays.copyOfRange(args, 2, args.length));

                    values.put(args[0], result);
                    storedAnswer = result;

                    System.out.println(printPrefix + result.toString());

                    // clear command: clears a stored variable
                } else if (args.length == 2 && args[0].equals("clear")) {

                    values.remove(args[1]);

                    System.out.println(printPrefix + "Deleted Value.");

                    // in command: prints value in given units
                } else if (args.length > 2 && args[args.length - 2].equals("in")) {

                    UncertainValue result = evaluateExpression(Arrays.copyOfRange(args, 0, args.length - 2));
                    storedAnswer = result;

                    System.out.println(printPrefix + result.toString(args[args.length - 1]));

                    // expression: evaluate and print result
                } else {

                    UncertainValue result = evaluateExpression(args);

                    System.out.println(printPrefix + result.toString());
                    storedAnswer = result;
                }

                // error handling
            }catch(IllegalArgumentException e){
                System.out.println(printPrefix + "Math Error: " + e.getMessage());
            }catch(EmptyStackException e){
                System.out.println(printPrefix + "Math Error: The expression couldn't be evaluated due to lack of numbers. (Make sure all operations have the required number of inputs.)");
            }
        }
    }

    /**
     * Eveluates an expression using RPN (Reverse Polish Notation).
     *
     * @param args the arguments and operations to evaluate
     * @return the output of the expression
     */
    private UncertainValue evaluateExpression(String[] args){

        Stack<UncertainValue> numbers = new Stack<>();

        // temp is used to reverse the order elements are taken off the stack to correct operand order.
        UncertainValue temp;

        for(int n = 0; n < args.length; n++) {

            switch (args[n]) {
                case "+":
                    numbers.push(numbers.pop().add(numbers.pop()));
                    break;
                case "-":
                    temp = numbers.pop();
                    numbers.push(numbers.pop().subtract(temp));
                    break;
                case "*":
                case "x":
                    numbers.push(numbers.pop().multiply(numbers.pop()));
                    break;
                case "/":
                    temp = numbers.pop();
                    numbers.push(numbers.pop().divide(temp));
                    break;
                case "ans":
                    numbers.push(storedAnswer);
                    break;
                default:

                    // process exponentiation
                    if(args[n].startsWith("^")){
                        numbers.push(numbers.pop().power(Integer.parseInt(args[n].substring(1))));
                        break;
                    }

                    if(values.containsKey(args[n])){
                        numbers.push(values.get(args[n]));
                        break;
                    }

                    numbers.push(UncertainValue.parse(args[n]));
            }
        }

        return numbers.pop();
    }
}
