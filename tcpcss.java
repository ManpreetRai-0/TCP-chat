import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class tcpcss{
    private static Boolean running = true;
    private static ServerSocket listen;

    public static void listenOnPort(int port)throws IOException{
        listen = new ServerSocket(port);
    }
    public static void main(String[] args) throws IOException{
        listenOnPort(12345);

        Socket clientSocket = listen.accept();
        System.out.println("New connection from " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
        DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));


        String sentenceFromClient;
        while (running) {
            while((sentenceFromClient = inFromClient.readLine()) != null){
                System.out.println(sentenceFromClient);
            }
            if(sentenceFromClient == null){
                System.out.println("Host disconnected, ip" + clientSocket.getInetAddress() + ", port" + clientSocket.getPort());
            }
        }

        outToClient.writeBytes("welcome to the server");
        
    }
}