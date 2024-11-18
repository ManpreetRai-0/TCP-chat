import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

// CLIENT CODE
public class tcpccs {
    private static volatile Boolean running = true;

    static class ClientChat implements Runnable {
        private Socket clientSocket;

        public ClientChat(Socket client) {
            this.clientSocket = client;
        }

        @Override
        public void run() {
            String sentenceFromServer;
            try (BufferedReader messageFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                while (running) {
                    try {
                        if ((sentenceFromServer = messageFromServer.readLine()) != null) {
                            System.out.println(sentenceFromServer);
                        }
                    } catch (IOException e) {
                        System.out.println("Error reading from server: " + e.getMessage());
                        break; // Exit the loop on error
                    }
                }
            } catch (IOException e) {
                System.out.println("Error initializing input stream: " + e.getMessage());
            }
        }

        public void stop() {
            running = false;
        }
    }

    static class ClientInput implements Runnable {
        private Socket clientSocket;
        private String userName;

        public ClientInput(Socket client, String userName) {
            this.clientSocket = client;
            this.userName = userName;
        }

        @Override
        public void run() {
            String sentenceToServer;
            try (BufferedReader clientMessage = new BufferedReader(new InputStreamReader(System.in))) {
                while (running) {
                    try {
                        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                        if ((sentenceToServer = clientMessage.readLine()) != null) {
                            if (sentenceToServer.equalsIgnoreCase("/quit")) {
                                clientSocket.close();
                                running = false;
                                break;
                            }
                            outToServer.writeBytes(userName + ": " + sentenceToServer + '\n');
                        }
                    } catch (IOException e) {
                        System.out.println("Error sending message: " + e.getMessage());
                        running = false; // Stop the loop on error
                    }
                }
            } catch (IOException e) {
                System.out.println("Error initializing input stream: " + e.getMessage());
            }
        }

        public void stop() {
            running = false;
        }
    }

    private static Socket connecting(String address, int serverPort) throws IOException {
        Socket clientSocket = new Socket(address, serverPort);
        System.out.println("Connected to the server. You can start sending messages.");
        return clientSocket;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: ./tcpccs <server_hostname> <username>");
            System.exit(1);
        }
        String address = args[0];
        String userName = args[1];
        int serverPort = 12345;

        try{
            try {
                Socket clientSocket = connecting(address, serverPort);

                ClientChat serverMessages = new ClientChat(clientSocket);
                Thread messagesForClient = new Thread(serverMessages);
                messagesForClient.start();

                ClientInput clientMessage = new ClientInput(clientSocket, userName);
                Thread messagesForServer = new Thread(clientMessage);
                messagesForServer.start();

                // Register a shutdown hook to handle Ctrl+C
                /*Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    running = false;  // Set running to false to stop loops
                    try {
                        clientSocket.close();
                        System.out.println("\nDisconnected from the server.");
                    } catch (IOException e) {
                        System.out.println("Error closing socket: " + e.getMessage());
                    }
                }));*/
                // Wait for threads to finish
                messagesForClient.join();
                messagesForServer.join();
            }catch (IOException e){
                System.out.println("Error: " + e.getMessage());
                System.exit(1);
            }
        }catch (InterruptedException e){
            System.out.println("interupted");
            Thread.currentThread().interrupt();
        }


        System.out.println("Client has finished execution.");
    }
}
