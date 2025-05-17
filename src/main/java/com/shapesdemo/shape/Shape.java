package com.shapesdemo.shape;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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

    public Shape(int x, int y, Color color, int size) {
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.color = color;
        this.size = size;
        this.showTrail = false;
        this.trailPoints = new ArrayList<>();
        this.id = UUID.randomUUID().toString();  // 生成唯一ID
        addTrailPoint(x, y);
    }

    public abstract void draw(Graphics g);

    public void drawTrail(Graphics g) {
        if (showTrail && trailPoints.size() > 1) {
            g.setColor(color);
            for (int i = 1; i < trailPoints.size(); i++) {
                Point p1 = trailPoints.get(i-1);
                Point p2 = trailPoints.get(i);
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
    }

    public void move(int newX, int newY) {
        this.targetX = newX;
        this.targetY = newY;
        updatePosition();
    }

    public void updatePosition() {
        // 使用插值计算新位置
        if (x != targetX || y != targetY) {
            x += (targetX - x) * INTERPOLATION_FACTOR;
            y += (targetY - y) * INTERPOLATION_FACTOR;

            // 如果非常接近目标，直接设置为目标位置
            if (Math.abs(targetX - x) < 1) x = targetX;
            if (Math.abs(targetY - y) < 1) y = targetY;

            addTrailPoint(x, y);
        }
    }

    protected void addTrailPoint(int x, int y) {
        if (showTrail) {
            trailPoints.add(new Point(x, y));
            // 限制轨迹点数量，防止内存占用过大
            if (trailPoints.size() > 100) {
                trailPoints.remove(0);
            }
        }
    }

    public void clearTrail() {
        trailPoints.clear();
        addTrailPoint(x, y);
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
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
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
    public List<Point> getTrailPoints() { return trailPoints; }
    public void setTrailPoints(List<Point> trailPoints) { this.trailPoints = trailPoints; }
} 