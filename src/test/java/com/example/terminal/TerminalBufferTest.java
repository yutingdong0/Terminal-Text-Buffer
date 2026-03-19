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

    @Test
    void shouldClearScreenButKeepScrollback() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 10);
        buffer.writeText("abcdefghijk"); // creates scrollback

        buffer.clearScreen();

        assertEquals(1, buffer.getScrollbackSize());
        assertEquals("     ", buffer.getLineAsString(0));
        assertEquals("     ", buffer.getLineAsString(1));
        assertEquals(0, buffer.getCursorColumn());
        assertEquals(0, buffer.getCursorRow());
    }

    @Test
    void shouldClearScreenAndScrollback() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 10);
        buffer.writeText("abcdefghijk");

        buffer.clearScreenAndScrollback();

        assertEquals(0, buffer.getScrollbackSize());
        assertEquals("     ", buffer.getLineAsString(0));
        assertEquals("     ", buffer.getLineAsString(1));
    }

    @Test
    void shouldFillLineWithCharacterUsingCurrentAttributes() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 10);
        TextAttributes attrs = new TextAttributes(
                TerminalColor.GREEN,
                TerminalColor.BLACK,
                java.util.EnumSet.of(TextStyle.UNDERLINE)
        );

        buffer.setCurrentAttributes(attrs);
        buffer.fillLine(1, 'x');

        assertEquals("xxxxx", buffer.getLineAsString(1));
        assertEquals(attrs, buffer.getAttributesAt(0, 1));
    }

    @Test
    void shouldInsertEmptyLineAtBottomAndPushTopLineToScrollback() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 10);
        buffer.writeText("abcdefghi"); // fills two lines except for last position

        buffer.insertEmptyLineAtBottom();

        assertEquals(1, buffer.getScrollbackSize());
        assertEquals("abcde", buffer.getScrollbackLineAsString(0));
        assertEquals("fghi ", buffer.getLineAsString(0));
        assertEquals("     ", buffer.getLineAsString(1));
    }

    @Test
    void shouldAccessContentAcrossScrollbackAndScreen() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 10);
        buffer.writeText("abcdefghijk");

        assertEquals("abcde", buffer.getGlobalLineAsString(0));
        assertEquals("fghij", buffer.getGlobalLineAsString(1));
        assertEquals("k    ", buffer.getGlobalLineAsString(2));
    }

    @Test
    void shouldInsertTextIntoEmptyLine() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 10);

        buffer.insertText("ab");

        assertEquals("ab   ", buffer.getLineAsString(0));
    }

    @Test
    void shouldShiftExistingCharactersRightWhenInserting() {
        TerminalBuffer buffer = new TerminalBuffer(6, 2, 10);

        buffer.writeText("hello");
        buffer.setCursorPosition(1, 0);
        buffer.insertText("X");

        assertEquals("hXello", buffer.getLineAsString(0));
    }

    @Test
    void shouldWrapOverflowToNextLineWhenInserting() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 10);

        buffer.writeText("abcde");
        buffer.setCursorPosition(2, 0);
        buffer.insertText("XY");

        assertEquals("abXYc", buffer.getLineAsString(0));
        assertEquals("de   ", buffer.getLineAsString(1));
    }

    @Test
    void shouldScrollWhenInsertOverflowsLastLine() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 10);

        buffer.writeText("abcdefghi");
        buffer.setCursorPosition(2, 1);
        buffer.insertText("XYZ");

        assertTrue(buffer.getScrollbackSize() >= 1);
    }

    @Test
    void shouldInsertAndPropagateOverflowAcrossLines() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 10);

        buffer.writeText("abcdefghijklm");
        buffer.setCursorPosition(3, 0);
        buffer.insertText("XY");

        assertEquals("abcXY", buffer.getLineAsString(0));
    }

    @Test
    void shouldPreserveAttributesOfShiftedCellsDuringInsert() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 10);

        TextAttributes red = new TextAttributes(
                TerminalColor.RED,
                TerminalColor.DEFAULT,
                java.util.EnumSet.of(TextStyle.BOLD)
        );
        TextAttributes green = new TextAttributes(
                TerminalColor.GREEN,
                TerminalColor.DEFAULT,
                java.util.EnumSet.noneOf(TextStyle.class)
        );

        buffer.setCurrentAttributes(red);
        buffer.writeText("A");

        buffer.setCurrentAttributes(green);
        buffer.setCursorPosition(0, 0);
        buffer.insertText("B");

        assertEquals('B', buffer.getCharacterAt(0, 0));
        assertEquals(green, buffer.getAttributesAt(0, 0));

        assertEquals('A', buffer.getCharacterAt(1, 0));
        assertEquals(red, buffer.getAttributesAt(1, 0));
    }

    @Test
    void shouldInsertIntoMiddleOfLine() {
        TerminalBuffer buffer = new TerminalBuffer(6, 2, 10);

        buffer.writeText("abcd");
        buffer.setCursorPosition(2, 0);
        buffer.insertText("XY");

        assertEquals("abXYcd", buffer.getLineAsString(0));
    }

    @Test
    void shouldCarryOverflowToNextLine() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 10);

        buffer.writeText("abcde");
        buffer.writeText("fg");
        buffer.setCursorPosition(3, 0);
        buffer.insertText("XY");

        assertEquals("abcXY", buffer.getLineAsString(0));
        assertEquals("defg ", buffer.getLineAsString(1));
    }

    @Test
    void shouldScrollWhenInsertPropagatesPastLastRow() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 10);

        buffer.writeText("abcdefg");
        buffer.setCursorPosition(1, 1);
        buffer.insertText("XY");

        assertEquals(1, buffer.getScrollbackSize());
    }

    @Test
    void shouldPreserveAttributesWhenCellsAreShiftedByInsert() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 10);

        TextAttributes red = new TextAttributes(
                TerminalColor.RED,
                TerminalColor.DEFAULT,
                java.util.EnumSet.of(TextStyle.BOLD)
        );
        TextAttributes green = new TextAttributes(
                TerminalColor.GREEN,
                TerminalColor.DEFAULT,
                java.util.EnumSet.noneOf(TextStyle.class)
        );

        buffer.setCurrentAttributes(red);
        buffer.writeText("A");

        buffer.setCurrentAttributes(green);
        buffer.setCursorPosition(0, 0);
        buffer.insertText("B");

        assertEquals('B', buffer.getCharacterAt(0, 0));
        assertEquals(green, buffer.getAttributesAt(0, 0));

        assertEquals('A', buffer.getCharacterAt(1, 0));
        assertEquals(red, buffer.getAttributesAt(1, 0));
    }
}