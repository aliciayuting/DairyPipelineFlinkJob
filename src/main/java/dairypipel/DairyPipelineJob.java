/*
 * Dairy Demo Example
 */

package dairypipel;

import java.lang.reflect.Field;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/** Skeleton code for the datastream walkthrough. */
public class DairyPipelineJob {
    public static void main(String[] args) throws Exception {
        final ParameterTool params = ParameterTool.fromArgs(args);
        final String imageSize = params.get("imageSize", "10kb");
        final Long num = params.getLong("numExperiments", 500);
        int numExperiments = num.intValue();
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setBufferTimeout(1);
        env.disableOperatorChaining();
        DataStream<Frame> frames = FrameSource.getSource(env, numExperiments, imageSize);
        DataStream<RecResult> results = 
                frames.map(new RecProcessor())
                .slotSharingGroup("CowFilter")
                .name("filterModel")
                .map(new BCSProcessor())
                .slotSharingGroup("CowBCS")
                .name("BCSModel");

        results.addSink(new FinalSink(numExperiments)).slotSharingGroup("Store").name("Store to memory");

        env.execute("Dairy Pipeline");
    }
}
