error id: file:///C:/Users/dany2/Documents/Ingeniería%20de%20Sistemas/Semestre%205/Computación%20en%20Internet%201/Laboratorio-TCP/client/src/main/java/co/icesi/buscaminas/controllers/TCPController.java:_empty_/Request#
file:///C:/Users/dany2/Documents/Ingeniería%20de%20Sistemas/Semestre%205/Computación%20en%20Internet%201/Laboratorio-TCP/client/src/main/java/co/icesi/buscaminas/controllers/TCPController.java
empty definition using pc, found symbol in pc: _empty_/Request#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 1563
uri: file:///C:/Users/dany2/Documents/Ingeniería%20de%20Sistemas/Semestre%205/Computación%20en%20Internet%201/Laboratorio-TCP/client/src/main/java/co/icesi/buscaminas/controllers/TCPController.java
text:
```scala
package co.icesi.buscaminas.controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import co.icesi.buscaminas.controllers.dtos.Request;
import co.icesi.buscaminas.controllers.dtos.Response;
import co.icesi.buscaminas.model.Cell;

public class TCPController {

    private String host; // El host al que se conecta, como localhost
    private int port;    // El puerto por el que se conecta (ej. 12345)
    private Gson gson; //Libreria de google para convertir de Java a Json

    // Estado local del juego
    private Cell[][] board;
    private boolean gameOver;
    private boolean win;

    // Conexión persistente con el servidor
    private Socket socket; 
    private BufferedWriter writer;
    private BufferedReader reader;

    public TCPController(String host, int port) { //Constructor que hace a un cliente y se corre en localhost y conecta a un puerto especifico
        this.host = host;
        this.port = port;
        this.gson = new GsonBuilder().create(); //Se crea un objeto que permite trabajar con Json
    }

    /**
     * Envía una acción y sus parámetros al servidor a través del socket persistente.
     * Ya no abre ni cierra el socket por cada petición, permitiendo comunicación bidireccional.
     */
    public void sendRequest(String action, Map<String, String> data) {
        try {
            Requ@@est req = new Request();
            req.action = action;
            req.data = (data != null) ? data : Map.of();

            writer.write(gson.toJson(req) + "\n");
            writer.flush(); // Forzamos el envío inmediato
        } catch (Exception e) {
            System.out.println("Error de red enviando petición: " + e.getMessage());
        }
    }

    // Imprime el tablero con formato claro
    public void printBoard() {
        if (board == null)
            return;
        System.out.println("\n=== TABLERO ===");
        System.out.print("   ");
        for (int c = 0; c < board[0].length; c++) System.out.printf("%2d ", c);
        System.out.println();

        for (int r = 0; r < board.length; r++) {
            System.out.printf("%2d ", r);
            for (int c = 0; c < board[0].length; c++) {
                System.out.print(" " + board[r][c] + " ");
            }
            System.out.println();
        }
        System.out.println("===============");
    }

    /**
     * Bucle principal interactivo del cliente:
     * 1. Abre el socket una sola vez.
     * 2. Inicia un hilo en segundo plano que escucha actualizaciones del servidor en vivo.
     * 3. El hilo principal lee la entrada del usuario por consola y envía las acciones.
     */
    public void start() {
        System.out.println("Conectando con el servidor en " + host + ":" + port + "...");
        try {
            socket = new Socket(host, port);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            System.out.println("No se pudo conectar: " + e.getMessage());
            System.out.println("¡Asegúrate de ejecutar el servidor primero!");
            return;
        }

        // Hilo en segundo plano que escucha las actualizaciones del servidor en vivo (Push)
        Thread listenerThread = new Thread(() -> {
            try {
                String line;
                // Escucha de manera continua cualquier mensaje que difunda el servidor
                while ((line = reader.readLine()) != null) {
                    Response res = gson.fromJson(line, Response.class);

                    if (res != null && res.data != null) {
                        if (res.data.containsKey("board")) {
                            this.board = gson.fromJson(gson.toJson(res.data.get("board")), Cell[][].class);
                        }
                        if (res.data.containsKey("message")) {
                            System.out.println("\n-> " + res.data.get("message"));
                        }
                        this.gameOver = "GAME_OVER".equals(res.status) || Boolean.parseBoolean(String.valueOf(res.data.get("gameEnd")));
                        this.win = Boolean.parseBoolean(String.valueOf(res.data.get("win")));

                        // Imprimir el tablero actualizado en vivo
                        printBoard();

                        if (gameOver) {
                            System.out.println(win ? "¡FELICIDADES, GANASTE!" : "¡GAME OVER, PISASTE UNA MINA!");
                        }

                        System.out.println("\n1: Destapar | 2: Marcar | 3: Reiniciar | -1: Salir");
                        System.out.print("Elige una opción: ");
                    }
                }
            } catch (Exception e) {
                if (socket != null && !socket.isClosed()) {
                    System.out.println("\nConexión con el servidor terminada.");
                }
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();

        // Esperar unos instantes a que llegue el tablero inicial del servidor
        for (int k = 0; k < 10 && board == null; k++) {
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        }

        Scanner scanner = new Scanner(System.in);

        while (true) {
            if (!scanner.hasNextInt()) break;
            int op = scanner.nextInt();
            if (op == -1) break;

            if (op == 3) {
                System.out.print("Filas, Columnas y Minas (Ej: 8 8 10): ");
                sendRequest("INIT_GAME", Map.of("n", scanner.next(), "m", scanner.next(), "minas", scanner.next()));
                gameOver = false;
                win = false;
            } else if (op == 1 || op == 2) {
                if (gameOver) {
                    System.out.println("Partida terminada. Reinicia con la opción 3.");
                    continue;
                }
                System.out.print("Fila y Columna (Ej: 0 0): ");
                sendRequest(op == 1 ? "SELECT_CELL" : "MARK_CELL", Map.of("i", scanner.next(), "j", scanner.next()));
            } else {
                System.out.println("Opción no válida.");
            }
        }

        try {
            socket.close();
        } catch (Exception ignored) {}
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/Request#