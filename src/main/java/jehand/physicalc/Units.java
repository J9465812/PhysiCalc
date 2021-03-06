
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that represents the units of a quantity.
 */

public class Units {

    /**
     * Basic Units used to represent all others (with a few exceptions).
     */
    private static final String[] baseUnits = {"m", "g", "s", "A", "K", "cd"};

    /**
     * List of all metric units. Only these will be used by default to print out values.
     */
    private static final Map<String, Units> standardUnits = new HashMap<>();

    /**
     * List of all units supported.
     */
    private static final Map<String, Units> allUnits = new HashMap<>();

    /**
     * Table of all units with information. Currently inaccessible.
     */
    public static final String unitDescriptions;

    /**
     * Regex with all unit abbreviations.
     */
    private static final String unitMatch;

    // Initialize unit lists, descriptions, and matcher from units.txt file.
    static {

        String[][] units = ResourceLoader.loadCSVObjectFormat("/assets/units.txt");

        StringBuilder descriptions = new StringBuilder();
        descriptions.append("\n" + String.format("%25s  %-15s%s", "Name:", "Abbreviation:", "Description:"));

        StringBuilder match = new StringBuilder();

        for(int n = 0; n < units.length; n++){

            addUnit(units[n][0], Integer.parseInt(units[n][3]), Double.parseDouble(units[n][4]), units[n][5].equals("T"));
            descriptions.append("\n" + String.format("%25s  %-15s%s", units[n][1], units[n][0], units[n][2]));
            match.append("|" + units[n][0]);
        }

        unitDescriptions = descriptions.toString();
        unitMatch = match.substring(1);
    }

    /**
     * Adds specified unit to lists.
     *
     * @param abr the abbreviation used in the program
     * @param value the actual dimensions being used
     * @param factor the ration between this unit and the MGS standard unit of the same type
     * @param isMetric Should this be added to the metric units list?
     */
    private static void addUnit(String abr, int value, double factor, boolean isMetric){

        Units u = new Units(value, factor);

        allUnits.put(abr, u);

        if(isMetric){
            standardUnits.put(abr, u);
        }
    }

    /**
     * The dimensions being represented.
     *
     * Each digit is the power of the corresponding dimension, offset by 5.
     *
     * For example, the least significant digit represents meters, so area (meters-squared) has a value of 555557.
     *
     * Powers above 4 and below -5 can't be represented and won't display properly.
     */
    private int value;

    /**
     * The ration between this unit and the standard MGS unit for the quantity being represented.
     *
     * For example, feet have a factor of 0.3048 because one foot is equivalent to 0.3048 meters, the standard MGS unit for length.
     */
    private double factor;

    public double getFactor(){
        return factor;
    }

    private Units(int value, double factor){
        this.value = value;
        this.factor = factor;
    }

    /**
     * Multiplies two units together.
     *
     * @param a the first unit
     * @param b the second unit
     * @return the product
     */
    public static Units multiply(Units a, Units b){
        return new Units(a.value + b.value - 555555, a.factor * b.factor);
    }

    /**
     * Divides two units.
     *
     * @param a the dividend
     * @param b the divisor
     * @return the quotient
     */
    public static Units divide(Units a, Units b){
        return new Units(a.value - b.value + 555555, a.factor / b.factor);
    }

    /**
     * Raises a unit to a power.
     *
     * @param a the unit
     * @param power the power
     * @return the result
     */
    public static Units power(Units a, int power){
        return new Units((a.value - 555555)*power + 555555, Math.pow(a.factor, power));
    }

    /**
     * Is this unit equal to the other.
     *
     * Two units are considered equal if the have the same value, but not necessarily the same factor.
     *
     * @param other the other unit
     * @return Are they equal?
     */
    public boolean equals(Object other){

        if(!(other instanceof Units)) return false;

        Units otherUnits = (Units) other;

        return value == otherUnits.value;
    }

    private static final char[] multipliers  = {'p', '?', '?', 'n', '?', '?', 'μ', '?', '?', 'm', 'c', 'd', '-', 'D', 'h', 'k', ' ', ' ', 'M', ' ', ' ', 'G', '?', '?', 'T'};
    private static final String multiplierMatch = "pnμmckMGT";
    private static final int FACTOR_OFFSET = -12;

    /**
     * Parses a string into a Units object.
     *
     * @param units the string to parse
     * @return the Units object
     */
    public static Units parse(String units){

        if(units.equals("")){
            units = " ";
        }

        int value = 555555;
        double factor = 1;

        units = units.replaceAll(" ", "");

        String[] nd = units.split("/");

        if(nd.length > 2){
            throw new IllegalArgumentException("Units contain multiple slashes (/).");
        }

        Pattern pattern = Pattern.compile("(([" + multiplierMatch + "]?)(" + unitMatch + ")(?:\\^([2-9]))?)*");

        for(int n = 0; n < nd.length; n++){
            
            if(nd[n].equals("1")){
                continue;
            }

            String remainingUnits = nd[n];

            while(remainingUnits.length() > 0){

                Matcher matcher = pattern.matcher(remainingUnits);

                if(!matcher.matches()){
                    throw new IllegalArgumentException("Can't parse units \"" + units + "\"");
                }

                String unitElement = matcher.group(1);

                String prefix = matcher.group(2);
                String unit = matcher.group(3);
                String power = matcher.group(4);

                if(prefix.equals("")){
                    prefix = "-";
                }

                if(power == null){
                    power = "1";
                }

                int i = 0;

                for(; multipliers[i] != prefix.charAt(0); i++);


                int powerInt = Integer.parseInt(power) * (1 - 2 * n);
                float prefixPowerInt = (i + FACTOR_OFFSET);

                Units unitObj = allUnits.get(unit);

                value += powerInt * (unitObj.value - 555555);

                factor *= Math.pow(unitObj.factor * Math.pow(10, prefixPowerInt), powerInt);

                remainingUnits = remainingUnits.substring(0, remainingUnits.length() - unitElement.length());
            }
        }

        return new Units(value, factor);
    }

    /**
     * Converts this Units object to a string.
     *
     * If there is a standard unit equivalent to this one, it will output that unit, with the scale appended.
     *
     * @return the string
     */
    public String toString(){

        for (String unit : standardUnits.keySet()) {

            if(this.equals(standardUnits.get(unit))){

                return unit + "_" + (standardUnits.get(unit).factor) + "";
            }
        }

        String nUnits = "";
        String dUnits = "";

        for(int n = 0; n < 6; n++){

            int signedPower = getUnitPower(n);
            int power = Math.abs(signedPower);

            if(signedPower > 0){

                nUnits += baseUnits[n];

                if(power > 1)
                    nUnits += "^" + power;

            } else if(signedPower < 0){

                dUnits += baseUnits[n];

                if(power > 1)
                    dUnits += "^" + power;
            }
        }

        if(nUnits.equals("")){
            nUnits = "1 ";
        }

        if(dUnits.equals("")){

            if(nUnits.equals("1 ")){
                return "";
            }

            return nUnits;
        }

        return nUnits + "/" + dUnits;
    }

    private int getUnitPower(int n){

        return ((value / (int)Math.pow(10, n)) % 10) - 5;
    }
}
