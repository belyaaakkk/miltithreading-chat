package com.belyak;

import java.io.BufferedReader;
import java.io.IOException;

public class ServerMessageReader implements Runnable {
    private final BufferedReader reader;
    private volatile boolean running = true;


    public ServerMessageReader(final BufferedReader reader) {
        this.reader = reader;
    }

    @Override
    public void run() {
        try {
            String message;
            while (running && (message = reader.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            if (running) { // Исключение только если поток не был остановлен принудительно
                System.err.println("Connection is lost.");
            }
        } finally {
            stop();
        }
    }

    public void stop() {
        running = false;
        try {
            reader.close();
        } catch (IOException e) {
            System.err.println("Error with closing a InputStream.");

        }
    }
}
