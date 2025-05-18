package com.shapesdemo.network;

import com.shapesdemo.shape.Shape;
import java.io.Serializable;

public class ShapeMessage implements Serializable {
    private Shape shape;
    private String action; // "UPDATE", "ADD", "REMOVE", "CLEAR_TRAILS"
    private long timestamp;  // 发送时的时间戳
    private String senderId; // 发送者ID

    public ShapeMessage(Shape shape, String action, String senderId) {
        this.shape = shape;
        this.action = action;
        this.timestamp = System.currentTimeMillis();
        this.senderId = senderId;
    }

    public Shape getShape() {
        return shape;
    }

    public String getAction() {
        return action;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
} 