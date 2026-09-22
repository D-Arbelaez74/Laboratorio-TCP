package co.icesi.buscaminas;

import co.icesi.buscaminas.controllers.TCPController;

public class Main {

    public static void main(String[] args) {
        TCPController controller = new TCPController("localhost", 12345);
        controller.start();
    }
}
