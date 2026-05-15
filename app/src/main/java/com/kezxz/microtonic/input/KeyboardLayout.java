package com.kezxz.microtonic.input;

import javafx.scene.input.KeyCode;
import java.util.OptionalInt;

/**
 * Maps computer keyboard keys to logical note indices.
 *
 * The note index is intentionally tuning-neutral.
 *
 * Example:
 * - In 12-TET, note index 1 means one semitone above tonic.
 * - In 24-TET, note index 1 means one quarter-tone above tonic.
 * - In JI, note index 1 means the next chromatic JI ratio.
 */
public final class KeyboardLayout {

    private KeyboardLayout() {
    }

    public static OptionalInt noteIndexFor(KeyCode keyCode) {
        return switch (keyCode) {
            case Q -> OptionalInt.of(0);
            case W -> OptionalInt.of(1);
            case E -> OptionalInt.of(2);
            case R -> OptionalInt.of(3);
            case T -> OptionalInt.of(4);
            case Y -> OptionalInt.of(5);
            case U -> OptionalInt.of(6);
            case I -> OptionalInt.of(7);
            case O -> OptionalInt.of(8);
            case P -> OptionalInt.of(9);

            case A -> OptionalInt.of(10);
            case S -> OptionalInt.of(11);
            case D -> OptionalInt.of(12);
            case F -> OptionalInt.of(13);
            case G -> OptionalInt.of(14);
            case H -> OptionalInt.of(15);
            case J -> OptionalInt.of(16);
            case K -> OptionalInt.of(17);
            case L -> OptionalInt.of(18);

            case Z -> OptionalInt.of(19);
            case X -> OptionalInt.of(20);
            case C -> OptionalInt.of(21);
            case V -> OptionalInt.of(22);
            case B -> OptionalInt.of(23);
            case N -> OptionalInt.of(24);
            case M -> OptionalInt.of(25);

            default -> OptionalInt.empty();
        };
    }
}