package alday.paa.qrscanserver;

import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static void main(String[] args) {
        new Server().startServer();
    }

    public void startServer() {
        final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);

        Runnable serverTask = new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(59900);
                    System.out.println("Waiting for clients to connect...");
                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        clientProcessingPool.submit(new ClientTask(clientSocket));
                    }
                } catch (IOException e) {
                    System.err.println("Unable to process client request");
                    e.printStackTrace();
                }
            }
        };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();

    }

    private class ClientTask implements Runnable {
        private final Socket clientSocket;

        private ClientTask(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                InputStreamReader reader = new InputStreamReader(clientSocket.getInputStream());
                BufferedReader br = new BufferedReader(reader);
                String msg = br.readLine();
                br.close();

                StringSelection selection = new StringSelection(msg);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                Robot r = new Robot();
                r.keyPress(KeyEvent.VK_CONTROL);
                r.keyPress(KeyEvent.VK_V);
                r.keyRelease(KeyEvent.VK_CONTROL);
                r.keyRelease(KeyEvent.VK_V);

                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception r){
                //Do nothing
            }
        }
    }

}
