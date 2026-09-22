package co.icesi.buscaminas.controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import co.icesi.buscaminas.controllers.dtos.Request;
import co.icesi.buscaminas.controllers.dtos.Response;
import co.icesi.buscaminas.model.Cell;
import co.icesi.buscaminas.services.ServicesImpl;

public class TCPController {

    // Lista de clientes conectados para difundir (broadcast) las actualizaciones en vivo
    private List<BufferedWriter> clients = new CopyOnWriteArrayList<>();
    
    private ServicesImpl services;
    private ServerSocket serverSocket;
    private boolean running;
    private ExecutorService executor;
    private Gson gson;

    public TCPController(ServicesImpl services) {
        this(services, 12345);
    }

    public TCPController(ServicesImpl services, int port) {
        this.services = services;
        try {
            serverSocket = new ServerSocket(port);
            // Usamos newCachedThreadPool para aceptar múltiples clientes sin límite fijo
            executor = Executors.newCachedThreadPool();
            gson = new GsonBuilder().create();
        } catch (Exception e) {
            e.printStackTrace();
        }
        running = true;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * Envía una respuesta a TODOS los clientes conectados de manera segura.
     * Si un cliente se desconecta, se remueve automáticamente de la lista.
     */
    public void broadcast(Response response) {
        String json = gson.toJson(response);
        for (BufferedWriter client : clients) {
            try {
                client.write(json);
                client.newLine();
                client.flush();
            } catch (Exception e) {
                // Si falla el envío, el cliente se desconectó
                clients.remove(client);
            }
        }
    }

    public void startService() {
        System.out.println("TCP Service started on port " + serverSocket.getLocalPort());
        while (running) {
            try {
                executor.execute(new TCPClientHandler(serverSocket.accept(), services));
            } catch (Exception e) {
                if (!running) break;
                e.printStackTrace();
            }
        }
        try {
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class TCPClientHandler implements Runnable {

        private Socket clientSocket;
        private ServicesImpl services;

        public TCPClientHandler(Socket clientSocket, ServicesImpl services) {
            this.clientSocket = clientSocket;
            this.services = services;
        }

        @Override
        public void run() {
            BufferedWriter writer = null;
            try {
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));

                writer = new BufferedWriter(
                        new OutputStreamWriter(clientSocket.getOutputStream()));

                // Registramos al cliente en la lista para futuros broadcasts
                clients.add(writer);

                // Enviar inmediatamente el estado actual del tablero al cliente recién conectado
                Response initialResponse = new Response();
                initialResponse.status = "OK";
                initialResponse.data = new HashMap<>();
                initialResponse.data.put("board", services.printBoard());
                writer.write(gson.toJson(initialResponse));
                writer.newLine();
                writer.flush();

                while (true) {
                    String line = reader.readLine();
                    if (line == null) break;

                    Request rq = gson.fromJson(line, Request.class);
                    if (rq == null || rq.action == null) continue;

                    Map<String, String> data = rq.data;
                    Response response = new Response();
                    response.data = new HashMap<>();

                    String action = rq.action.toUpperCase();

                    switch (action) {

                        case "SELECT_CELL":
                            int i = Integer.parseInt(data.get("i"));
                            int j = Integer.parseInt(data.get("j"));

                            try {
                                boolean resp = services.selectCell(i, j);
                                response.status = "OK";
                                response.data.put("win", resp);
                                response.data.put("gameEnd", resp);
                                response.data.put("message",
                                        "Celda (" + i + ", " + j + ") despejada.");
                            } catch (Exception e) {
                                response.status = "GAME_OVER";
                                response.data.put("gameEnd", true);
                                response.data.put("win", false);
                                response.data.put("message", e.getMessage());
                            }

                            Cell[][] board = services.printBoard();
                            response.data.put("board", board);
                            // Notificar la jugada a TODOS los clientes conectados
                            broadcast(response);
                            break;

                        case "MARK_CELL":
                            i = Integer.parseInt(data.get("i"));
                            j = Integer.parseInt(data.get("j"));

                            try {
                                services.markCell(i, j);
                                response.status = "OK";
                                response.data.put("message",
                                        "Celda (" + i + ", " + j + ") marcada/desmarcada.");
                            } catch (Exception e) {
                                response.status = "ERROR";
                                response.data.put("message", e.getMessage());
                            }

                            board = services.printBoard();
                            response.data.put("board", board);
                            // Notificar el marcado a TODOS los clientes conectados
                            broadcast(response);
                            break;

                        case "SHOW_ALL":
                        case "SOW_ALL":
                            services.showAll(true);
                            board = services.printBoard();
                            response.status = "OK";
                            response.data.put("board", board);
                            broadcast(response);
                            break;

                        case "GET_BOARD":
                            board = services.printBoard();
                            response.status = "OK";
                            response.data.put("board", board);
                            // Responder solo al cliente que lo pidió
                            writer.write(gson.toJson(response));
                            writer.newLine();
                            writer.flush();
                            break;

                        case "INIT_GAME":
                            i = Integer.parseInt(data.get("n"));
                            j = Integer.parseInt(data.get("m"));
                            int m = Integer.parseInt(data.get("minas"));

                            services.initGame(i, j, m);

                            board = services.printBoard();
                            response.status = "OK";
                            response.data.put("message", "Juego reiniciado con éxito.");
                            response.data.put("board", board);
                            // Notificar el reinicio a TODOS los clientes
                            broadcast(response);
                            break;

                        default:
                            break;
                    }
                }

            } catch (Exception e) {
                // Conexión interrumpida o cerrada por el cliente
            } finally {
                if (writer != null) {
                    clients.remove(writer);
                }
                try {
                    clientSocket.close();
                } catch (Exception ignored) {}

                System.out.println("Client disconnected: " + clientSocket.getInetAddress());
            }
        }
    }
}