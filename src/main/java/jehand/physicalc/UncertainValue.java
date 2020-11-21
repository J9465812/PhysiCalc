
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class representing a physical quantity, with uncertainty and units.
 */

public class UncertainValue {

    /**
     * The value of the quantity.
     */
    double value;

    /**
     * The absolute uncertainty of the quantity.
     */
    double uncertainty;

    /**
     * The units of the quantity.
     */
    Units units;

    /**
     * The normal constructor.
     *
     * @param value the value
     * @param uncertainty the uncertainty
     * @param units th units
     */
    public UncertainValue(double value, double uncertainty, Units units) {
        this(value, uncertainty, units, false, false);
    }

    /**
     * The advanced constructor.
     *
     * @param value the value
     * @param uncertainty the uncertainty
     * @param units the units
     * @param relativeUncertainty is the uncertainty relative (a.k.a. fraction of the value) or absolute
     * @param unitScaled is the value already scaled to standard MGS (meters-grams-seconds) units.
     */
    public UncertainValue(double value, double uncertainty, Units units, boolean relativeUncertainty, boolean unitScaled) {
        this.value = value;
        this.uncertainty = uncertainty;

        if(!unitScaled){
            this.value *= units.getFactor();
            this.uncertainty *= units.getFactor();
        }

        if(relativeUncertainty)
            this.uncertainty *= value;
        this.units = units;
    }

    /**
     * Adds this value to other.
     *
     * @param other the other value to add
     * @return the sum
     */
    public UncertainValue add(UncertainValue other){

        if(!this.units.equals(other.units)){
            throw new IllegalArgumentException("Can't add/subtract values with different units.");
        }

        return new UncertainValue(value + other.value, uncertainty + other.uncertainty, units, false, true);
    }

    /**
     * Subtracts the other value from this one.
     *
     * @param other the value to subtract
     * @return the difference
     */
    public UncertainValue subtract(UncertainValue other){

        if(!this.units.equals(other.units)){
            throw new IllegalArgumentException("Can't add/subtract values with different units.");
        }

        return new UncertainValue(value - other.value, uncertainty + other.uncertainty, units, false, true);
    }

    /**
     * Multiplies this value and the other one.
     *
     * @param other the other value to multiply
     * @return the product
     */
    public UncertainValue multiply(UncertainValue other){

        return new UncertainValue(value * other.value, (uncertainty/value + other.uncertainty/other.value) * value * other.value, Units.multiply(units, other.units), false, true);
    }

    /**
     * Divides this value by the other one.
     *
     * @param other the value to divide by.
     * @return the quotient
     */
    public UncertainValue divide(UncertainValue other){

        return new UncertainValue(value / other.value, (uncertainty/value + other.uncertainty/other.value) * value / other.value, Units.divide(units, other.units), false, true);
    }

    /**
     * Raises this value to the power of the other value.
     *
     * @param power
     * @return
     */
    public UncertainValue power(int power){

        double outValue = Math.pow(value, power);

        return new UncertainValue(outValue, uncertainty * power * outValue / value, Units.power(units, power), false, true);
    }

    /**
     * Converts this value to a string: 8.76E5±4.32E1m/s^2.
     *
     * @return the string
     */
    public String toString(){

        String unitString = units.toString();

        String[] unitSplit = unitString.split("_");

        double factor = 1;

        if(unitSplit.length > 1){
            factor = Double.parseDouble(unitSplit[1]);
        }

        return (value/factor) + "+-" + (uncertainty/factor) + " " + unitSplit[0];
    }

    /**
     * Converts this value to a string in the specified units.
     *
     * @param units the units to output in.
     * @return the string
     */
    public String toString(String units){

        Units u = Units.parse(units);

        if(!u.equals(this.units)){
            throw new IllegalArgumentException("Can not convert to incompatible units:" + u);
        }

        return (value/u.getFactor()) + "±" + (uncertainty/u.getFactor()) + " " + units;
    }

    private static final String parseRegex = "(-?[0-9.]+)(?:(?:[x*]10\\^|E)(-?[0-9]+))?(?:(?:\\+-|±)([0-9.]+)(?:(?:[x*]10\\^|E)(-?[0-9]+))?)?([a-zA-Z0-9/^]*)";

    /**
     * Parses a string into an uncertain value.
     *
     * Formatting is quite flexible, see "help number formatting" for more information.
     *
     * @param value the string to parse
     * @return the
     */
    public static UncertainValue parse(String value){

        Pattern pat = Pattern.compile(parseRegex);

        Matcher match = pat.matcher(value);

        if(!match.matches()){
            throw new IllegalArgumentException("Could not parse number:" + value);
        }

        String[] parts = new String[5];

        for(int n = 1; n < 6; n++){
            parts[n-1] = match.group(n);
        }

        if(parts[1] == null){
            parts[1] = "0";
        }
        if(parts[2] == null){
            parts[2] = "0";
        }
        if(parts[3] == null){
            parts[3] = "0";
        }
        if(parts[4] == null){
            parts[4] = "1";
        }

        double number = Double.parseDouble(parts[0]) * Math.pow(10, Integer.parseInt(parts[1]));
        double uncertainty = Double.parseDouble(parts[2]) * Math.pow(10, Integer.parseInt(parts[3]));
        Units units = Units.parse(parts[4]);

        return new UncertainValue(number, uncertainty, units);
    }
}