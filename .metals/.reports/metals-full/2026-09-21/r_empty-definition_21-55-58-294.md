error id: file:///C:/Users/dany2/Documents/Ingeniería%20de%20Sistemas/Semestre%205/Computación%20en%20Internet%201/compu-internet-1/client/src/main/java/co/icesi/buscaminas/controllers/TCPController.java:co/icesi/buscaminas/controllers/dtos/Response#
file:///C:/Users/dany2/Documents/Ingeniería%20de%20Sistemas/Semestre%205/Computación%20en%20Internet%201/compu-internet-1/client/src/main/java/co/icesi/buscaminas/controllers/TCPController.java
empty definition using pc, found symbol in pc: co/icesi/buscaminas/controllers/dtos/Response#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 410
uri: file:///C:/Users/dany2/Documents/Ingeniería%20de%20Sistemas/Semestre%205/Computación%20en%20Internet%201/compu-internet-1/client/src/main/java/co/icesi/buscaminas/controllers/TCPController.java
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
import co.icesi.buscaminas.controllers.dtos.R@@esponse;
import co.icesi.buscaminas.model.Cell;

public class TCPController {

    private String host; //El host al que se conecta, como localhost o un host en internet
    private int port; //El puerto por el que se conecta como puerto 12345
    private Gson gson; //Libreria de Google que sirve para convertir de Java a JSON
    //Por ejemplo si tengo un objeto Java como Request req y quiero enviarlo por el socket, lo debo convertir a JSON con gson.toJson(req)

    private Cell[][] board; //Mi matriz que es mi tablero
    private boolean gameOver; //Mensaje de que perdio
    private boolean win; //Mensaje de que gano

    public TCPController(String host, int port) { // Constructor que se conecta al puerto (No sé)
        this.host = host;
        this.port = port;
        this.gson = new GsonBuilder().create(); //Aquí se crea un objeto que permite trabajar con JSON
    }


    //Todo esto es logica de comunicación con el socket. Quizas se de esot ya hecho en el examen.
    public void sendRequest(String action, Map<String, String> data) { //Recibe como parametro nuestra accion(Destapar, Marcar, Salir), y los dos valores que queremos poner(fila, columna).
        try (Socket socket = new Socket(host, port); //Se hace un try catch grande en todo el metodo para poder confirmar que enviar el mensaje funciona o falla con el server

             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); //Cuando creo un socket tengo socket.getOutputStream() envia info

             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) { //Recibe info del socket

            Request req = new Request();
            req.action = action;
            if(data != null){
                req.data = data; //Si recibo datos los guardo
            } else {
                req.data = Map.of(); //Si no recibo datos pongo un mapa vacío
            }
            
            writer.write(gson.toJson(req) + "\n"); //Convertimos nuestro objeto Java en Json para poder escribir a traves del socket
            writer.flush(); // Forzamos a que el writer mande al socket todo lo que tenga para que no se acumule la info

            String line = reader.readLine(); //Aquí recibimos la respuesta del server
            if (line == null) return;
            
            Response res = gson.fromJson(line, Response.class); //Ya aquí lo que pasa es lo inverso, que es que recibimos de formato Json que nos da de respuesta el server
            //Con esto podemos ver si hay res.data y res.status
            
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

    //Logica con los dos bucles for para que se imprima el tablero especificando la cantidad de filas y columnas que se digitaron
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

    //La logica que quizas pueda importar más y que toque hacer en el parcial.
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

empty definition using pc, found symbol in pc: co/icesi/buscaminas/controllers/dtos/Response#