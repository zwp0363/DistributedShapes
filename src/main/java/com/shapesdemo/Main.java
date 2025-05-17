package com.shapesdemo;

import com.shapesdemo.gui.ShapesFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -jar shapes-demo.jar [server|client] [host] [port]");
            System.exit(1);
        }

        boolean isServer = args[0].equalsIgnoreCase("server");
        String host = isServer ? "localhost" : (args.length > 1 ? args[1] : "localhost");
        int port = args.length > 2 ? Integer.parseInt(args[2]) : 12345;

        SwingUtilities.invokeLater(() -> {
            ShapesFrame frame = new ShapesFrame(isServer, host, port);
            frame.setVisible(true);
        });
    }
} 