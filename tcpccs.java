import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
//      CLIENT CODE

public class tcpccs{
    private static Boolean running = true;
    private static Socket clientSocket;

    static class ClientChat implements Runnable{
        private Socket clientSocket;
        //private volatile Boolean running = true;

        public ClientChat(Socket client){
            this.clientSocket = client;
        }

        @Override
        public void run(){

            String sentenceFromServer;
            try{
                BufferedReader messageFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                while (running) {
                        if((sentenceFromServer = messageFromServer.readLine()) != null){
                            System.out.println(sentenceFromServer);
                        }
                }
            } catch(IOException e) {
                if(!clientSocket.isClosed())
                    System.out.println(e.getMessage());
            }

        }
        public void stop(){
            running = false;
        }
    }
    static class ClientInput implements Runnable{
        private Socket clientSocket;
        //private volatile Boolean running = true;

        public ClientInput(Socket client){
            this.clientSocket = client;
        }

        @Override
        public void run(){

            String sentenceToServer;
            try{
                BufferedReader clientMessage = new BufferedReader(new InputStreamReader(System.in));

                while (running) {
                    DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                    while((sentenceToServer = clientMessage.readLine()) != null){
                        if(sentenceToServer.equals("/quit")){
                            clientSocket.close();
                            running = false;
                            break;
                        }
                        outToServer.writeBytes(sentenceToServer + '\n');

                    }
                }
            } catch(IOException e) {
                System.out.println("Connection2");
            }

        }
        public void stop(){
            running = false;
        }
    }

    private static Socket connecting(String address, int serverPort) throws Exception {
        Socket clientSocket = new Socket(address, serverPort);
        return clientSocket;
    }
    
    public static void main(String[] args) throws Exception {
        if(args.length != 2){
            System.err.println("Usage: ./tcpccs <server_hostname> <username>");
            System.exit(1);
        }
        String address = args[0];
        String userName = args[1];
        int serverPort = 12345;


        Socket clientSocket = connecting(address, serverPort);

        ClientChat serverMessages = new ClientChat(clientSocket);
        Thread messagesForClient = new Thread(serverMessages);
        messagesForClient.start();

        ClientInput clientMessage = new ClientInput(clientSocket);
        Thread messagesForServer = new Thread(clientMessage);
        messagesForServer.start();
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;  // Set running to false to stop loops
            try{
                clientSocket.close();
                System.out.println();
            }catch (Exception e){
                System.out.println("Error closing socket");
            }
        }));


        messagesForClient.join();
        messagesForServer.join();

    }
}