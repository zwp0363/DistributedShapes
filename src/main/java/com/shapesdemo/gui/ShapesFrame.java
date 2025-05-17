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

public class ShapesFrame extends JFrame {
    private ShapesPanel shapesPanel;
    private JPanel controlPanel;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean isServer;
    private ServerSocket serverSocket;
    private Shape selectedShape;

    public ShapesFrame(boolean isServer, String host, int port) {
        this.isServer = isServer;
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
                new Thread(() -> {
                    try {
                        socket = serverSocket.accept();
                        setupStreams();
                    } catch (IOException e) {
                        e.printStackTrace();
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

    private void setupStreams() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Setup shape update listener
            shapesPanel.setUpdateListener(shape -> sendShape(shape, "UPDATE"));

            // Start receiving messages
            new Thread(this::receiveMessages).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendShape(Shape shape, String action) {
        try {
            if (out != null) {
                // 确保发送完整的图形状态
                if (shape != null) {
                    shape.setX(shape.getX());
                    shape.setY(shape.getY());
                    shape.setTargetX(shape.getTargetX());
                    shape.setTargetY(shape.getTargetY());
                    shape.setColor(shape.getColor());
                    shape.setSize(shape.getSize());
                    shape.setShowTrail(shape.isShowTrail());
                    shape.setTrailPoints(new ArrayList<>(shape.getTrailPoints()));
                }
                out.writeObject(new ShapeMessage(shape, action));
                out.reset(); // 重置对象流状态，确保发送完整对象
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveMessages() {
        while (true) {
            try {
                ShapeMessage message = (ShapeMessage) in.readObject();
                SwingUtilities.invokeLater(() -> {
                    switch (message.getAction()) {
                        case "ADD":
                            shapesPanel.addShape(message.getShape());
                            break;
                        case "UPDATE":
                            shapesPanel.updateShape(message.getShape());
                            break;
                        case "CLEAR_TRAILS":
                            shapesPanel.clearAllTrails();
                            break;
                    }
                });
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                break;
            }
        }
    }
} 