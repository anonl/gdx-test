package nl.weeaboo.gdx.test.pixmap;

import java.util.Locale;

import org.junit.Assert;

import com.badlogic.gdx.graphics.Pixmap;

public class PixmapEquality {

    /**
     * Allow a small difference in color to account for rounding errors and other insignificant
     * platform-dependent behavior.
     */
    private int maxColorDiff = 0;

    public void assertEquals(Pixmap expected, Pixmap actual) {
        // Check pixmap dimensions
        if (expected.getWidth() != actual.getWidth() || expected.getHeight() != actual.getHeight()) {
            Assert.fail(String.format(Locale.ROOT, "Actual size (%dx%d) doesn't match expected size (%dx%d)",
                    actual.getWidth(), actual.getHeight(), expected.getWidth(), expected.getHeight()));
        }

        // Check pixel values
        for (int y = 0; y < expected.getHeight(); y++) {
            for (int x = 0; x < expected.getWidth(); x++) {
                int expectedPixel = expected.getPixel(x, y);

                int actualPixel = actual.getPixel(x, y);
                if (expectedPixel != actualPixel) {
                    int er = (expectedPixel>>24) & 0xFF;
                    int eg = (expectedPixel>>16) & 0xFF;
                    int eb = (expectedPixel>> 8) & 0xFF;
                    int ea = (expectedPixel    ) & 0xFF;

                    int ar = (actualPixel>>24) & 0xFF;
                    int ag = (actualPixel>>16) & 0xFF;
                    int ab = (actualPixel>> 8) & 0xFF;
                    int aa = (actualPixel    ) & 0xFF;

                    if (!cequal(ea, aa) || !cequal(er, ar) || !cequal(eg, ag) || !cequal(eb, ab)) {
                        throw new AssertionError(String.format(Locale.ROOT,
                            "Pixels at (%d,%d) not equal (maxDiff=%d): expected=%08x, actual=%08x",
                            x, y, maxColorDiff, expectedPixel, actualPixel));
                    }
                }
            }
        }
    }

    /**
     * return {@code true} if the two values are no more than {@code maxColorDiff} apart.
     */
    private boolean cequal(int a, int b) {
        Assert.assertTrue(a >= 0 && a <= 255);
        Assert.assertTrue(b >= 0 && b <= 255);

        return Math.abs(a - b) <= maxColorDiff;
    }

    public void setMaxColorDiff(int maxDiff) {
        Assert.assertTrue(maxDiff >= 0);

        this.maxColorDiff = maxDiff;
    }

}
