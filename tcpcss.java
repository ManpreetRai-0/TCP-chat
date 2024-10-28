import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class tcpcss {
    private static volatile Boolean running = true;
    private static ServerSocket listen;
    private static ConcurrentHashMap<Socket, String> clientLookUp = new ConcurrentHashMap<>();

    static class Client implements Runnable {
        private Socket clientSocket;
        private volatile Boolean running = true;

        public Client(Socket client) {
            this.clientSocket = client;
        }

        @Override
        public void run() {
            try {
                BufferedReader messageFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String sentenceFromClient;

                while (running) {
                    if ((sentenceFromClient = messageFromClient.readLine()) != null) {
                        System.out.println("Client: " + sentenceFromClient);
                        if (clientLookUp.size() > 1)
                            broadcastMessage(sentenceFromClient);
                    } else {
                        System.out.println("Host disconnected, ip " + this.clientSocket.getInetAddress().getHostAddress() + ", port " + this.clientSocket.getPort());
                        break; // Break on null read (disconnection)
                    }
                }
            } catch (IOException e) {
                System.out.println("Exception in client thread: " + e.getMessage());
            } finally {
                cleanup();
            }
        }

        private void broadcastMessage(String message) {
            clientLookUp.forEach((connectedClient, activityStatus)->{
                if(connectedClient != this.clientSocket && activityStatus.equalsIgnoreCase("active")){
                    try {
                        DataOutputStream outToClient = new DataOutputStream(connectedClient.getOutputStream());
                        outToClient.writeBytes(message + '\n');
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }

            });
        }

        private void cleanup() {
            running = false;
            clientLookUp.remove(this.clientSocket);
            try {
                this.clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
        }

        public void stop(){
            running = false;
        }
    }

    public static void listenOnPort(int port)throws IOException{
        listen = new ServerSocket(port);
        System.out.println("Listener on port " + port);
    }

    public static void main(String[] args) throws IOException{
        listenOnPort(12345);

        System.out.println("Waiting for connections ...");


    //detects ctrl+c and closes all sockets in clientLoopUp, and Server listen socket
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;
            try {
                for (Socket socket : clientLookUp.keySet()) {
                    socket.close();
                }
                if (listen != null && !listen.isClosed()) {
                    listen.close();
                }
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
        }));

        int i=0;
        while(running){
            Socket clientSocket = listen.accept();
            Client clientConnection = new Client(clientSocket);
            Thread newThread = new Thread(clientConnection);
            System.out.println("New connection, thread name is " + newThread.getName() + ", ip is: " + clientSocket.getInetAddress() + ", port: " + clientSocket.getPort());
            clientLookUp.put(clientSocket, "active");
            System.out.println("Adding to list of sockets as " + i);
            newThread.start();
            i++;
        }

        
    }
}