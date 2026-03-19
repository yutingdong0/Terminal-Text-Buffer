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

    @Test
    void shouldReturnEmptyLineInitially() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 10);

        assertEquals("     ", buffer.getLineAsString(0));
        assertEquals("     ", buffer.getLineAsString(1));
    }

    @Test
    void shouldWriteTextAtCursorPosition() {
        TerminalBuffer buffer = new TerminalBuffer(10, 3, 10);

        buffer.writeText("abc");

        assertEquals("abc       ", buffer.getLineAsString(0));
        assertEquals(3, buffer.getCursorColumn());
        assertEquals(0, buffer.getCursorRow());
    }

    @Test
    void shouldOverwriteExistingCharacters() {
        TerminalBuffer buffer = new TerminalBuffer(10, 3, 10);

        buffer.writeText("hello");
        buffer.setCursorPosition(1, 0);
        buffer.writeText("XYZ");

        assertEquals("hXYZo     ", buffer.getLineAsString(0));
    }

    @Test
    void shouldStopWritingAtEndOfLineForNow() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 10);

        buffer.setCursorPosition(3, 0);
        buffer.writeText("abcd");

        assertEquals("   ab", buffer.getLineAsString(0));
        assertEquals(2, buffer.getCursorColumn());
        assertEquals(1, buffer.getCursorRow());
    }

    @Test
    void shouldApplyCurrentAttributesToWrittenCells() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 10);
        TextAttributes attrs = new TextAttributes(
                TerminalColor.RED,
                TerminalColor.BLACK,
                java.util.EnumSet.of(TextStyle.BOLD)
        );

        buffer.setCurrentAttributes(attrs);
        buffer.writeText("A");

        assertEquals('A', buffer.getCharacterAt(0, 0));
        assertEquals(attrs, buffer.getAttributesAt(0, 0));
    }

    @Test
    void shouldWrapToNextLineWhenReachingEndOfLine() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 10);

        buffer.writeText("abcdef");

        assertEquals("abcde", buffer.getLineAsString(0));
        assertEquals("f    ", buffer.getLineAsString(1));
    }

    @Test
    void shouldScrollWhenWritingPastBottomOfScreen() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 10);

        buffer.writeText("abcdefghijk");

        assertEquals(1, buffer.getScrollbackSize());
        assertEquals("abcde", buffer.getScrollbackLineAsString(0));
        assertEquals("fghij", buffer.getLineAsString(0));
        assertEquals("k    ", buffer.getLineAsString(1));
    }

    @Test
    void shouldKeepOnlyNewestScrollbackLinesUpToMaxSize() {
        TerminalBuffer buffer = new TerminalBuffer(3, 2, 2);

        buffer.writeText("abcdefghi");   // visible: def / ghi, scrollback: abc
        buffer.writeText("jklmno");      // more scrolling

        assertEquals(2, buffer.getScrollbackSize());
    }

    @Test
    void shouldNotStoreScrollbackWhenMaxScrollbackIsZero() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 0);

        buffer.writeText("abcdefghijk");

        assertEquals(0, buffer.getScrollbackSize());
    }

    @Test
    void shouldReturnScrollbackAndScreenContentTogether() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 10);

        buffer.writeText("abcdefghijk");

        assertEquals(
                "abcde\nfghij\nk    ",
                buffer.getAllContentAsString()
        );
    }
}