package com.example.terminal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TerminalBufferTest {

    @Test
    void shouldInitializeWithCorrectDimensionsAndCursor() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);

        assertEquals(80, buffer.getWidth());
        assertEquals(24, buffer.getHeight());
        assertEquals(1000, buffer.getMaxScrollback());
        assertEquals(0, buffer.getCursorColumn());
        assertEquals(0, buffer.getCursorRow());
    }

    @Test
    void shouldClampCursorInsideBounds() {
        TerminalBuffer buffer = new TerminalBuffer(10, 5, 100);

        buffer.setCursorPosition(100, 100);

        assertEquals(9, buffer.getCursorColumn());
        assertEquals(4, buffer.getCursorRow());
    }

    @Test
    void shouldMoveCursorWithinBounds() {
        TerminalBuffer buffer = new TerminalBuffer(10, 5, 100);

        buffer.moveCursorRight(3);
        buffer.moveCursorDown(2);

        assertEquals(3, buffer.getCursorColumn());
        assertEquals(2, buffer.getCursorRow());
    }

    @Test
    void shouldNotMoveCursorOutsideBounds() {
        TerminalBuffer buffer = new TerminalBuffer(10, 5, 100);

        buffer.moveCursorLeft(10);
        buffer.moveCursorUp(10);

        assertEquals(0, buffer.getCursorColumn());
        assertEquals(0, buffer.getCursorRow());
    }
}