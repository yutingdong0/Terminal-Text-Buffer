package com.example.terminal;

public final class Cursor {
    private int column;
    private int row;

    public Cursor(int column, int row) {
        this.column = column;
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public void setPosition(int column, int row) {
        this.column = column;
        this.row = row;
    }
}