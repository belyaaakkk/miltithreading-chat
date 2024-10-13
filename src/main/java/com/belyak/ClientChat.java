package com.belyak;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class ClientChat {
    public static void main(String[] args) {
        try (Socket socket = new Socket(InetAddress.getLocalHost(), ServerChat.PORT)) {

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())), true);
            Scanner consoleScanner = new Scanner(System.in);

            // login
            while (true) {
                System.out.println(reader.readLine()); // сервер запрашивает логин
                String userName = consoleScanner.nextLine();
                writer.println(userName); // отправляем логин на сервер

                String response = reader.readLine();
                if (response.startsWith("Welcome")) {
                    System.out.println(response);
                    break;
                } else {
                    System.out.println(response); // если логин недоступен
                }
            }

            Thread messageReaderThread = new Thread(new ServerMessageReader(reader));
            messageReaderThread.start();

            while (true) {
                String messageText = consoleScanner.nextLine();
                writer.println(messageText); // отправляем сообщение на сервер

                if (messageText.equalsIgnoreCase("EXIT")) {
                    writer.println("EXIT"); // отправляем на сервер команду выхода
                    break;
                }
            }

            messageReaderThread.join();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
