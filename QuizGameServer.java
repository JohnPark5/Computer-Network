import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.text.SimpleDateFormat;

public class QuizGameServer {
    public static void main(String[] args) throws Exception {
        String ip = "127.0.0.1"; // used self ip (== "localhost")
        int portNum = 13579; // any portnum that is valid
        String serverInfoFilePath = "./server_info.dat"; // to make to use by client side.
        int maxThreadNum = 10; // max threads to get

        try { // create server_info.dat file to make client (same computer) get in to it.
            DataOutputStream dos = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(serverInfoFilePath)));
            dos.writeUTF(ip);
            dos.writeInt(portNum);
            dos.close();
            System.out.println("server_info.dat file created");
        } catch (Exception e) {
            e.printStackTrace();
            // if error, make port to 1234, since client can't open the file, 1234 will be default
            portNum = 1234;
        }

        // open server socket
        ServerSocket listener = new ServerSocket(portNum);
        System.out.println("The QuizGame server is running . . .");

        // make thread
        ExecutorService pool = Executors.newFixedThreadPool(maxThreadNum);

        while (true) { // wait and execute the quiz program simultaniously by thread
            Socket socket = listener.accept();
            pool.execute(new QuizGame(socket));
        }
    }
}

class QuizGame implements Runnable { // implement runnable class to use ThreadPool
    private Socket socket;

    QuizGame(Socket socket) { // Constructor initalizing socket (connected)
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("Connected to - " + socket);
        Scanner fromClient = null; // to get message from client
        PrintWriter toClient = null; // to push message to client

        try {
            int score = 0; // score initalation
            fromClient = new Scanner(new InputStreamReader(socket.getInputStream()));
            toClient = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true); 
            // init quizs and answers
            String[] quiz = { "What is initials of our class professor? (ex. HGD)",
                    "What is main subject(purpose) of this project(or quiz, hw)? (one word)",
                    "what's totay's date?(ex: 24/11/16)" };
            String[] ans = { "CJH", "socket", new SimpleDateFormat("YY/MM/dd").format(new Date()) };
            int current = -1; // to reapeat number of quiz, save curret sequence
            
            while (++current < quiz.length) { // if there's 3 quiz, repeat 3 times
                toClient.println("Quiz : " + quiz[current]);
                String clientMessage = fromClient.nextLine(); // getting client's answer
                if(ans[current].equalsIgnoreCase(clientMessage)) { // check client's answer (don't care about capital/small letters)
                    // if right, score + 1, say correnct to client
                    score ++; 
                    toClient.println("Correct!");
                }
                else {
                    // if not, just tell Incorrect to client
                    toClient.println("Incorrect");
                }
                // get any input (ex. just enter). (to go to next quiz)
                // this is needed because using toClient is using autoFlush. 
                fromClient.nextLine(); 
            }
            //quiz ended, send total score to client
            toClient.println("Total Score : " + score + " / " + quiz.length);


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                //close streams
                socket.close();
                fromClient.close();
                toClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Closed : " + socket);
        }
    }

}