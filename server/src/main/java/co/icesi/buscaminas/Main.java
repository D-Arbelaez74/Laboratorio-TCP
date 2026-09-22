package co.icesi.buscaminas;

import java.util.Scanner;

import co.icesi.buscaminas.controllers.TCPController;
import co.icesi.buscaminas.model.BoardGame;
import co.icesi.buscaminas.services.ServicesImpl;

public class Main {

    public static void main(String[] args)
    {
        ServicesImpl serv = new ServicesImpl();
        new Thread(() -> apply(serv.getGame())).start();
        // TCPController controller = new TCPController(serv);
        // controller.startService();

        TCPController iceController = new TCPController(serv);
        iceController.startService();
    }
    public static void apply(BoardGame bg) {
        int n = bg.getBoard().length;
        int m = bg.getBoard()[0].length;
        System.out.println("Minas en el juego: " + bg.getMines());
        bg.printBoard();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Usa el formato: <operacion> <i> <j>");
        System.out.println("operación 1: seleccionar celda, operación 2: marcar/desmarcar celda, -1 -1 -1 para salir");

        while (true) {
            System.out.print("\nIngresa tu jugada (<op> <i> <j>): ");
            if (!scanner.hasNextInt()) break;
            int operation = scanner.nextInt();
            if (operation == -1) break;
            int i = scanner.nextInt();
            int j = scanner.nextInt();

            try {
                if (operation == 2) {
                    bg.markCell(i, j);
                    System.out.println("-> Celda (" + i + ", " + j + ") marcada/desmarcada.");
                } else if (operation == 1) {
                    boolean win = bg.selectCell(i, j);
                    System.out.println("-> ¡Seguro! No había mina en (" + i + ", " + j + ").");
                    if (win) {
                        bg.printBoard();
                        System.out.println("¡FELICIDADES! Has ganado el juego.");
                        break;
                    }
                }
                bg.printBoard();
            } catch (RuntimeException e) {
                bg.printBoard();
                System.out.println(e.getMessage());
                break;
            }
        }
        System.out.println("Fin del juego.");
        scanner.close();
    }
}