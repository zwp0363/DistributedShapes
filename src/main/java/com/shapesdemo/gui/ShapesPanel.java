package com.shapesdemo.gui;

import com.shapesdemo.shape.*;
import com.shapesdemo.shape.Shape;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ShapesPanel extends JPanel {
    private final List<Shape> shapes = new CopyOnWriteArrayList<>();
    private Shape selectedShape;
    private ShapeUpdateListener updateListener;
    private Timer updateTimer;
    private Timer animationTimer;
    private static final int UPDATE_INTERVAL = 16; // 约60FPS
    private static final int ANIMATION_INTERVAL = 16;
    private Point lastMousePoint;

    public ShapesPanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);
        
        // 鼠标事件处理
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectShapeAt(e.getX(), e.getY());
                lastMousePoint = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (selectedShape != null && updateListener != null) {
                    updateListener.onShapeUpdated(selectedShape);
                }
                selectedShape = null;
                lastMousePoint = null;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (selectedShape != null) {
                    // 计算移动增量
                    int dx = e.getX() - lastMousePoint.x;
                    int dy = e.getY() - lastMousePoint.y;
                    
                    // 更新图形位置
                    selectedShape.setX(selectedShape.getX() + dx);
                    selectedShape.setY(selectedShape.getY() + dy);
                    selectedShape.setTargetX(selectedShape.getX());
                    selectedShape.setTargetY(selectedShape.getY());
                    
                    // 更新最后的鼠标位置
                    lastMousePoint = e.getPoint();
                    
                    // 立即发送更新
                    if (updateListener != null) {
                        updateListener.onShapeUpdated(selectedShape);
                    }
                    
                    repaint();
                }
            }
        };
        
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);

        // 创建更新定时器
        updateTimer = new Timer(UPDATE_INTERVAL, e -> repaint());
        updateTimer.start();
    }

    private void selectShapeAt(int x, int y) {
        Shape selected = null;
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
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制所有图形的轨迹
        for (Shape shape : shapes) {
            shape.drawTrail(g2d);
        }

        // 绘制所有图形
        for (Shape shape : shapes) {
            shape.draw(g2d);
            if (shape == selectedShape) {
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(2));
                int size = shape.getSize();
                g2d.drawRect(shape.getX() - size/2, 
                            shape.getY() - size/2, 
                            size, size);
            }
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
                // 直接更新位置和状态
                shape.setX(updatedShape.getX());
                shape.setY(updatedShape.getY());
                shape.setTargetX(updatedShape.getX());
                shape.setTargetY(updatedShape.getY());
                shape.setShowTrail(updatedShape.isShowTrail());
                
                // 更新轨迹
                if (updatedShape.isShowTrail()) {
                    shape.setTrailPoints(updatedShape.getTrailPoints());
                }
                
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
    }

    public interface ShapeUpdateListener {
        void onShapeUpdated(Shape shape);
    }
} 