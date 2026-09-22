error id: file:///C:/Users/dany2/Documents/Ingeniería%20de%20Sistemas/Semestre%205/Computación%20en%20Internet%201/compu-internet-1/client/src/main/java/co/icesi/buscaminas/controllers/TCPController.java:co/icesi/buscaminas/model/Cell#
file:///C:/Users/dany2/Documents/Ingeniería%20de%20Sistemas/Semestre%205/Computación%20en%20Internet%201/compu-internet-1/client/src/main/java/co/icesi/buscaminas/controllers/TCPController.java
empty definition using pc, found symbol in pc: co/icesi/buscaminas/model/Cell#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 480
uri: file:///C:/Users/dany2/Documents/Ingeniería%20de%20Sistemas/Semestre%205/Computación%20en%20Internet%201/compu-internet-1/client/src/main/java/co/icesi/buscaminas/controllers/TCPController.java
text:
```scala
package co.icesi.buscaminas.controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import co.icesi.buscaminas.controllers.dtos.Request;
import co.icesi.buscaminas.controllers.dtos.Response;
import co.icesi.buscaminas.model.Ce@@ll;

public class TCPController {

    private String host;
    private int port;
    private Gson gson;

    // Matriz del tablero actual en juego
    private Cell[][] board;
    private boolean gameOver;
    private boolean win;
    private String lastMessage;

    public TCPController() {
        this("localhost", 12345);
    }

    public TCPController(String host, int port) {
        this.host = host;
        this.port = port;
        this.gson = new GsonBuilder().create();
        this.gameOver = false;
        this.win = false;
        this.lastMessage = "";
    }

    public Cell[][] getBoard() {
        return board;
    }

    public void setBoard(Cell[][] board) {
        this.board = board;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isWin() {
        return win;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    /**
     * Muestra en consola la matriz o tablero actual en el que se está jugando.
     */
    public void printBoard() {
        if (board == null || board.length == 0) {
            System.out.println("\n[Aviso] El tablero aún no ha sido cargado desde el servidor.");
            return;
        }

        int rows = board.length;
        int cols = board[0].length;

        System.out.println("\n========= TABLERO DE JUEGO (" + rows + "x" + cols + ") =========");
        System.out.print("    ");
        for (int c = 0; c < cols; c++) {
            System.out.printf("%2d ", c);
        }
        System.out.println();

        for (int r = 0; r < rows; r++) {
            System.out.printf("%2d [", r);
            for (int c = 0; c < cols; c++) {
                System.out.print(" " + board[r][c] + " ");
            }
            System.out.println("]");
        }
        System.out.println("==========================================");
    }

    /**
     * Envía una petición genérica al servidor TCP y actualiza la matriz del tablero.
     */
    public Response sendRequest(String action, Map<String, String> data) {
        try (Socket socket = new Socket(host, port);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            Request request = new Request();
            request.action = action;
            request.data = (data != null) ? data : new HashMap<>();

            String jsonRequest = gson.toJson(request);
            writer.write(jsonRequest);
            writer.newLine();
            writer.flush();

            String jsonResponse = reader.readLine();
            Response response = gson.fromJson(jsonResponse, Response.class);

            updateInternalState(response);
            return response;
        } catch (Exception e) {
            System.err.println("Error al comunicarse con el servidor en " + host + ":" + port + " -> " + e.getMessage());
            return null;
        }
    }

    /**
     * Actualiza la matriz del tablero y estados del juego a partir de la respuesta del servidor.
     */
    private void updateInternalState(Response response) {
        if (response == null || response.data == null) {
            return;
        }

        if (response.data.containsKey("board")) {
            Object boardObj = response.data.get("board");
            this.board = gson.fromJson(gson.toJson(boardObj), Cell[][].class);
        }

        if (response.data.containsKey("message")) {
            this.lastMessage = String.valueOf(response.data.get("message"));
        }

        if (response.data.containsKey("gameEnd")) {
            this.gameOver = Boolean.parseBoolean(String.valueOf(response.data.get("gameEnd")));
        } else if ("GAME_OVER".equals(response.status)) {
            this.gameOver = true;
        }

        if (response.data.containsKey("win")) {
            this.win = Boolean.parseBoolean(String.valueOf(response.data.get("win")));
        }
    }

    /**
     * Consulta el estado del tablero al servidor y lo muestra en pantalla.
     */
    public Response fetchBoard() {
        Response resp = sendRequest("GET_BOARD", new HashMap<>());
        printBoard();
        return resp;
    }

    /**
     * Selecciona (destapa) una casilla en las coordenadas (i, j).
     */
    public Response selectCell(int i, int j) {
        Map<String, String> data = new HashMap<>();
        data.put("i", String.valueOf(i));
        data.put("j", String.valueOf(j));

        Response resp = sendRequest("SELECT_CELL", data);
        if (lastMessage != null && !lastMessage.isEmpty()) {
            System.out.println("-> " + lastMessage);
        }
        printBoard();

        if (gameOver) {
            if (win) {
                System.out.println("¡FELICIDADES! ¡Has ganado la partida!");
            } else {
                System.out.println("¡GAME OVER! Has pisado una mina.");
            }
        }
        return resp;
    }

    /**
     * Marca o desmarca una casilla con bandera (M) en las coordenadas (i, j).
     */
    public Response markCell(int i, int j) {
        Map<String, String> data = new HashMap<>();
        data.put("i", String.valueOf(i));
        data.put("j", String.valueOf(j));

        Response resp = sendRequest("MARK_CELL", data);
        if (lastMessage != null && !lastMessage.isEmpty()) {
            System.out.println("-> " + lastMessage);
        }
        printBoard();
        return resp;
    }

    /**
     * Inicializa una nueva partida en el servidor con dimensiones n x m y cantidad de minas.
     */
    public Response initGame(int n, int m, int minas) {
        Map<String, String> data = new HashMap<>();
        data.put("n", String.valueOf(n));
        data.put("m", String.valueOf(m));
        data.put("minas", String.valueOf(minas));

        this.gameOver = false;
        this.win = false;
        Response resp = sendRequest("INIT_GAME", data);
        System.out.println("-> Partida reiniciada (" + n + "x" + m + ", " + minas + " minas).");
        printBoard();
        return resp;
    }

    /**
     * Revela todo el tablero en el servidor.
     */
    public Response showAll() {
        Response resp = sendRequest("SHOW_ALL", new HashMap<>());
        printBoard();
        return resp;
    }

    /**
     * Inicia el flujo interactivo de control de juego directamente desde el controller.
     */
    public void start() {
        System.out.println("==========================================");
        System.out.println("      BUSCAMINAS - CLIENTE TCP");
        System.out.println("==========================================");
        System.out.println("Conectando con el servidor en " + host + ":" + port + "...");

        Response initialResponse = sendRequest("GET_BOARD", new HashMap<>());
        if (initialResponse == null) {
            System.err.println("\n[ERROR] No fue posible conectar con el servidor Buscaminas.");
            System.err.println("Asegúrate de que la aplicación 'server' esté ejecutándose.");
            return;
        }

        printBoard();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- Menú de Control ---");
            System.out.println("1: Seleccionar celda (destapar)");
            System.out.println("2: Marcar / Desmarcar celda con bandera (M)");
            System.out.println("3: Reiniciar partida (nuevo tablero)");
            System.out.println("-1: Salir");
            System.out.print("Elige una opción: ");

            if (!scanner.hasNextInt()) {
                break;
            }
            int option = scanner.nextInt();

            if (option == -1) {
                System.out.println("Saliendo del juego...");
                break;
            }

            if (option == 1) {
                if (gameOver) {
                    System.out.println("[!] La partida actual ya terminó. Elige la opción 3 para reiniciar.");
                    continue;
                }
                System.out.print("Ingresa fila (i): ");
                int i = scanner.nextInt();
                System.out.print("Ingresa columna (j): ");
                int j = scanner.nextInt();
                selectCell(i, j);
            } else if (option == 2) {
                if (gameOver) {
                    System.out.println("[!] La partida actual ya terminó. Elige la opción 3 para reiniciar.");
                    continue;
                }
                System.out.print("Ingresa fila (i): ");
                int i = scanner.nextInt();
                System.out.print("Ingresa columna (j): ");
                int j = scanner.nextInt();
                markCell(i, j);
            } else if (option == 3) {
                System.out.print("Número de filas (ej: 8): ");
                int n = scanner.nextInt();
                System.out.print("Número de columnas (ej: 8): ");
                int m = scanner.nextInt();
                System.out.print("Cantidad de minas (ej: 10): ");
                int minas = scanner.nextInt();
                initGame(n, m, minas);
            } else {
                System.out.println("[!] Opción no reconocida. Intenta nuevamente.");
            }
        }
        System.out.println("Conexión con el cliente finalizada.");
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: co/icesi/buscaminas/model/Cell#