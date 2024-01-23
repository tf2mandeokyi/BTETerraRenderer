package com.mndk.bteterrarenderer.mcconnector.gui.text;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TextFormatCopy {
    BLACK('0', 0x0),
    DARK_BLUE('1', 0x1),
    DARK_GREEN('2', 0x2),
    DARK_AQUA('3', 0x3),
    DARK_RED('4', 0x4),
    DARK_PURPLE('5', 0x5),
    GOLD('6', 0x6),
    GRAY('7', 0x7),
    DARK_GRAY('8', 0x8),
    BLUE('9', 0x9),
    GREEN('a', 0xa),
    AQUA('b', 0xb),
    RED('c', 0xc),
    LIGHT_PURPLE('d', 0xd),
    YELLOW('e', 0xe),
    WHITE('f', 0xf),
    OBFUSCATED('k', -1),
    BOLD('l', -1),
    STRIKETHROUGH('m', -1),
    UNDERLINE('n', -1),
    ITALIC('o', -1),
    RESET('r', -1);

    private final char code;
    private final int colorIndex;
}
