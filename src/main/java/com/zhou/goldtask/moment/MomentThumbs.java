package com.zhou.goldtask.moment;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * 手机传来的小图。尺寸或体积不对就丢掉，客户端继续下原图。
 */
public final class MomentThumbs {
    /** 小图最长边不能超过 800。 */
    public static final int MAX_EDGE = 800;
    public static final int MAX_BYTES = 1024 * 1024;

    private MomentThumbs() {
    }

    public static byte[] accept(byte[] source, String contentType) {
        if (source == null || source.length == 0 || source.length > MAX_BYTES) {
            return null;
        }
        if (contentType != null && !contentType.toLowerCase().startsWith("image/")) {
            return null;
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(source));
            if (image == null) {
                return null;
            }
            int edge = Math.max(image.getWidth(), image.getHeight());
            if (edge <= 0 || edge > MAX_EDGE) {
                return null;
            }
            return source;
        } catch (IOException ex) {
            return null;
        }
    }
}
