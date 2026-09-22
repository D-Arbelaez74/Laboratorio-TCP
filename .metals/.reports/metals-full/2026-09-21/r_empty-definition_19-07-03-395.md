error id: file:///C:/Users/dany2/Documents/Ingeniería%20de%20Sistemas/Semestre%205/Computación%20en%20Internet%201/compu-internet-1/client/src/main/java/co/icesi/buscaminas/controllers/TCPController.java:java/io/InputStreamReader#
file:///C:/Users/dany2/Documents/Ingeniería%20de%20Sistemas/Semestre%205/Computación%20en%20Internet%201/compu-internet-1/client/src/main/java/co/icesi/buscaminas/controllers/TCPController.java
empty definition using pc, found symbol in pc: java/io/InputStreamReader#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 127
uri: file:///C:/Users/dany2/Documents/Ingeniería%20de%20Sistemas/Semestre%205/Computación%20en%20Internet%201/compu-internet-1/client/src/main/java/co/icesi/buscaminas/controllers/TCPController.java
text:
```scala
package co.icesi.buscaminas.controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStr@@eamReader;
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

    private String host;
    private int port;
    private Gson gson;

    private Cell[][] board;
    private boolean gameOver;
    private boolean win;

    public TCPController(String host, int port) {
        this.host = host;
        this.port = port;
        this.gson = new GsonBuilder().create();
    }

    public void sendRequest(String action, Map<String, String> data) {
        try (Socket socket = new Socket(host, port);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            Request req = new Request();
            req.action = action;
            req.data = data != null ? data : Map.of();
            
            writer.write(gson.toJson(req) + "\n");
            writer.flush(); // Fundamental forzar el envío del JSON

            String line = reader.readLine();
            if (line == null) return;
            
            Response res = gson.fromJson(line, Response.class);
            
            // Actualizar el estado local con la respuesta
            if (res.data != null) {
                if (res.data.containsKey("board")) {
                    this.board = gson.fromJson(gson.toJson(res.data.get("board")), Cell[][].class);
                }
                if (res.data.containsKey("message")) {
                    System.out.println("-> " + res.data.get("message"));
                }
                this.gameOver = "GAME_OVER".equals(res.status) || Boolean.parseBoolean(String.valueOf(res.data.get("gameEnd")));
                this.win = Boolean.parseBoolean(String.valueOf(res.data.get("win")));
            }
        } catch (Exception e) {
            System.err.println("Error de red: " + e.getMessage());
        }
    }

    public void printBoard() {
        if (board == null) return;
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

    public void start() {
        System.out.println("Conectando con el servidor...");
        sendRequest("GET_BOARD", null);
        if (board == null) {
            System.err.println("No se pudo conectar. ¡Asegúrate de ejecutar el servidor primero!");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        while (true) {
            printBoard();
            if (gameOver) System.out.println(win ? "¡FELICIDADES, GANASTE!" : "¡GAME OVER, PISASTE UNA MINA!");

            System.out.println("\n1: Destapar | 2: Marcar | 3: Reiniciar | -1: Salir");
            System.out.print("Elige una opción: ");
            
            if (!scanner.hasNextInt()) break;
            int op = scanner.nextInt();
            if (op == -1) break;

            if (op == 3) {
                System.out.print("Filas, Columnas y Minas (Ej: 8 8 10): ");
                sendRequest("INIT_GAME", Map.of("n", scanner.next(), "m", scanner.next(), "minas", scanner.next()));
                gameOver = false; win = false;
            } else if (op == 1 || op == 2) {
                if (gameOver) {
                    System.out.println("Partida terminada. Reinicia con la opción 3.");
                    continue;
                }
                System.out.print("Fila y Columna (Ej: 0 0): ");
                // Usamos un if ternario para enviar SELECT_CELL o MARK_CELL en una sola línea
                sendRequest(op == 1 ? "SELECT_CELL" : "MARK_CELL", Map.of("i", scanner.next(), "j", scanner.next()));
            } else {
                System.out.println("Opción no válida.");
            }
        }
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: java/io/InputStreamReader#