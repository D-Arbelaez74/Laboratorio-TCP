Preguntas

1. Delimitacion de Mensajes en TCP: ´ TCP es un protocolo de transporte orientado a flujo de bytes continuo y no a bloques. ¿Por
que se produciría un fallo de lectura o bloqueo indefinido si el cliente enviara el JSON sin el salto de línea (\n) o sin invocar
writer.flush()? Explique el concepto de TCP Framing.

Es necesario hacer el writer.flush() porque de esa forma se obliga a que toda la información del buffer se envíe y no se estanque información si es que hubo algún error. Esto garantiza que se limpie el buffer y se envíe toda la info. Mientras que el + "\n" se usa porque al usar el método de reader.readLine() necesita saber donde hay un salto de linea porque esto lo que le dice es que ahí se termina el mensaje. Es para no ir a combinar mensajes e ir a cometer errores.

2. Ventajas y Sobrecargas de ThreadPools: El servidor utiliza Executors.newFixedThreadPool(5). ¿Que ventajas ofrece esta ´
estrategia frente al esquema ingenuo de instanciar un new Thread() por cada cliente? ¿Que sucede cuando se conectan 6 o ´
mas clientes simultáneos?

La idea es que puedan tener una lógica compartida ejecutando hilos que corren otros procesos pero que no sean infinitos, lo que consumiría muchos recursos y es lo que pasaría con solo definir el new Thread(). Mientras que al hacerlo fijo, se hace que máximo 5 clientes se puedan crear en simultaneo, y si llega un sexto se queda en fila hasta que se desocupe un hilo.

3. Condiciones de Carrera y Exclusion Mutua: ´ Si el servidor maneja una unica instancia compartida de ´ BoardGame, ¿que´
problema espec´ıfico de concurrencia ocurrir´ıa si dos clientes ejecutan selectCell en celdas contiguas exactamente al mismo
milisegundo sin metodos ´ synchronized?

Se genera la condición de carrera y es que ambos clientes quieren acceder lo más rápido posible al mismo método, por lo cual generara errores donde los resultados son impredecibles y se puede marcar una celda primero y luego la otra, o no marcar una porque gano o perdió el juego haciendo que el otro cliente nunca llegue a marcar su posición.

4. Conexiones Cortas vs. Conexiones Persistentes: En el diseno actual, el servidor cierra el socket tras responder cada co- ˜
mando (Short-lived connection). Compare este enfoque frente a mantener el socket abierto durante toda la partida (Persistent
connection) en terminos de consumo de descriptores de archivo, latencia y escalabilidad.

Como podemos ver, se usa el formato TCP el cual es muy preciso pero consume muchos recursos. Por lo cual puede ser escalable pero para ciertas cosas que requieran precisión, como pagos bancarios o bases de datos importantes. Pero no podría llegar a servir tener el socket abierto todo el tiempo para un programa muy grande porque el sistema se colapsaría. Por eso en este ejercicio para evitar este tipo de colapsos se dejan máximo 5 hilos. Al mantener abierto todo el tiempo el socket, este queda ocupado durante toda la partida y por ende no se podrá usar para nada más ya que está ocupado. A gran escala bloquearía muchos programas o funciones de un solo programa grande porque los sockets se ocupan todos.

5. Declaracion y Reflexión sobre IAG: ´ Declare expl´ıcitamente que herramientas de Inteligencia Artificial Generativa utiliz ´ o du- ´
rante el desarrollo del laboratorio, que prompts empleó para formular la solución del cliente o depurar el servidor, y cómo validó´
que el codigo propuesto por la IA cumpliera las reglas del protocolo sin introducir vulnerabilidades.

Se uso Gemini 3.8 Flash.

Se usaron algunos prompts como;
"Ayúdame a entender cómo puedo hacer una conexión con el socket sin que se cierre para ejecutar el tablero en vivo"
"Cómo funcionan algunos comandos necesarios para poderme comunicar con el socket"
"Qué hace y cómo funciona la librería Gson"

Le pedí que fuese explicito, claro y lo más corto posible, para poder así analizar de manera manual todo lo que escribió la IA y evitar cualquier tipo de problema. Además le pedí que me explicase en qué partes se señala donde se hace la conexión al socket, cómo funciona cada linea, y dicha linea se comento y analizo una por una buscando evitar que hubiese cualquier tipo de error. Además de probar el correcto funcionamiento del codigo.
