package com.belyak;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerChat {

    public static final int PORT = 8080;
    public static final int MAX_USERS = 5;

    protected static final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();
    protected static Set<String> users = new HashSet<>();
    protected static final Set<Socket> clientSockets = new HashSet<>();
    protected static Map<Socket, String> clientLogins = new HashMap<>();  // Сопоставление сокета и логина

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running...");

            // Отдельный поток для обработки и рассылки сообщений
            new Thread(new MessageDistributor()).start();

            while (true) {
                if (clientSockets.size() < MAX_USERS) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected!");

                    // Новый поток для обработки каждого клиента
                    new Thread(new ClientHandler(clientSocket)).start();
                } else {
                    System.out.println("Maximum number of users reached!");
                }
            }

        } catch (final IOException e) {
            System.err.println("Server was interrupted!");
            Thread.currentThread().interrupt();
        }
    }

    static class MessageDistributor implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    Message message = ServerChat.messageQueue.take();  // Получаем сообщение из очереди

                    for (Socket socket : ServerChat.clientSockets) {
                        // Проверяем, что отправитель не видит свои сообщения
                        if (!isSender(socket, message.getSender())) {
                            sendMessage(socket, message);
                        }
                    }
                }
            } catch (InterruptedException | IOException e) {
                System.err.println("Message distributor was interrupted!");
                Thread.currentThread().interrupt();
            }
        }

        private boolean isSender(Socket socket, String sender) {
            // Проверяем, что логин пользователя соответствует отправителю сообщения
            return ServerChat.clientLogins.get(socket).equals(sender);
        }

        private void sendMessage(Socket socket, Message message) throws IOException {
            PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            writer.println(message);
        }
    }
}
