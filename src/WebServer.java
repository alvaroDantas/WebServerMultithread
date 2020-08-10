import java.net.* ;

public final class WebServer {
    public static void main(String args[]) throws Exception {
        int port = 15000;

        ServerSocket serverSocket = new ServerSocket(port); 

        System.out.println("Servidor em execução aguardando requisições");
        
        while(true) { 
            Socket connectionSocket = serverSocket.accept(); 
            

            HttpRequest request = new HttpRequest(connectionSocket);

            Thread thread = new Thread(request);

            thread.start();
            
        }
    }
}