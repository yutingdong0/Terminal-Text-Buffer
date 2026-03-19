## Explanation of the Solution

The core of the implementation is the `TerminalBuffer`, which models the internal state of a terminal emulator. The buffer is split into two logical parts: the **screen** (a fixed-size grid representing the visible area) and the **scrollback** (a bounded history of lines that are scrolled off the screen which is invisible).

Each screen line is represented as a list of `Cell` objects, where each cell stores:

* a character
* foreground and background colors
* style attributes (bold, italic, underline)

The buffer also maintains:

* a **cursor position** (column, row)
* **current attributes**, which are applied to all newly written or inserted characters

### Writing (overwrite mode)

The `writeText` operation writes characters at the current cursor position and overwrites existing content. After each character:

* the cursor advances to the right
* when reaching the end of a line, it wraps to the next line
* when writing to the end of the bottom of the screen, the buffer scrolls:

    * the top line is moved to scrollback
    * a new empty line is added at the bottom

### Insertion

The `insertText` operation inserts characters at the cursor position and shifts existing content to the right. Overflow from a line propagates to the next line, and may continue across multiple lines:

* if propagation reaches the last row, the screen scrolls
* inserted characters use the current attributes
* shifted cells retain their original attributes

Propagation stops when the shifted content encounters an empty cell (a cell containing a space character).

### Scrolling and Scrollback

Scrolling is implemented by removing the top line of the screen and adding it to the scrollback buffer. The scrollback is bounded:

* when it exceeds the configured maximum size, the oldest lines are discarded

### Clearing and Line Operations

The buffer supports:

* clearing the screen (resets content and cursor)
* clearing both screen and scrollback
* filling a line with a given character
* inserting an empty line at the bottom (equivalent to manual scroll)

### Content Access

The implementation provides methods to access:

* characters and attributes from the screen
* characters and attributes from the scrollback
* full lines as strings
* the entire buffer content (screen + scrollback)

---

## Design Decisions and Trade-offs

### 1. Data Structure Choice

The screen is implemented as `List<List<Cell>>` rather than a 2D array.
This makes it easier to shift content during insertion, and remove and append lines during scrolling

The trade-off is slightly higher overhead compared to arrays, but it simplifies implementation and improves readability.

---

### 2. Cursor Semantics

The cursor advances after every written or inserted character.
If a character is written in the bottom-right cell, the cursor immediately wraps and may trigger scrolling.

This behavior is simple and consistent.

---

### 3. Separation of Screen and Scrollback

The screen and scrollback are stored separately:

* the screen is mutable
* the scrollback is treated as immutable history

This matches how terminal emulators conceptually work.

---

### 4. Insert Propagation Strategy

Insertion is implemented using propagation with a **carry**:

* inserting into a full line pushes the last cell into the next line
* propagation continues until an empty cell is found or scrolling occurs

This approach is simple to implement and preserves both characters and attributes correctly.

The trade-off is that insertion can be O(n) across multiple lines.

---

### 5. Definition of Empty Cell

A cell is considered empty if 
* its character is a space `' '`
* and it has default attributes

This decision ensures that styled content is preserved correctly and not unintentionally discarded during insert operations.
The trade-off is that cells appeared empty might still be occupied, which slightly complicates reasoning about insertion behavior.
---

### 6. API Design

Both screen-only and global (screen + scrollback) access methods are provided.
This makes the API flexible while keeping responsibilities clear.

---

## Possible Improvements (Not Implemented)

* **Wide character support** (e.g., CJK, emoji occupying two cells)
* **Resize handling**, including content reflow strategies
* **Performance optimizations** (e.g., using arrays instead of lists)
* More precise emulation of terminal cursor behavior
* Support for additional ANSI terminal features