package com.example.terminal;

import java.util.Objects;

public final class Cell {
    private final char character;
    private final TextAttributes attributes;

    public Cell(char character, TextAttributes attributes) {
        this.character = character;
        this.attributes = Objects.requireNonNull(attributes);
    }

    public static Cell empty() {
        return new Cell(' ', TextAttributes.defaults());
    }

    public char getCharacter() {
        return character;
    }

    public TextAttributes getAttributes() {
        return attributes;
    }
}