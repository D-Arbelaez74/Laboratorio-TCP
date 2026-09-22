# Guía Rápida para Clientes TCP en Exámenes

Cuando te enfrentes a un parcial donde debes construir un cliente TCP interactivo en Java, el objetivo es **hacerlo funcionar rápido, con código corto y fácil de leer**. No necesitas métodos getter/setter innecesarios ni separar cada comando en un método distinto si todos hacen lo mismo.

## Estructura de 4 Pasos para un Cliente TCP

Para cualquier cliente TCP que requiera leer/escribir comandos (como este Buscaminas), sigue esta estructura de 4 partes dentro de tu clase principal (ej. `TCPController`):

### 1. Variables de Estado Mínimas
Solo guarda lo estrictamente necesario.
```java
private String host;
private int port;
private Gson gson = new Gson();
// Estado del juego
private Cell[][] board;
private boolean gameOver;
private boolean win;
```

### 2. Un único método de Red (`sendRequest`)
No crees un método para cada acción (`selectCell`, `markCell`, etc.). Crea **uno solo** que reciba la acción y los datos, abra el socket, envíe y reciba.

* **Usa `try-with-resources`**: Así no pierdes tiempo escribiendo `socket.close()`, `writer.close()`, etc. Se cierran solos.
* **Usa JSON o strings simples**: Si te dejan usar Gson, crea una clase `Request` y otra `Response`.

```java
public void sendRequest(String action, Map<String, String> data) {
    // 1. Abrir conexión y flujos (Streams)
    try (Socket s = new Socket(host, port);
         BufferedWriter w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
         BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
        
        // 2. Preparar petición y enviarla
        Request req = new Request();
        req.action = action;
        req.data = data;
        w.write(gson.toJson(req) + "\n");
        w.flush(); // IMPORTANTE: Siempre hacer flush
        
        // 3. Leer respuesta
        Response res = gson.fromJson(r.readLine(), Response.class);
        
        // 4. Actualizar tu estado local
        if (res != null && res.data != null) {
            // Actualizar tablero, banderas de ganar/perder, etc.
        }
    } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
    }
}
```

### 3. Método para dibujar/imprimir (`printBoard`)
Una función sencilla con dos ciclos `for` para mostrar los datos locales. No necesita pedir nada por red, solo imprime lo que tienes guardado.

### 4. Bucle Principal Interactivo (`start`)
Usa un `while(true)` y un `Scanner`. Pide opciones y llama al único método `sendRequest` usando `Map.of()` para crear diccionarios rápidos en una sola línea.

```java
Scanner sc = new Scanner(System.in);
while (true) {
    System.out.println("1: Jugar | 2: Salir");
    int op = sc.nextInt();
    if (op == 2) break;
    
    if (op == 1) {
        System.out.print("Fila y Columna: ");
        // Map.of() es perfecto para enviar los parámetros rápido
        sendRequest("SELECT_CELL", Map.of("i", sc.next(), "j", sc.next()));
    }
}
```

## Resumen de la Simplificación (Buscaminas)

He reducido tu `TCPController.java` de **casi 300 líneas a solo 80 líneas**. 
¿Qué se eliminó/cambió para lograrlo?
1. **Se borraron los getters y setters** (`getBoard()`, `setGameOver()`, etc.) porque nadie los usa fuera de la clase.
2. **Se eliminaron funciones redundantes**: Métodos como `selectCell`, `markCell`, `initGame` y `showAll` hacían lo mismo: crear un HashMap y llamar a `sendRequest`. Ahora todo eso se hace directamente en el menú (opción 1, 2 y 3) usando `Map.of(...)`.
3. **El menú se compactó**: En vez de tantos `System.out.println`, agrupamos las opciones en una sola línea.
4. **La actualización de estado se integró**: `updateInternalState()` se fusionó dentro de `sendRequest()` para no saltar entre métodos leyendo código.

Con esta estructura compacta, puedes escribir y memorizar un cliente TCP funcional en 15-20 minutos durante tu examen.
