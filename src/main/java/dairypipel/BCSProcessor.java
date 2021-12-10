/*
 * Dairy Demo Experiment.
 */

package dairypipel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;

import java.io.File;
import java.io.IOException;
import java.lang.System;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList; 

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.util.Collector;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.SavedModelBundle.Loader;
import org.tensorflow.framework.ConfigProto;
import org.tensorflow.framework.GPUOptions;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;

public class BCSProcessor implements MapFunction<Frame, RecResult> {
    public static final Logger LOG = LoggerFactory.getLogger(BCSProcessor.class);
    /** BCS score ML process */
    static int bcsw = 300;
    static int bcsh = 300;
    static String BCS_MODEL_DIR = "/home/yy354/models/cow_bcs/model_10steps";
    // static SavedModelBundle model = SavedModelBundle.load(BCS_MODEL_DIR, "serve");
    static ThreadLocal<Session> ses = new ThreadLocal<Session>();

    private static void initialize() {
        if (ses.get() != null) return;
	ses.set(SavedModelBundle.loader(BCS_MODEL_DIR)
		.withConfigProto(
		    ConfigProto.newBuilder()
		    	.setGpuOptions(
				GPUOptions.newBuilder()
				.setAllowGrowth(true)
				.setPerProcessGpuMemoryFraction(0.20)
				.build())
			.build().toByteArray()
		    )
		.withTags("serve")
		.load()
		.session());
    }

    public static ArrayList<String> StartProcessTimes = new ArrayList<String>();
    public static ArrayList<String> endProcessTimes = new ArrayList<String>();

    @Override
    public RecResult map(Frame frameObj) throws Exception {
        Instant instant = Clock.systemUTC().instant();
        long nano = instant.getEpochSecond() * 1000000000 + instant.getNano();
        long frameID = frameObj.getFrameId();
        String arriveTimeStamp = Long.toString(frameObj.getFrameId()) + "," + Long.toString(nano);
        StartProcessTimes.add(arriveTimeStamp);
        
        Image resultingImage = frameObj.frame.getScaledInstance(bcsw, bcsh, Image.SCALE_DEFAULT);
        BufferedImage outputImage = new BufferedImage(bcsw, bcsh, BufferedImage.TYPE_INT_RGB);
        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        DataBufferInt data = (DataBufferInt) outputImage.getData().getDataBuffer();
        // normalize each entry of the databuffer
        int dataLen = data.getSize();
        float[] formatData = new float[dataLen * 3];
        for (int i = 0; i < dataLen; i++) {
              formatData[i] = (float) (data.getElemDouble(i) / 255.0);
              int second = dataLen + i;
              formatData[second] = (float) (data.getElemDouble(i) / 255.0);
              int third = 2 * dataLen + i;
              formatData[third] = (float) (data.getElemDouble(i) / 255.0);
        }

        // Expand dimensions to have shape: [1, None, None]
        long[] shape = new long[] { 1, bcsw, bcsh, 3 };
        Tensor<Float> input = Tensor.create(shape, FloatBuffer.wrap(formatData));
        
	initialize();

        Tensor<?> output = ses.get().runner().feed("serving_default_conv2d_5_input", input)
                  .fetch("StatefulPartitionedCall", 0)
                  .run().get(0);
        FloatBuffer dst =  FloatBuffer.allocate(10);;
        output.writeTo(dst);
        RecResult rec = new RecResult();
        rec.setId(frameObj.getFrameId());
        rec.setBCSResult(dst.get(0));
        // collector.collect(prev);
        instant = Clock.systemUTC().instant();
        nano = instant.getEpochSecond() * 1000000000 + instant.getNano();
        String endTimeStamp = Long.toString(frameObj.getFrameId()) + "," + Long.toString(nano);
        endProcessTimes.add(endTimeStamp);

        if(endProcessTimes.size() >= ExpConstants.NUMBER_OF_FRAMES ){
            for(int i = 0 ; i < endProcessTimes.size(); i++){
                LOG.info("\n---2.Before ProcessorBCS:" + StartProcessTimes.get(i));
                LOG.info("\n---2.Finish ProcessorBCS:" + endProcessTimes.get(i));
            }
        }
        return rec;
    }
}
