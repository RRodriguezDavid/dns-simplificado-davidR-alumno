import java.io.*;
import java.net.*;
import java.util.*;
public class DNSServer {
    private static Map<String, String> registros = new HashMap<>();

    public static void main(String[] args) {
        // Cargar registros desde archivo
        try (BufferedReader br = new BufferedReader(new FileReader("records.txt"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split("\\s+");
                if (partes.length == 3 && partes[1].equals("A")) {
                    registros.put(partes[0], partes[2]); // dominio -> IP
                }
            }
        } catch (IOException e) {
            System.err.println("Error leyendo archivo: " + e.getMessage());
            return;
        }

        // Servidor en puerto fijo
        try (ServerSocket server = new ServerSocket(5353)) {
            System.out.println("Servidor DNS escuchando en puerto 5353...");
            Socket cliente = server.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
            PrintWriter out = new PrintWriter(cliente.getOutputStream(), true);

            String linea;
            while ((linea = in.readLine()) != null) {
                try {
                    if (linea.equals("EXIT")) {
                        break;
                    } else if (linea.startsWith("LOOKUP")) {
                        String[] partes = linea.split("\\s+");
                        if (partes.length == 3 && partes[1].equals("A")) {
                            String ip = registros.get(partes[2]);
                            if (ip != null) {
                                out.println("200 " + ip);
                            } else {
                                out.println("404 Not Found");
                            }
                        } else {
                            out.println("400 Bad request");
                        }
                    } else {
                        out.println("400 Bad request");
                    }
                } catch (Exception e) {
                    out.println("500 Server error");
                }
            }
            cliente.close();
        } catch (IOException e) {
            System.err.println("Error en servidor: " + e.getMessage());
        }
    }
}
