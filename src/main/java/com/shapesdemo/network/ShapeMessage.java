package com.shapesdemo.network;

import com.shapesdemo.shape.Shape;
import java.io.Serializable;

public class ShapeMessage implements Serializable {
    private Shape shape;
    private String action; // "UPDATE", "ADD", "REMOVE", "CLEAR_TRAILS"

    public ShapeMessage(Shape shape, String action) {
        this.shape = shape;
        this.action = action;
    }

    public Shape getShape() {
        return shape;
    }

    public String getAction() {
        return action;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public void setAction(String action) {
        this.action = action;
    }
} 