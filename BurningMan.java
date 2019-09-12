/*

Program which receives as input the user's current and desired locations in Burning Man using TimeLetter
notation, and outputs the shortest path.

Omer Baddour, 5/9/19

*/

import java.io.IOException;
import java.util.Scanner;
import java.util.regex.*;

public class BurningMan {

    public static void main(String[] args){

        try{
            Scanner sc = new Scanner(System.in);
            System.out.println("Enter your current location: ");
            String current = sc.nextLine();
            validateInput(current);
            // capitalise last character
            current = current.substring(0,current.length()-1) + current.substring(current.length()-1).toUpperCase();
            System.out.println("Enter your desired location: ");
            String desired = sc.nextLine();
            validateInput(desired);
            // capitalise last character
            desired = desired.substring(0,desired.length()-1) + desired.substring(desired.length()-1).toUpperCase();
            System.out.println("The shortest route is: " + distance(current, desired));
        }
        catch(IOException e){
            System.out.println("Invalid current location. Enter in the form TimeLetter, for example 1A, 10B, 2:15C, 12:30D. Note that Letter ranges from A-L");
        }
    }

    // confirms validity of current and desired locations
    public static void validateInput(String input) throws IOException{
        if (input.length() == 2) {
            if (!regexChecker("[2-9][a-lA-L]", input)) {
                throw new IOException();
            }
        }
        else if (input.length() == 3) {
            if (!regexChecker("[1][0-2][a-lA-L]", input)) {
                throw new IOException();
            }
        }
        else if (input.length() == 5){
            if(!regexChecker("[2-9][:](1[5]|3[0]|4[5])[a-lA-L]", input)){
                throw new IOException();
            }
        }
        else if (input.length() == 6){
            if(!regexChecker("[1][0-2]:(1[5]|3[0]|4[5])[a-lA-L]", input)){
                throw new IOException();
            }
        }
        else{
            throw new IOException();
        }
    }

    // returns true if input string matches form of regex
    public static boolean regexChecker(String regex, String input){

        return Pattern.matches(regex, input);
    }

    // returns String of optimal path and distance in feet
    public static String distance(String current, String desired){

        final int MANTOESPLANADE = 2500; // in feet
        // 0th is distance from Esplanade to A in feet, etc.
        final int[] ESPLANADETOLETTERS = new int[]{400, 650, 900, 1150, 1400, 1600, 1800, 2000, 2200, 2400, 2550, 2700};

        // path 1: arc + adjust
        // establish whether we first traverse arc or traverse towards man
        // currentLetter < desiredLetter -> first traverse arc
        char currentLetter = current.charAt(current.length()-1);
        char desiredLetter = desired.charAt(desired.length()-1);
        boolean arcFirst = Character.compare(currentLetter, desiredLetter) < 0 ? true : false;
        String angularDisp = angularDisplacement(current, desired);
        String direction = angularDisp.charAt(0) == 'T' ? "clockwise" : "anticlockwise";
        double angle = Double.parseDouble(angularDisp.substring(1));
        double p1Distance = 0;
        if(arcFirst){
            // p1Distance = r*angle + difInLetters
            int r = MANTOESPLANADE + ESPLANADETOLETTERS[Character.compare(currentLetter, 'A')];
            p1Distance = r*angle + ESPLANADETOLETTERS[Character.compare(desiredLetter, 'A')] - ESPLANADETOLETTERS[Character.compare(currentLetter, 'A')];
        }
        else{
            // p1Distance = difInLetters + newR*angle
            int newR = MANTOESPLANADE + ESPLANADETOLETTERS[Character.compare(desiredLetter, 'A')];
            p1Distance = ESPLANADETOLETTERS[Character.compare(currentLetter, 'A')] - ESPLANADETOLETTERS[Character.compare(desiredLetter, 'A')] + newR*angle;
        }

        // path 2: go to esplanade from current letter, go directly to esplanade and destination time, then go to desired letter
        double p2Distance = ESPLANADETOLETTERS[Character.compare(currentLetter, 'A')] + ESPLANADETOLETTERS[Character.compare(desiredLetter, 'A')];
        // distance from esplanade and current letter to esplanade and desired letter can be found with cosine rule
        p2Distance += Math.sqrt(2*Math.pow(MANTOESPLANADE,2)*(1-Math.cos(angle)));

        if(p1Distance < p2Distance){
            if(arcFirst){
                return "current location " + current + " to " + desired.substring(0,desired.length()-1)
                        + currentLetter  + " (" + direction + " arc) and then go to "
                        + desired + " (line) = " + p1Distance + " feet";
            }
            else{
                return "current location " + current + " to " + current.substring(0,current.length()-1)
                        + desiredLetter + " (line) and then go to " + desired
                        + " (" + direction + " arc) = " + p1Distance + " feet";
            }
        }
        else{
            return "current location " + current + " to " + current.substring(0,current.length()-1) + " & Esplanade (line) "
                    + "then go to " + desired.substring(0,desired.length()-1) + " & Esplanade (line) "
                    + "and then go to " + desired + " (line) = " + p2Distance + " feet";
        }
    }

    // returns String: T/F (true/false to assume clockwise) + angle
    public static String angularDisplacement(String current, String desired){

        int currentHour = hourFromPosition(current);
        int currentMinute = minuteFromPosition(current);
        int desiredHour = hourFromPosition(desired);
        int desiredMinute = minuteFromPosition(desired);

        // assume clockwise traversal. if angle > pi, reverse
        double angle = (desiredHour - currentHour)*Math.PI/6 + (desiredMinute - currentMinute)*Math.PI/360;
        String angularDisp;
        if(angle <= Math.PI){
            angularDisp = "T" + Double.toString(angle);
        }
        else{
            angularDisp = "F" + Double.toString(angle);
        }
        return angularDisp;
    }

    public static int hourFromPosition(String position){
        if(position.length() == 2 || position.length() == 5){
            return Integer.parseInt(position.substring(0,1));
        }
        else if(position.length() == 3 || position.length() == 6){
            return Integer.parseInt(position.substring(0,2));
        }
        // will never happen because input has already been verified as valid before method call
        // done for readability
        return -1;
    }

    public static int minuteFromPosition(String position){
        if(position.length() == 2 || position.length() == 3){
            return 0;
        }
        else if(position.length() == 5){
            return Integer.parseInt(position.substring(2,4));
        }
        else if(position.length() == 6){
            return Integer.parseInt(position.substring(3,5));
        }
        // will never happen because input has already been verified as valid before method call
        // done for readability
        return -1;
    }

}
