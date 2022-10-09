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
            System.out.print("Enter your current location: ");
            String current = sc.nextLine();
            validateInput(current);
            // capitalise last character
            current = current.substring(0,current.length()-1) + current.substring(current.length()-1).toUpperCase();
            System.out.print("Enter your desired location: ");
            String desired = sc.nextLine();
            validateInput(desired);
            // capitalise last character
            desired = desired.substring(0,desired.length()-1) + desired.substring(desired.length()-1).toUpperCase();
            System.out.println("\nThe shortest route is:\n" + distance(current, desired));
        }
        catch(IOException e){
            System.out.println("Invalid location. Enter in the form <time><letter>, for example 2A, 10B, 2:15C, 9:30D");
            System.out.println("Note that <time> ranges from 2 to 10, with h, h:15, h:30, h:45");
            System.out.println("Note that <letter> ranges from A-L");
        }
    }

    // confirms validity of current and desired locations
    public static void validateInput(String input) throws IOException{
        String timeRegex = "(10|[2-9](:(15|30|45))?)";
        String letterRegex = "[a-lA-L]";
        if (!Pattern.matches(timeRegex+letterRegex, input)) {
            throw new IOException();
        }
    }

    // returns String of optimal path and distance in feet
    public static String distance(String current, String desired){

        final int MAN_TO_ESPLANADE = 2500; // in feet
        // 0th is distance from Esplanade to A in feet, etc.
        final int[] ESPLANADE_TO_LETTERS = new int[]{400, 650, 900, 1150, 1400, 1600, 1800, 2000, 2200, 2400, 2550, 2700};

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
            int r = MAN_TO_ESPLANADE + ESPLANADE_TO_LETTERS[Character.compare(currentLetter, 'A')];
            p1Distance = r*angle + ESPLANADE_TO_LETTERS[Character.compare(desiredLetter, 'A')] - ESPLANADE_TO_LETTERS[Character.compare(currentLetter, 'A')];
        }
        else{
            // p1Distance = difInLetters + newR*angle
            int newR = MAN_TO_ESPLANADE + ESPLANADE_TO_LETTERS[Character.compare(desiredLetter, 'A')];
            p1Distance = ESPLANADE_TO_LETTERS[Character.compare(currentLetter, 'A')] - ESPLANADE_TO_LETTERS[Character.compare(desiredLetter, 'A')] + newR*angle;
        }

        // path 2: go to esplanade from current letter, go directly to esplanade and destination time, then go to desired letter
        double p2Distance = ESPLANADE_TO_LETTERS[Character.compare(currentLetter, 'A')] + ESPLANADE_TO_LETTERS[Character.compare(desiredLetter, 'A')];
        // distance from esplanade and current letter to esplanade and desired letter can be found with cosine rule
        p2Distance += Math.sqrt(2*Math.pow(MAN_TO_ESPLANADE,2)*(1-Math.cos(angle)));

        // TODO add checks for if currentLetter and desiredLetter are equal, to remove that printed line. For example current: 2A, desired: 3A, get "...current location 2A to 2A (line)..."
        if(p1Distance < p2Distance){
            if(arcFirst){
                return "- current location " + current + " to " + desired.substring(0,desired.length()-1) + currentLetter  + " (" + direction + " arc)"
                        + "\n- and then go to " + desired + " (line)"
                        + "\n= " + p1Distance + " feet";
            }
            else{
                return "- current location " + current + " to " + current.substring(0,current.length()-1) + desiredLetter + " (line)" 
                        + "\n- and then go to " + desired + " (" + direction + " arc)"
                        + "\n= " + p1Distance + " feet";
            }
        }
        else{
            return "- current location " + current + " to " + current.substring(0,current.length()-1) + " & Esplanade (line)"
                    + "\n- then go to " + desired.substring(0,desired.length()-1) + " & Esplanade (line)"
                    + "\n- and then go to " + desired + " (line)"
                    + "\n= " + p2Distance + " feet";
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
