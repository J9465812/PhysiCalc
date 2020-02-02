package jehand.physicalc;

import java.util.*;

public class Main {

    private static final String printPrefix = "--> ";

    public static void main(String[] args) throws Exception{

        new jehand.physicalc.Main().run();
    }

    private Map<String, String> helpText = ResourceLoader.loadTextResources("/help.txt");

    private Map<String, UncertainValue> values = new HashMap<>();

    private UncertainValue storedAnswer = new UncertainValue(0, 0, Units.parse(""));

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

        System.out.println("PhysiCalc 1.0\n");
        System.out.println("Type \"help\" for more information.\n");

        Scanner scan = new Scanner(System.in);

        while(true){

            String line = scan.nextLine();

            if(line.equals("list")){

                System.out.println(printPrefix + "List of all values:");

                for (String label : values.keySet()) {
                    System.out.println("\t\t" + label + " : " + values.get(label).toString());
                }

                continue;
            }

            if(helpText.containsKey(line)){
                System.out.println("\n" + helpText.get(line) + "\n");
                continue;
            }

            String[] args = line.split(" ");

            try {

                if (args.length >= 3 && args[1].equals("is")) {

                    UncertainValue result = evaluateExpression(Arrays.copyOfRange(args, 2, args.length));

                    values.put(args[0], result);
                    storedAnswer = result;

                    System.out.println(printPrefix + result.toString());

                } else if (args.length == 2 && args[0].equals("clear")) {

                    values.remove(args[1]);

                    System.out.println(printPrefix + "Deleted Value.");

                } else if (args.length > 2 && args[args.length - 2].equals("in")) {

                    UncertainValue result = evaluateExpression(Arrays.copyOfRange(args, 0, args.length - 2));
                    storedAnswer = result;

                    System.out.println(printPrefix + result.toString(args[args.length - 1]));

                } else {

                    UncertainValue result = evaluateExpression(args);

                    System.out.println(printPrefix + result.toString());
                    storedAnswer = result;
                }

            }catch(IllegalArgumentException e){
                System.out.println(printPrefix + "Math Error: " + e.getMessage());
            }catch(EmptyStackException e){
                System.out.println(printPrefix + "Math Error: The expression couldn't be evaluated due to lack of numbers. (Make sure all operations have the required number of inputs.)");
            }
        }
    }

    private UncertainValue evaluateExpression(String[] args){

        Stack<UncertainValue> numbers = new Stack<>();

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
