package alday.paa.qrscanserver;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Robot;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.Popup;

public class Server {
    private boolean run = true;
    private TrayIcon trayIcon = null;

    public static void main(String[] args) {
        new Server().startServer();
    }

    private void startServer() {
        final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);

        if(SystemTray.isSupported()){
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage("image.png");
            PopupMenu popup = new PopupMenu();
            MenuItem defaultItem = new MenuItem("Exit");
            defaultItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    run =false;
                }
            });
            popup.add(defaultItem);
            trayIcon = new TrayIcon(image, "QRScanner", popup);
            //trayIcon.addActionListener(listener);
            try{
                tray.add(trayIcon);
            }catch (AWTException e) {
                e.printStackTrace();
            }
        }
        Runnable serverTask = new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(59900);
                    System.out.println("Waiting for scanned text...");
                    while (run) {
                        Socket clientSocket = serverSocket.accept();
                        clientProcessingPool.submit(new ClientTask(clientSocket));
                    }
                    System.out.println("Closed");
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
                System.out.println("Scanned: " + msg);
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
