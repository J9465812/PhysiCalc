
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * Static class for loading text resources.
 */

public class ResourceLoader {


    /**
     * Loads key value pairs from a text file.
     *
     * Colons (:) separate keys from values.
     * Newline characters separate pairs, except when followed by a hash-mark (#).
     * Pipes (|) can be used to insert newlines.
     *
     * @param fileName The file to be opened and read.
     * @return The key-value pairs extracted from the file.
     */

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

    /**
     * Loads comma-separated-values from a file into a two-dimensional array.
     *
     * @param fileName The file to be read and parsed.
     * @return The array generated form the file.
     */

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
