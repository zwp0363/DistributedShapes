package com.shapesdemo.gui;

import com.shapesdemo.network.ShapeMessage;
import com.shapesdemo.shape.*;
import com.shapesdemo.shape.Rectangle;
import com.shapesdemo.shape.Shape;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ShapesFrame extends JFrame {
    private ShapesPanel shapesPanel;
    private JPanel controlPanel;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean isServer;
    private ServerSocket serverSocket;
    private Shape selectedShape;
    private List<ClientHandler> clients;

    public ShapesFrame(boolean isServer, String host, int port) {
        this.isServer = isServer;
        this.clients = new CopyOnWriteArrayList<>();
        setTitle(isServer ? "Shapes Demo - Server" : "Shapes Demo - Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initialize components
        shapesPanel = new ShapesPanel();
        controlPanel = createControlPanel();

        // Layout
        setLayout(new BorderLayout());
        add(shapesPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);

        // Setup network
        setupNetwork(host, port);

        // 添加窗口关闭事件处理
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                cleanup();
            }
        });
    }

    private void cleanup() {
        try {
            if (isServer) {
                for (ClientHandler client : clients) {
                    client.close();
                }
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } else {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(200, 600));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Shape selection
        String[] shapeTypes = {"Circle", "Rectangle", "Triangle"};
        JComboBox<String> shapeCombo = new JComboBox<>(shapeTypes);

        // Color selection
        JButton colorBtn = new JButton("选择颜色");
        final Color[] currentColor = {Color.BLUE};

        // Size control
        JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(50, 20, 200, 10));

        // Trail toggle
        JCheckBox trailCheck = new JCheckBox("显示轨迹");
        JButton clearTrailBtn = new JButton("清除轨迹");

        // Add shape button
        JButton addBtn = new JButton("添加形状");

        // Add listeners
        colorBtn.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "选择颜色", currentColor[0]);
            if (newColor != null) {
                currentColor[0] = newColor;
            }
        });

        trailCheck.addActionListener(e -> {
            if (selectedShape != null) {
                selectedShape.setShowTrail(trailCheck.isSelected());
                sendShape(selectedShape, "UPDATE");
            }
        });

        clearTrailBtn.addActionListener(e -> {
            shapesPanel.clearAllTrails();
            // 通知其他客户端清除轨迹
            sendShape(null, "CLEAR_TRAILS");
        });

        addBtn.addActionListener(e -> {
            Shape shape = null;
            int size = (Integer) sizeSpinner.getValue();
            switch (shapeCombo.getSelectedIndex()) {
                case 0:
                    shape = new Circle(400, 300, currentColor[0], size);
                    break;
                case 1:
                    shape = new Rectangle(400, 300, currentColor[0], size);
                    break;
                case 2:
                    shape = new Triangle(400, 300, currentColor[0], size);
                    break;
            }
            if (shape != null) {
                shape.setShowTrail(trailCheck.isSelected());
                shapesPanel.addShape(shape);
                sendShape(shape, "ADD");
            }
        });

        // Layout components
        panel.add(new JLabel("形状类型:"));
        panel.add(shapeCombo);
        panel.add(Box.createVerticalStrut(10));
        panel.add(colorBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(new JLabel("大小:"));
        panel.add(sizeSpinner);
        panel.add(Box.createVerticalStrut(10));
        panel.add(trailCheck);
        panel.add(clearTrailBtn);
        panel.add(Box.createVerticalStrut(20));
        panel.add(addBtn);

        return panel;
    }

    private void setupNetwork(String host, int port) {
        try {
            if (isServer) {
                serverSocket = new ServerSocket(port);
                // 启动服务器监听线程
                new Thread(() -> {
                    while (!serverSocket.isClosed()) {
                        try {
                            Socket clientSocket = serverSocket.accept();
                            ClientHandler clientHandler = new ClientHandler(clientSocket);
                            clients.add(clientHandler);
                            new Thread(clientHandler).start();
                        } catch (IOException e) {
                            if (!serverSocket.isClosed()) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            } else {
                socket = new Socket(host, port);
                setupStreams();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupStreams() throws IOException {
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        // Setup shape update listener
        shapesPanel.setUpdateListener(shape -> sendShape(shape, "UPDATE"));

        // Start receiving messages
        new Thread(this::receiveMessages).start();
    }

    private void sendShape(Shape shape, String action) {
        try {
            if (isServer) {
                // 服务器向所有客户端广播
                ShapeMessage message = new ShapeMessage(shape, action);
                for (ClientHandler client : clients) {
                    client.sendMessage(message);
                }
            } else if (out != null) {
                // 客户端发送到服务器
                out.writeObject(new ShapeMessage(shape, action));
                out.reset();
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveMessages() {
        while (socket != null && !socket.isClosed()) {
            try {
                ShapeMessage message = (ShapeMessage) in.readObject();
                handleMessage(message);
            } catch (IOException | ClassNotFoundException e) {
                if (!socket.isClosed()) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private void handleMessage(ShapeMessage message) {
        SwingUtilities.invokeLater(() -> {
            try {
                Shape receivedShape = message.getShape();
                switch (message.getAction()) {
                    case "ADD":
                        if (receivedShape != null) {
                            shapesPanel.addShape(receivedShape);
                        }
                        break;
                    case "UPDATE":
                        if (receivedShape != null) {
                            shapesPanel.updateShape(receivedShape);
                        }
                        break;
                    case "CLEAR_TRAILS":
                        shapesPanel.clearAllTrails();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // 内部类：处理客户端连接
    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private ObjectOutputStream clientOut;
        private ObjectInputStream clientIn;

        public ClientHandler(Socket socket) throws IOException {
            this.clientSocket = socket;
            this.clientOut = new ObjectOutputStream(socket.getOutputStream());
            this.clientIn = new ObjectInputStream(socket.getInputStream());
        }

        @Override
        public void run() {
            while (!clientSocket.isClosed()) {
                try {
                    ShapeMessage message = (ShapeMessage) clientIn.readObject();
                    // 处理从客户端收到的消息
                    handleMessage(message);
                    // 转发给其他客户端
                    for (ClientHandler client : clients) {
                        if (client != this) {
                            client.sendMessage(message);
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    if (!clientSocket.isClosed()) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            clients.remove(this);
        }

        public void sendMessage(ShapeMessage message) throws IOException {
            if (!clientSocket.isClosed()) {
                clientOut.writeObject(message);
                clientOut.reset();
                clientOut.flush();
            }
        }

        public void close() {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
} 