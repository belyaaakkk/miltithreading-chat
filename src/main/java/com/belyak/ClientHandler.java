package com.belyak;

import java.io.*;
import java.net.Socket;
import java.util.Map;

import static com.belyak.ServerChat.*;

public class ClientHandler implements Runnable {
    private final Socket socket;

    public ClientHandler(final Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

            // Получаем уникальный логин
            String userName;
            while (true) {
                writer.println("Enter your username:");
                userName = reader.readLine();

                synchronized (users) {
                    if (!users.contains(userName)) {
                        users.add(userName);
                        clientSockets.add(socket);
                        clientLogins.put(socket, userName); // Сохраняем логин пользователя

                        writer.println("Welcome, " + userName + "!");
                        broadcastMessage(userName + " joined the chat!");
                        break;
                    } else {
                        writer.println("Username is already taken. Try again.");
                    }
                }
            }

            // Обработка сообщений от клиента
            while (true) {
                String messageText = reader.readLine();
                if (messageText.equalsIgnoreCase("EXIT")) {
                    break;
                }

                // Проверка на личное сообщение
                if (messageText.startsWith("@")) {
                    String[] parts = messageText.split(" ", 2);
                    String recipient = parts[0].substring(1);  // Имя получателя
                    String privateMessage = parts[1];          // Сообщение

                    sendPrivateMessage(userName, recipient, privateMessage);
                } else {
                    // Обычное сообщение для всех
                    Message message = new Message(userName, messageText);
                    messageQueue.put(message);
                }
            }


            synchronized (users) {
                users.remove(userName);
                clientSockets.remove(socket);
                clientLogins.remove(socket); // Удаляем логин при выходе
                broadcastMessage(userName + " left the chat.");
            }

            socket.close();

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void broadcastMessage(final String messageText) throws InterruptedException {
        Message message = new Message("Server", messageText);
        messageQueue.put(message);
    }

    private void sendPrivateMessage(final String sender, final String recipient, final String messageText) throws IOException {
        synchronized (clientLogins) {
            // Поиск сокета получателя
            Socket recipientSocket = null;

            for (Map.Entry<Socket, String> entry : clientLogins.entrySet()) {
                if (entry.getValue().equals(recipient)) {
                    recipientSocket = entry.getKey();
                    break;
                }
            }

            if (recipientSocket != null) {
                PrintWriter writer = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(recipientSocket.getOutputStream())), true);
                writer.println("Private message from " + sender + ": " + messageText);
            } else {
                // Уведомление отправителя, что получатель не найден
                PrintWriter writer = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);
                writer.println("User " + recipient + " not found.");
            }
        }
    }

}
