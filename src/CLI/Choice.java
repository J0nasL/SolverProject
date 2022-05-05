package CLI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Objects;

public class Choice{
    public static final int ERROR_INT=-1;
    public static final char CHECKOUT='c';
    public static final char BACK='b';
    public static final char QUIT='q';
    public static final char ADD_CART='a';
    public static final char VIEW_CART='v';
    public static final char HELP='h';
    public static final String INVALID_STR="Invalid option";
    public static final String DEFAULT_INPUT=">";
    public static final String HELP_MSG="""
            Options:
            (H)elp
            (Q)uit
            (B)ack
            (C)heckout
            (A)dd to cart
            (V)iew cart""";

    public enum Choices{
        CHECKOUT, BACK, QUIT, ADD_CART, VIEW_CART, HELP, INT
    }

    public Choices curChoice;
    public Integer intChoice;


    public static final BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));

    public static int chooseInt(String text){
        while (true){
            String res=getLine(text);
            if (res==null){
                System.out.println("An error has occurred");
                return ERROR_INT;
            }
            try{
                return Integer.parseInt(res);
            } catch (NumberFormatException e){
                System.out.println(INVALID_STR);
            }
        }
    }

    public static Choice getChoice(){
        while (true){
            String res=getLine(DEFAULT_INPUT, false);
            Choices c=null;
            switch (Character.toLowerCase(res.charAt(0))){
                case Choice.ADD_CART:
                    c=Choices.ADD_CART;
                    break;
                case Choice.BACK:
                    c=Choices.BACK;
                    break;
                case Choice.CHECKOUT:
                    c=Choices.CHECKOUT;
                    break;
                case Choice.HELP:
                    c=Choices.HELP;
                    break;
                case Choice.QUIT:
                    c=Choices.QUIT;
                    break;
                case Choice.VIEW_CART:
                    c=Choices.VIEW_CART;
                    break;
            }

            if (c==null){
                try{
                    int i=Integer.parseInt(res);
                    return new Choice(i);
                } catch (NumberFormatException e){
                    System.out.println(INVALID_STR);
                }
            } else{
                return new Choice(c);
            }
        }

    }

    public static void printHelp(){
        System.out.println(HELP_MSG);
        System.out.println();
    }

    public static String getLine(String text, boolean acceptsEmpty){
        while (true){
            String res=getLine(text);
            if (res==null){
                return null;
            }
            if (!res.isEmpty() || acceptsEmpty){
                return res;
            }
            System.out.println(INVALID_STR);
        }
    }

    public static String getLine(String text){
        System.out.print(text + " ");
        String res=null;
        try{
            res=reader.readLine();
        } catch (IOException e){
            e.printStackTrace();
        }
        return res;
    }

    public static void closeReader(){
        try{
            reader.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public Choice(int i){
        intChoice=i;
        curChoice=Choices.INT;
    }

    public Choice(Choices c){
        curChoice=c;
    }

}
