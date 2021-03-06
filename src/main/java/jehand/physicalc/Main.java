
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
    private static final String printPrefix = "";

    /**
     * The string used tp prompt the user for input.
     */
    private static final String commandPrompt = ">>>: ";

    /**
     * List of forbidden label/lock names
     */
    private static final String[] keywords = {"in", "is", "lock", "to", "list", "units", "constants", "labels", "ans", "x", "*", "+", "-", "/", "^"};

    /**
     * HashMap for storing any information text, such as license information and help.
     * Start elements with a exclamation point (!) to prevent command access.
     */
    private static final Map<String, String> infoText = ResourceLoader.loadTextResources("/assets/info.txt");

    /**
     * HashMap storing the built-in physical constants.
     */
    private static final Map<String, UncertainValue> physicalConstants = new HashMap<>();

    private static final String constantsDescriptions;

    static{

        String[][] constants = ResourceLoader.loadCSVObjectFormat("/assets/constants.txt");
        StringBuilder description = new StringBuilder();

        description.append(String.format("%38s  %-10s%-35s%-15s%s", "Constant:", "Symbol:", "Value:", "Units:", "Description:"));

        for(int n = 0; n < constants.length; n++){

            physicalConstants.put(constants[n][0], new UncertainValue(Double.parseDouble(constants[n][3]), Double.parseDouble(constants[n][4]), Units.parse(constants[n][5]), true, false));

            description.append(String.format("\n%38s  %-10s%-35s%-15s%s", constants[n][1], constants[n][0], constants[n][3] + "±" + constants[n][4], constants[n][5], constants[n][2]));
        }
        
        constantsDescriptions = description.toString();
    }

    public static void main(String[] args){

        new jehand.physicalc.Main().run();
    }

    /**
     * HashMap for storing labelled values.
     */
    private Map<String, UncertainValue> values = new HashMap<>();

    /**
     * HashMap for storing labelled values.
     */
    private Map<String, String[]> locks = new HashMap<>();


    /**
     * Value used by the "ans" keyword.
     */
    private UncertainValue storedAnswer = new UncertainValue(0, 0, Units.parse(""));


    /**
     * Method containing main loop of program.
     */
    public void run(){

        System.out.println(infoText.get("!onStart"));

        Scanner scan = new Scanner(System.in);

        while(true){

            System.out.print(commandPrompt);

            String line = scan.nextLine();
            String[] args = line.split(" ");

            if(infoText.containsKey(line) && !line.startsWith("!")){
                System.out.println(infoText.get(line));
                continue;
            }


            // list command: lists all stored variables
            if(args[0].equals("list")){
                
                if(args.length < 2){
                    System.out.println();
                }
                
                switch(args[1]){
                    
                    case "labels":
                        System.out.println(printPrefix + "List of all values:");
                        System.out.printf("%-40s%-20s%n", "Name:", "Value:");

                        Set<String> keys = values.keySet();
                        Iterator<String> iterator = keys.stream().sorted(String.CASE_INSENSITIVE_ORDER).iterator();

                        while(iterator.hasNext()) {
                            String label = iterator.next();
                            System.out.printf("%-40s%-20s%n", label, values.get(label).toString());
                        }
                        break;
                    case "constants":
                        System.out.println(constantsDescriptions);
                        break;
                    case "units":
                        System.out.println(Units.unitDescriptions);
                        break;
                }

                continue;
            }

            try {

                // is command: assigns a new variable
                if (args.length >= 3 && args[1].equals("is")) {

                    if(Arrays.stream(keywords).anyMatch((s) -> s.equals(args[0]))){
                        System.out.println("That name is reserved");
                        continue;
                    }

                    if (locks.containsKey(args[0])) {
                        System.out.println("There is a lock with that name, clear it first");
                        continue;
                    }

                    UncertainValue result = evaluateExpression(Arrays.copyOfRange(args, 2, args.length));
                    storedAnswer = result;
                    values.put(args[0], result);

                    System.out.println(printPrefix + result.toString());

                    // clear command: clears a stored variable
                } else if (args.length == 2 && args[0].equals("clear")) {

                    if(values.containsKey(args[1]) || locks.containsKey(args[1])) {

                        values.remove(args[1]);
                        locks.remove(args[1]);

                        System.out.println(printPrefix + "Deleted label/lock: " + args[1]);
                    } else {
                        System.out.println(printPrefix + "That label/lock doesn't exist");
                    }

                    // in command: prints value in given units
                } else if (args.length > 2 && args[args.length - 2].equals("in")) {

                    UncertainValue result = evaluateExpression(Arrays.copyOfRange(args, 0, args.length - 2));
                    storedAnswer = result;

                    System.out.println(printPrefix + result.toString(args[args.length - 1]));

                    // lock command: lock a label to an expression
                } else if (args.length > 3 && args[0].equals("lock") && args[2].equals("to")) {

                    if(Arrays.stream(keywords).anyMatch((s) -> s.equals(args[1]))){
                        System.out.println("That name is reserved");
                        continue;
                    }

                    if (values.containsKey(args[1])) {
                        System.out.println("There is a label with that name, clear it first");
                        continue;
                    }

                    String[] expression = Arrays.copyOfRange(args, 3, args.length);

                    locks.put(args[1], expression);

                    System.out.println(printPrefix + "Lock set successfully: " + args[1]);

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
            }catch(StackOverflowError e){
                System.out.println(printPrefix + "Math Error: Detected a loop in expression evaluation. (Make sure your locks don't reference each other in a loop.)");
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

                    if(physicalConstants.containsKey(args[n])){
                        numbers.push(physicalConstants.get(args[n]));
                        break;
                    }

                    if(values.containsKey(args[n])){
                        numbers.push(values.get(args[n]));
                        break;
                    }

                    if(locks.containsKey(args[n])){
                        numbers.push(evaluateExpression(locks.get(args[n])));
                        break;
                    }

                    numbers.push(UncertainValue.parse(args[n]));
            }
        }

        return numbers.pop();
    }
}
