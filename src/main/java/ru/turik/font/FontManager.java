package ru.turik.font;

import ru.turik.Turik;

import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owner of the bundled Turik typeface (Manrope, OFL license, full
 * Cyrillic coverage). Hands out CachedFonts per size/weight and validates
 * that the font can really render the strings the client shows.
 */
public final class FontManager {
    private static final String FONT_RESOURCE = "/assets/turik/fonts/Manrope-Regular.ttf";

    private final Map<Long, CachedFont> cache = new ConcurrentHashMap<>();
    private boolean broken;

    public void init() {
        // Force-load a probe font so a missing resource fails loudly, not silently.
        try (InputStream in = FontManager.class.getResourceAsStream(FONT_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Turik font resource missing: " + FONT_RESOURCE);
            }
            in.readAllBytes();
            Turik.LOGGER.info("[Turik] font system ready (Manrope, Cyrillic + Latin)");
        } catch (Exception e) {
            broken = true;
            Turik.LOGGER.error("[Turik] failed to load bundled font", e);
        }
    }

    public boolean isBroken() {
        return broken;
    }

    /** Cache key: size (float bits) << 32 | type ordinal. */
    private static long key(float size, FontType type) {
        return ((long) Float.floatToIntBits(size) << 32) | type.ordinal();
    }

    public CachedFont font(float size, FontType type) {
        return cache.computeIfAbsent(key(size, type), k -> {
            try (InputStream in = FontManager.class.getResourceAsStream(FONT_RESOURCE)) {
                return new CachedFont(in, size, type);
            } catch (Exception e) {
                Turik.LOGGER.error("[Turik] cannot open font for size " + size, e);
                throw new IllegalStateException("font open failed", e);
            }
        });
    }

    /** Coverage check used by the test report: does the font contain these chars? */
    public boolean supports(String text) {
        try (InputStream in = FontManager.class.getResourceAsStream(FONT_RESOURCE)) {
            var awt = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, in);
            for (int i = 0; i < text.length(); i++) {
                if (!awt.canDisplay(text.charAt(i))) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String coverageReport() {
        String sample = "Turik 0123456789 !@#$% Абвгдеёжзийклмнопрстуфхцчшщъыьэюя";
        return supports(sample) ? "OK: Latin, digits, specials, Cyrillic"
                : "MISSING glyphs in " + sample.toLowerCase(Locale.ROOT);
    }
}
