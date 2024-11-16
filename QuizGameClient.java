import java.io.*;
import java.net.*;
import java.util.*;

public class QuizGameClient {
    public static void main(String[] args) throws Exception {
        String serverInfoFilePath = "./server_info.dat"; // get server information from this file
        String host = null; // host ip (get from server_info.dat)
        int portNum = -1; // host portnum (get from server_info.dat)
        boolean isServerDetailError = false; // flag for error at get from server_info.dat
        Socket socket = null; // connection socket with server
        try { // get ip, port number from server_info.dat
            DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(serverInfoFilePath)));
            host = dis.readUTF();
            portNum = dis.readInt();
            if (host == null || portNum == -1) {
                System.out.println("host or port number is not valid.");
                dis.close();
                isServerDetailError = true;
            } else {
                System.out.println("Got IP(" + host + "), port(" + portNum + ") from server_info.dat");
            }
            dis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //if error from getting ip, port from server_info.dat
        if (isServerDetailError) // ip : using localhost, port : 1234
            socket = new Socket("localhost", 1234);
        else
            socket = new Socket(host, portNum);

        System.out.println("Quiz will start...");
        Scanner kb = new Scanner(System.in); // to get answer from keyboard
        Scanner fromServer = new Scanner(new InputStreamReader(socket.getInputStream())); // to get message from server
        PrintWriter toServer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true); // to push message to server

        while (true) {
            String input = null; // to get answer input
            String serverMessage = fromServer.nextLine(); // get message
            System.out.println("Server> " + serverMessage); 
            if (serverMessage.contains("Total Score")) { // if got total score -> end
                socket.close();
                break;
            } else if (serverMessage.contains("Quiz : ")) { // if quiz : get input(answer)
                System.out.print("Input> ");
                input = kb.nextLine();
                toServer.println(input);
            } else if (serverMessage.contains("Correct") || serverMessage.contains("Incorrect")) { // if whether of correctness : send any to go next sequence
              // jump into next question (by just sending any line)
              toServer.println();
            } else { // else : error
              System.err.println("error : need syncronization with server algorithm");
              socket.close();
              break;
            }
            
            
        }
        
        // close streams
        kb.close();
        fromServer.close();
        toServer.close();
    }
}
