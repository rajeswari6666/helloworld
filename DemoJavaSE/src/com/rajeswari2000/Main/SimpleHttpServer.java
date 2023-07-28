package com.rajeswari2000.Main;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleHttpServer {
    private static final int PORT = 8080;
    private static final int ACCEPTOR_THREAD_COUNT = 1;
    private static final int WORKER_THREAD_COUNT = 4;
    private static final int MAX_QUEUE_SIZE = 10;

    private static final BlockingQueue<Socket> requestQueue = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE);
    private static final ExecutorService acceptorExecutor = Executors.newFixedThreadPool(ACCEPTOR_THREAD_COUNT);
    private static final ExecutorService workerExecutor = Executors.newFixedThreadPool(WORKER_THREAD_COUNT);

    public static void main(String[] args) {
        startAcceptorThreads();
        startWorkerThreads();
        
    }

    private static void startAcceptorThreads() {
        for (int i = 0; i < ACCEPTOR_THREAD_COUNT; i++) {
            acceptorExecutor.execute(() -> {
                try {
                    ServerSocket serverSocket = new ServerSocket(PORT);
                    System.out.println("Acceptor thread started, listening on port " + PORT);
                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("Accepted connection from " + clientSocket.getRemoteSocketAddress());
                        requestQueue.put(clientSocket);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            
        }
    }

    private static void startWorkerThreads() {
        for (int i = 0; i < WORKER_THREAD_COUNT; i++) {
            workerExecutor.execute(() -> {
                while (true) {
                    try {
                        Socket clientSocket = requestQueue.take();
                        processRequest(clientSocket);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private static void processRequest(Socket clientSocket) {
        try {
            // Simulate processing the request
            Thread.sleep(1000);
            OutputStream out = clientSocket.getOutputStream();
            String response = "HTTP/1.1 200 OK\r\nContent-Length: 12\r\n\r\nHello World!";
            out.write(response.getBytes());
            out.flush();
            out.close();
            clientSocket.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
