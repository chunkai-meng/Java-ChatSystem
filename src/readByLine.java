import java.io.*;
import java.util.HashMap;
import java.util.Scanner;
import java.io.FileNotFoundException;

public class readByLine
{
    private static HashMap userHash = new HashMap();
    public readByLine() throws FileNotFoundException
    {
        Scanner linReader = new Scanner(new File("/Users/CK/Git/eclipse-workspace/ChatSystem/src/UserModel.txt"));

        while (linReader.hasNext())
        {
            String line = linReader.nextLine();
            String[] user = line.split(",");
            userHash.put(user[0], user[1]);
            // System.out.println(line);
        }
        linReader.close();

    }

    public static boolean login(String user, String passwd){
        String pwd = (String) userHash.get(user);
        if (pwd == null){
            System.out.println("User Not Found:" + user);
            return false;
        } else if ( pwd.equals(passwd) ){
            System.out.println("Logined:");
            return true;
        } else {
            System.out.println("Password not correct:" + passwd + "!=" + pwd);
            return false;
        }
    }
}