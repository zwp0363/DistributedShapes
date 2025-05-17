package com.shapesdemo.shape;

import java.awt.*;

public class Triangle extends Shape {
    public Triangle(int x, int y, Color color, int size) {
        super(x, y, color, size);
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(color);
        int[] xPoints = {x, x - size/2, x + size/2};
        int[] yPoints = {y - size/2, y + size/2, y + size/2};
        g.fillPolygon(xPoints, yPoints, 3);
    }
} 