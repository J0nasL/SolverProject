package CLI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Choice {
    public static final int ERROR_INT=-1;
    public static final String ERROR_STR=null;

    public static final BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));

    public static int chooseInt(String text) {
        while (true) {
            String res=getLine(text);
            if (res==null){
                System.out.println("An error has occurred");
                return ERROR_INT;
            }
            try {
                int resInt = Integer.parseInt(res);
                return resInt;
            }catch (Exception e){
                System.out.println("Invalid option");
            }
        }
    }

    public static String getLine(String text){
        System.out.print(text+" ");
        String res=ERROR_STR;
        try {
            res = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }
    
    public static void closeReader(){
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
