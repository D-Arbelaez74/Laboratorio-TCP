error id: file:///C:/Users/dany2/Documents/Ingeniería%20de%20Sistemas/Semestre%205/Computación%20en%20Internet%201/compu-internet-1/client/src/main/java/co/icesi/buscaminas/controllers/TCPController.java:_empty_/Int#
file:///C:/Users/dany2/Documents/Ingeniería%20de%20Sistemas/Semestre%205/Computación%20en%20Internet%201/compu-internet-1/client/src/main/java/co/icesi/buscaminas/controllers/TCPController.java
empty definition using pc, found symbol in pc: _empty_/Int#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 800
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import co.icesi.buscaminas.controllers.dtos.Request;
import co.icesi.buscaminas.controllers.dtos.Response;

public class TCPController {

    private String host;
    private int port;
    private Gson gson;

    public TCPController() {
        this("localhost", 12345);
    }

    public TCPController(String host, int port) {
        this.host = host;
        this.port = port;
        this.gson = new GsonBuilder().create();
    }

    public Response sendRequest(String action, Map<n@@t, Int> data) {
        try (Socket socket = new Socket(host, port);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            Request request = new Request();
            request.action = action;
            request.data = data;

            String jsonRequest = gson.toJson(request);
            writer.write(jsonRequest);
            writer.newLine();
            writer.flush();

            String jsonResponse = reader.readLine();
            return gson.fromJson(jsonResponse, Response.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/Int#