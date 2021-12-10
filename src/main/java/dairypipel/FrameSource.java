/*
 * Demo Dairy
 */

package dairypipel;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList; 
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;


/** FrameSource that simulates a farm camera to generate farm images for processing. */
public class FrameSource implements Iterator<Frame>, Serializable {
    public static final Logger LOG = LoggerFactory.getLogger(FrameSource.class);
    private static final String FRAME_DIRECTORY = "/home/yy354/images";
    private static final int NUM_FRAME_SAMPLES = 50;
    private final Random rand = new Random();

    private int numExperiments = 500;
    private String frameSize;

    private long frameIdx;
    static int recw = 352;
    static int rech = 240;

    public static ArrayList<String> StartProcessTimes = new ArrayList<String>();

    public FrameSource(int numExperiments, String frameSize) {
        this.numExperiments = numExperiments;
        this.frameSize = frameSize;
        this.frameIdx = 0;
    }

    @Override
    public boolean hasNext() {
        return frameIdx < numExperiments;
    }

    @Override
    public Frame next() {
        try {
            TimeUnit.MILLISECONDS.sleep(50);
            String imagePath = "/home/yy354/images/test200k.jpg";//String.format("%s/image0.jpg", FRAME_DIRECTORY);
            // String imagePath = String.format("%s/%s/image%d.jpg", FRAME_DIRECTORY, frameSize, rand.nextInt(NUM_FRAME_SAMPLES));
            // resize the bufferedImage frame
            BufferedImage image = ImageIO.read(new File(imagePath));
            Image resultingImage = image.getScaledInstance(recw, rech, Image.SCALE_DEFAULT);
            BufferedImage outputImage = new BufferedImage(recw, rech, BufferedImage.TYPE_INT_RGB);
            outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
            // ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // ImageIO.write(image, "jpg", baos);
            Instant instant = Clock.systemUTC().instant();
            long nano = instant.getEpochSecond() * 1000000000 + instant.getNano();
            String arriveTimeStamp = Long.toString(this.frameIdx) + "," + Long.toString(nano);
            StartProcessTimes.add(arriveTimeStamp);
            if(StartProcessTimes.size() == numExperiments ){
                for(int i = 0 ; i < StartProcessTimes.size(); i++){
                    LOG.info("\n---0. Frame Sent:" + StartProcessTimes.get(i));
                }
            }
            return new Frame(this.frameIdx, nano, outputImage);
        } catch (IOException e) {
            LOG.info("Failed to fetch the image file");
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
           LOG.info("Sleep between sends is interrupted");
           e.printStackTrace();
           return null;
        } finally {
            frameIdx++;
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    public static DataStream<Frame> getSource(StreamExecutionEnvironment env, int numExperiments, String frameSize) {
        return env.fromCollection(new FrameSource(numExperiments, frameSize), Frame.class);
    }
}
