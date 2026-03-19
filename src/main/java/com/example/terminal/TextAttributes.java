package com.example.terminal;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public final class TextAttributes {
    private final TerminalColor foreground;
    private final TerminalColor background;
    private final EnumSet<TextStyle> styles;

    public TextAttributes(TerminalColor foreground, TerminalColor background, Set<TextStyle> styles) {
        this.foreground = Objects.requireNonNull(foreground);
        this.background = Objects.requireNonNull(background);
        this.styles = styles.isEmpty() ? EnumSet.noneOf(TextStyle.class) : EnumSet.copyOf(styles);
    }

    public static TextAttributes defaults() {
        return new TextAttributes(TerminalColor.DEFAULT, TerminalColor.DEFAULT, EnumSet.noneOf(TextStyle.class));
    }

    public TerminalColor getForeground() {
        return foreground;
    }

    public TerminalColor getBackground() {
        return background;
    }

    public Set<TextStyle> getStyles() {
        return EnumSet.copyOf(styles);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TextAttributes that)) return false;
        return foreground == that.foreground
                && background == that.background
                && styles.equals(that.styles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(foreground, background, styles);
    }
}