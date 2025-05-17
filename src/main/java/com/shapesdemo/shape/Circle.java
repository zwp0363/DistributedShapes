package com.shapesdemo.shape;

import java.awt.*;

public class Circle extends Shape {
    public Circle(int x, int y, Color color, int size) {
        super(x, y, color, size);
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(color);
        g.fillOval(x - size/2, y - size/2, size, size);
    }
} 