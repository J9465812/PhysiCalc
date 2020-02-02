package jehand.physicalc;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class ResourceLoader {


    public static HashMap<String, String> loadTextResources(String fileName){

        Scanner scan;

        HashMap<String, String> resources = new HashMap<>();

        try{

            InputStream f = ResourceLoader.class.getResourceAsStream(fileName);
            scan = new Scanner(f);

        }catch(Exception e){

            System.out.println("Could not load resources: \"" + fileName + "\"");
            return resources;
        }

        List<String> contents = new ArrayList<>();

        while(scan.hasNextLine()){

            String line = scan.nextLine();

            if(!line.isEmpty()){

                if(line.startsWith("#")){
                    contents.add(contents.remove(contents.size() - 1) + line.substring(1));
                }else{
                    contents.add(line);
                }
            }
        }

        for(String line : contents){

            String[] split = line.split(":", 2);

            split[1] = split[1].replaceAll("\\|", "\n");

            resources.put(split[0], split[1]);
        }

        return resources;
    }

    public static String[][] loadCSVObjectFormat(String fileName){

        Scanner scan;

        try{

            InputStream f = ResourceLoader.class.getResourceAsStream(fileName);
            scan = new Scanner(f);

        }catch(Exception e){

            System.out.println("Could not load resources: \"" + fileName + "\"");
            return null;
        }

        List<String[]> values = new ArrayList<>();

        while(scan.hasNextLine()){

            String line = scan.nextLine();

            values.add(line.split(","));
        }

        return values.toArray(new String[5][values.size()]);
    }
}
