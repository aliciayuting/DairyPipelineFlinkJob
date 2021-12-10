package dairypipel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

/** A simple frame record . */
@SuppressWarnings("unused")
public final class Frame {

    public static final Logger FrameLOG = LoggerFactory.getLogger(Frame.class);
    public long frameId;
    public long sentTime;
    public long recTime = 0;
    public long bcsTime = 0;
    public BufferedImage frame = null; // For storing image in RAM

    public Frame() {}

    public Frame(long frameId, long timestamp, BufferedImage frame) {
        this.frameId = frameId;
        this.sentTime = timestamp;
        this.frame = frame;
    }

    public long getFrameId() {
        return this.frameId;
    }

}
