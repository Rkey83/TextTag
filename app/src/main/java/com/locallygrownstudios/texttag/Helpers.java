package com.locallygrownstudios.texttag;

import java.util.Random;

public class Helpers {

    public static int randInt (int min, int max){

        Random random = new Random();
        return random.nextInt((max - min)+ 1) + min;

    }

    public static String formatPhoneNumber(String string){

        if (string != null) {

            if (string.contains("(")) {

                return string;

            } else {

                if (string.length() <= 8) {

                    return string;

                }

                if (string.length() >= 10 && string.length() < 11) {

                    String areaCodeRaw = string.substring(0, 3);
                    String numberProper = string.substring(3, 6);
                    String numberLast = string.substring(6);
                    String contactsNumber = "(" + areaCodeRaw + ")" + " " + numberProper + "-" + numberLast;
                    string = contactsNumber;

                }

                else {

                    String remIntCode = string.substring(2);
                    String areaCodeRaw = remIntCode.substring(0, 3);
                    String numberProper = remIntCode.substring(3, 6);
                    String numberLast = remIntCode.substring(6);
                    String contactsNumber = "(" + areaCodeRaw + ")" + " " + numberProper + "-" + numberLast;
                    string = contactsNumber;

                }
            }
        }
        return string;
    }

    public static String stripNumberFormatiing(String string){

        if (string.contains("(")){
            string = string.replaceAll("[^0-9]", "");
        }
        return string;
    }



}
