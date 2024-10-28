import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
//      CLIENT CODE

public class tcpccs{
    //controls threads running
    private static volatile Boolean running = true;


    static class ClientChat implements Runnable{
        private Socket clientSocket;

        public ClientChat(Socket client){
            this.clientSocket = client;
        }

        @Override
        public void run(){

            String sentenceFromServer;
            try{
                BufferedReader messageFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            //reading froms messages server
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
        private String userName;

        public ClientInput(Socket client, String userName){
            this.clientSocket = client;
            this.userName = userName;

        }

        @Override
        public void run(){

            String sentenceToServer;
            try{
                BufferedReader clientMessage = new BufferedReader(new InputStreamReader(System.in));
            
            //running while server is open or till we quit, if we cant writebytes server is disconnected
                while (running) {
                    DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                    while((sentenceToServer = clientMessage.readLine()) != null){
                        if(sentenceToServer.equalsIgnoreCase("/quit")){
                            clientSocket.close();
                            running = false;
                            break;
                        }
                        outToServer.writeBytes(userName + ": " + sentenceToServer + '\n');

                    }
                }
            } catch(IOException e) {
                System.out.println("Server Disconnected");
                System.exit(1);
            }

        }
        public void stop(){
            running = false;
        }
    }

    private static Socket connecting(String address, int serverPort) throws Exception {
        Socket clientSocket = new Socket(address, serverPort);
        System.out.println("Connected to the server. You can start sending messages.");
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


        try{ 
            Socket clientSocket = connecting(address, serverPort);

            ClientChat serverMessages = new ClientChat(clientSocket);
            Thread messagesForClient = new Thread(serverMessages);
            messagesForClient.start();

            ClientInput clientMessage = new ClientInput(clientSocket, userName);
            Thread messagesForServer = new Thread(clientMessage);
            messagesForServer.start();
        
        
        //ctrl+c will stop all threads running and close the socket
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;  // Set running to false to stop loops
            try{
                clientSocket.close();
                System.out.println();
            }catch (Exception e){
                System.out.println("Error closing socket");
            }
        }));

        //wait for threads to stop running before main finishes
        messagesForClient.join();
        messagesForServer.join();
        }catch(IOException e){
            System.out.println("No Connection");
            System.exit(1);
        }

    }
}