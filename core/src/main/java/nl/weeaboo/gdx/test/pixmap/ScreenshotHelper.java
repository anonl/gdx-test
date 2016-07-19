package nl.weeaboo.gdx.test.pixmap;

import java.nio.ByteBuffer;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.utils.ScreenUtils;

public final class ScreenshotHelper {

    private ScreenshotHelper() {
    }

    /**
     * Takes a screenshot of the specified rectangle (in viewport coordinates). The resulting pixmap will
     * <em>not</em> be upside-down, unline {@link ScreenUtils#getFrameBufferPixmap(int, int, int, int)}.
     *
     * @return A pixmap in {@link Format#RGBA8888} format
     * @see ScreenUtils#getFrameBufferPixmap(int, int, int, int)
     */
    public static Pixmap screenshot(int x, int y, int w, int h) {
        byte[] pixelData = ScreenUtils.getFrameBufferPixels(x, y, w, h, true);

        Pixmap pixmap = new Pixmap(w, h, Format.RGBA8888);
        ByteBuffer buf = pixmap.getPixels();
        buf.put(pixelData);
        buf.rewind();
        return pixmap;
    }

}
