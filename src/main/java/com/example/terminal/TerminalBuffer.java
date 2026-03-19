package com.example.terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TerminalBuffer {
    private final int width;
    private final int height;
    private final int maxScrollback;

    private final List<List<Cell>> screen;
    private final List<List<Cell>> scrollback;

    private final Cursor cursor;
    private TextAttributes currentAttributes;

    public TerminalBuffer(int width, int height, int maxScrollback) {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be > 0");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be > 0");
        }
        if (maxScrollback < 0) {
            throw new IllegalArgumentException("maxScrollback must be >= 0");
        }

        this.width = width;
        this.height = height;
        this.maxScrollback = maxScrollback;
        this.cursor = new Cursor(0, 0);
        this.currentAttributes = TextAttributes.defaults();
        this.screen = new ArrayList<>();
        this.scrollback = new ArrayList<>();

        for (int row = 0; row < height; row++) {
            screen.add(createEmptyLine());
        }
    }

    private List<Cell> createEmptyLine() {
        List<Cell> line = new ArrayList<>(width);
        for (int i = 0; i < width; i++) {
            line.add(Cell.empty());
        }
        return line;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getMaxScrollback() {
        return maxScrollback;
    }

    public int getCursorColumn() {
        return cursor.getColumn();
    }

    public int getCursorRow() {
        return cursor.getRow();
    }

    public void setCursorPosition(int column, int row) {
        cursor.setPosition(clamp(column, 0, width - 1), clamp(row, 0, height - 1));
    }

    public void moveCursorRight(int n) {
        setCursorPosition(cursor.getColumn() + n, cursor.getRow());
    }

    public void moveCursorLeft(int n) {
        setCursorPosition(cursor.getColumn() - n, cursor.getRow());
    }

    public void moveCursorDown(int n) {
        setCursorPosition(cursor.getColumn(), cursor.getRow() + n);
    }

    public void moveCursorUp(int n) {
        setCursorPosition(cursor.getColumn(), cursor.getRow() - n);
    }

    public void setCurrentAttributes(TextAttributes attributes) {
        this.currentAttributes = Objects.requireNonNull(attributes);
    }

    public TextAttributes getCurrentAttributes() {
        return currentAttributes;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public char getCharacterAt(int column, int row) {
        validateScreenPosition(column, row);
        return screen.get(row).get(column).getCharacter();
    }

    public TextAttributes getAttributesAt(int column, int row) {
        validateScreenPosition(column, row);
        return screen.get(row).get(column).getAttributes();
    }

    public String getLineAsString(int row) {
        validateRow(row);
        StringBuilder sb = new StringBuilder(width);
        for (Cell cell : screen.get(row)) {
            sb.append(cell.getCharacter());
        }
        return sb.toString();
    }

    public String getScreenContentAsString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < height; row++) {
            sb.append(getLineAsString(row));
            if (row < height - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private void validateScreenPosition(int column, int row) {
        validateColumn(column);
        validateRow(row);
    }

    private void validateRow(int row) {
        if (row < 0 || row >= height) {
            throw new IndexOutOfBoundsException("row must be between 0 and " + (height - 1));
        }
    }

    private void validateColumn(int column) {
        if (column < 0 || column >= width) {
            throw new IndexOutOfBoundsException("column must be between 0 and " + (width - 1));
        }
    }

    public void writeText(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }

        for (int i = 0; i < text.length(); i++) {
            int row = cursor.getRow();
            int column = cursor.getColumn();

            char ch = text.charAt(i);
            screen.get(row).set(column, new Cell(ch, currentAttributes));

            advanceCursorForWrite();
        }
    }

    private void advanceCursorForWrite() {
        if (cursor.getColumn() < width - 1) {
            cursor.setPosition(cursor.getColumn() + 1, cursor.getRow());
        } else {
            wrapToNextLine();
        }
    }

    private void wrapToNextLine() {
        if (cursor.getRow() < height - 1) {
            cursor.setPosition(0, cursor.getRow() + 1);
        } else {
            scrollUp();
            cursor.setPosition(0, height - 1);
        }
    }

    private void scrollUp() {
        List<Cell> topLine = screen.remove(0);
        addToScrollback(topLine);
        screen.add(createEmptyLine());
    }

    private void addToScrollback(List<Cell> line) {
        if (maxScrollback == 0) {
            return;
        }

        scrollback.add(copyLine(line));

        if (scrollback.size() > maxScrollback) {
            scrollback.remove(0);
        }
    }

    private List<Cell> copyLine(List<Cell> source) {
        return new ArrayList<>(source);
    }

    public int getScrollbackSize() {
        return scrollback.size();
    }

    public String getScrollbackLineAsString(int row) {
        if (row < 0 || row >= scrollback.size()) {
            throw new IndexOutOfBoundsException("scrollback row out of bounds: " + row);
        }

        StringBuilder sb = new StringBuilder(width);
        for (Cell cell : scrollback.get(row)) {
            sb.append(cell.getCharacter());
        }
        return sb.toString();
    }

    public String getAllContentAsString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < scrollback.size(); i++) {
            sb.append(getScrollbackLineAsString(i)).append("\n");
        }

        for (int i = 0; i < height; i++) {
            sb.append(getLineAsString(i));
            if (i < height - 1) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    public void clearScreen() {
        screen.clear();
        for (int row = 0; row < height; row++) {
            screen.add(createEmptyLine());
        }
        cursor.setPosition(0, 0);
    }

    public void clearScreenAndScrollback() {
        clearScreen();
        scrollback.clear();
    }

    public void fillLine(int row, char ch) {
        validateRow(row);

        List<Cell> line = new ArrayList<>(width);
        for (int i = 0; i < width; i++) {
            line.add(new Cell(ch, currentAttributes));
        }
        screen.set(row, line);
    }

    public void clearLine(int row) {
        validateRow(row);
        screen.set(row, createEmptyLine());
    }

    public void insertEmptyLineAtBottom() {
        List<Cell> topLine = screen.remove(0);
        addToScrollback(topLine);
        screen.add(createEmptyLine());
    }

    public char getCharacterAtGlobal(int column, int row) {
        List<Cell> line = getGlobalLine(row);
        validateColumn(column);
        return line.get(column).getCharacter();
    }

    public TextAttributes getAttributesAtGlobal(int column, int row) {
        List<Cell> line = getGlobalLine(row);
        validateColumn(column);
        return line.get(column).getAttributes();
    }

    public String getGlobalLineAsString(int row) {
        List<Cell> line = getGlobalLine(row);
        StringBuilder sb = new StringBuilder(width);
        for (Cell cell : line) {
            sb.append(cell.getCharacter());
        }
        return sb.toString();
    }

    private List<Cell> getGlobalLine(int row) {
        if (row < 0 || row >= scrollback.size() + screen.size()) {
            throw new IndexOutOfBoundsException("global row out of bounds: " + row);
        }

        if (row < scrollback.size()) {
            return scrollback.get(row);
        }

        return screen.get(row - scrollback.size());
    }

    public void insertText(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }

        for (int i = 0; i < text.length(); i++) {
            insertChar(cursor.getColumn(), cursor.getRow(), text.charAt(i), currentAttributes);
            advanceCursorForWrite();
        }
    }

    private void insertChar(int column, int row, char ch, TextAttributes attributes) {
        Cell carry = new Cell(ch, attributes);
        int currentRow = row;
        int currentColumn = column;

        while (true) {
            List<Cell> line = screen.get(currentRow);

            Cell overflow = line.get(width - 1);

            for (int i = width - 1; i > currentColumn; i--) {
                line.set(i, line.get(i - 1));
            }

            line.set(currentColumn, carry);

            if (isEmptyCell(overflow)) {
                return;
            }

            carry = overflow;
            currentColumn = 0;

            if (currentRow < height - 1) {
                currentRow++;
            } else {
                scrollUp();
                currentRow = height - 1;
            }
        }
    }

    private boolean isEmptyCell(Cell cell) {
        return cell.getCharacter() == ' '
                && TextAttributes.defaults().equals(cell.getAttributes());
    }
}