package com.shapesdemo.shape;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Shape implements Serializable {
    protected int x;
    protected int y;
    protected int targetX;  // 目标X坐标
    protected int targetY;  // 目标Y坐标
    protected Color color;
    protected int size;
    protected boolean showTrail;
    protected List<Point> trailPoints;
    protected static final float INTERPOLATION_FACTOR = 0.3f;  // 插值因子
    protected final String id;  // 添加唯一标识符
    protected static final int MAX_TRAIL_POINTS = 50; // 减少轨迹点数量以降低延迟
    protected static final int MIN_TRAIL_DISTANCE = 5; // 增加最小距离以减少点数

    public Shape(int x, int y, Color color, int size) {
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.color = color;
        this.size = size;
        this.showTrail = false;
        this.trailPoints = new CopyOnWriteArrayList<>();
        this.id = UUID.randomUUID().toString();  // 生成唯一ID
    }

    public abstract void draw(Graphics g);

    public void drawTrail(Graphics g) {
        if (showTrail && trailPoints.size() > 1) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(2));
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                               RenderingHints.VALUE_ANTIALIAS_ON);

            for (int i = 1; i < trailPoints.size(); i++) {
                Point p1 = trailPoints.get(i-1);
                Point p2 = trailPoints.get(i);
                if (p1 != null && p2 != null) {
                    g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
        }
    }

    public void move(int newX, int newY) {
        if (this.x != newX || this.y != newY) {
            this.targetX = newX;
            this.targetY = newY;
            addTrailPoint(this.x, this.y); // 添加起始点
            updatePosition();
        }
    }

    public void updatePosition() {
        if (x != targetX || y != targetY) {
            int oldX = x;
            int oldY = y;
            
            x += (targetX - x) * INTERPOLATION_FACTOR;
            y += (targetY - y) * INTERPOLATION_FACTOR;

            if (Math.abs(targetX - x) < 1) x = targetX;
            if (Math.abs(targetY - y) < 1) y = targetY;

            // 只在位置显著变化时添加轨迹点
            int dx = x - oldX;
            int dy = y - oldY;
            if (Math.sqrt(dx * dx + dy * dy) >= MIN_TRAIL_DISTANCE) {
                addTrailPoint(x, y);
            }
            
            // 如果到达目标位置，确保添加最后一个点
            if (x == targetX && y == targetY) {
                addTrailPoint(x, y);
            }
        }
    }

    protected synchronized void addTrailPoint(int x, int y) {
        if (showTrail) {
            Point newPoint = new Point(x, y);
            
            // 检查是否与最后一个点距离太近
            if (!trailPoints.isEmpty()) {
                Point lastPoint = trailPoints.get(trailPoints.size() - 1);
                if (lastPoint != null) {
                    int dx = newPoint.x - lastPoint.x;
                    int dy = newPoint.y - lastPoint.y;
                    double distance = Math.sqrt(dx * dx + dy * dy);
                    if (distance < MIN_TRAIL_DISTANCE) {
                        return;
                    }
                }
            }
            
            trailPoints.add(newPoint);
            
            // 限制轨迹点数量
            while (trailPoints.size() > MAX_TRAIL_POINTS) {
                trailPoints.remove(0);
            }
        }
    }

    public void clearTrail() {
        trailPoints.clear();
        if (showTrail) {
            addTrailPoint(x, y);
        }
    }

    // 重写equals方法，基于id进行比较
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shape)) return false;
        Shape shape = (Shape) o;
        return Objects.equals(id, shape.id);
    }

    // 重写hashCode方法
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Getters and setters
    public String getId() { return id; }
    public int getX() { return x; }
    public void setX(int x) { 
        this.x = x;
        if (showTrail) {
            addTrailPoint(x, y);
        }
    }
    public int getY() { return y; }
    public void setY(int y) { 
        this.y = y;
        if (showTrail) {
            addTrailPoint(x, y);
        }
    }
    public int getTargetX() { return targetX; }
    public void setTargetX(int targetX) { this.targetX = targetX; }
    public int getTargetY() { return targetY; }
    public void setTargetY(int targetY) { this.targetY = targetY; }
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public boolean isShowTrail() { return showTrail; }
    public void setShowTrail(boolean showTrail) { 
        this.showTrail = showTrail;
        if (showTrail && trailPoints.isEmpty()) {
            addTrailPoint(x, y);
        }
    }
    public List<Point> getTrailPoints() { 
        return new ArrayList<>(trailPoints); 
    }
    public void setTrailPoints(List<Point> points) {
        if (points != null) {
            this.trailPoints = new CopyOnWriteArrayList<>(points);
        }
    }
} 