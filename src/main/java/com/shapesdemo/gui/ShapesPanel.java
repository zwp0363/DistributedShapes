package com.shapesdemo.gui;

import com.shapesdemo.shape.*;
import com.shapesdemo.shape.Shape;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ShapesPanel extends JPanel {
    private final List<Shape> shapes = new CopyOnWriteArrayList<>();
    private Shape selectedShape;
    private ShapeUpdateListener updateListener;
    private Timer updateTimer;
    private Timer animationTimer;  // 新增：动画更新计时器
    private static final int UPDATE_INTERVAL = 50; // 20 FPS
    private static final int ANIMATION_INTERVAL = 16; // ~60 FPS
    private long lastUpdateTime = 0;
    private static final long THROTTLE_INTERVAL = 50; // 50ms throttling interval

    public ShapesPanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);

        // 鼠标事件处理
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectShapeAt(e.getX(), e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (selectedShape != null && updateListener != null) {
                    updateListener.onShapeUpdated(selectedShape);
                }
                lastUpdateTime = 0; // 重置节流计时器
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (selectedShape != null) {
                    selectedShape.move(e.getX(), e.getY());

                    // 添加消息节流逻辑
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastUpdateTime >= THROTTLE_INTERVAL) {
                        if (updateListener != null) {
                            updateListener.onShapeUpdated(selectedShape);
                        }
                        lastUpdateTime = currentTime;
                    }

                    repaint();
                }
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);

        // 创建动画更新计时器
        animationTimer = new Timer(ANIMATION_INTERVAL, e -> {
            boolean needsRepaint = false;
            for (Shape shape : shapes) {
                shape.updatePosition();
                needsRepaint = true;
            }
            if (needsRepaint) {
                repaint();
            }
        });
        animationTimer.start();

        // 创建定时器用于定期更新
        updateTimer = new Timer(UPDATE_INTERVAL, e -> repaint());
        updateTimer.start();
    }

    private void selectShapeAt(int x, int y) {
        Shape selected = null;
        // 从后向前检查，以便选择最上层的图形
        for (int i = shapes.size() - 1; i >= 0; i--) {
            Shape shape = shapes.get(i);
            if (isPointInShape(x, y, shape)) {
                selected = shape;
                break;
            }
        }
        selectedShape = selected;
        repaint();
    }

    private boolean isPointInShape(int x, int y, Shape shape) {
        int dx = x - shape.getX();
        int dy = y - shape.getY();
        return (dx * dx + dy * dy) <= (shape.getSize() * shape.getSize() / 4);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // 启用抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制所有图形的轨迹
        for (Shape shape : shapes) {
            shape.drawTrail(g2d);
        }

        // 绘制所有图形
        for (Shape shape : shapes) {
            shape.draw(g2d);
        }

        // 绘制选中图形的边框
        if (selectedShape != null) {
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(2));
            int size = selectedShape.getSize();
            g2d.drawRect(selectedShape.getX() - size/2,
                    selectedShape.getY() - size/2,
                    size, size);
        }
    }

    public void addShape(Shape shape) {
        shapes.add(shape);
        repaint();
    }

    public void updateShape(Shape updatedShape) {
        for (int i = 0; i < shapes.size(); i++) {
            Shape shape = shapes.get(i);
            if (shape.equals(updatedShape)) {
                // 更新目标位置而不是直接设置位置
                shape.setTargetX(updatedShape.getTargetX());
                shape.setTargetY(updatedShape.getTargetY());
                if (selectedShape != null && selectedShape.equals(shape)) {
                    selectedShape = shape;
                }
                break;
            }
        }
        repaint();
    }

    public void clearAllTrails() {
        for (Shape shape : shapes) {
            shape.clearTrail();
        }
        repaint();
    }

    public void setUpdateListener(ShapeUpdateListener listener) {
        this.updateListener = listener;
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (updateTimer != null) {
            updateTimer.stop();
        }
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }

    public interface ShapeUpdateListener {
        void onShapeUpdated(Shape shape);
    }
} 