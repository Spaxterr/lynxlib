package dev.spaxter.lynxlib.common;

import dev.spaxter.lynxlib.chat.Messenger;
import net.minecraft.util.text.StringTextComponent;

/**
 * Extension of StringTextComponent that translates color codes.
 */
public class ColoredTextComponent extends StringTextComponent {
    public ColoredTextComponent(String value) {
        super(Messenger.translateColorCodes(value));
    }
}
