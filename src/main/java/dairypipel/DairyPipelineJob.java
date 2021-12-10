/*
 * Dairy Demo Example
 */

package dairypipel;

import java.lang.reflect.Field;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.api.common.operators.SlotSharingGroup;

/** Skeleton code for the datastream walkthrough. */
public class DairyPipelineJob {
    public static void main(String[] args) throws Exception {
        final ParameterTool params = ParameterTool.fromArgs(args);
        final String imageSize = params.get("imageSize", "10kb");
        final Long num = params.getLong("numExperiments", ExpConstants.NUMBER_OF_FRAMES);
        int numExperiments = num.intValue();
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setBufferTimeout(0);
        env.disableOperatorChaining();

	SlotSharingGroup ssg_src = SlotSharingGroup.newBuilder("src")
		.setCpuCores(1)
		.setTaskHeapMemoryMB(1024)
		.setExternalResource("src", 1.0)
		.build();
	SlotSharingGroup ssg_filter = SlotSharingGroup.newBuilder("filter")
		.setCpuCores(1)
		.setTaskHeapMemoryMB(1024)
		.setExternalResource("filter", 1.0)
		.build();
	SlotSharingGroup ssg_compute = SlotSharingGroup.newBuilder("compute")
		.setCpuCores(1)
		.setTaskHeapMemoryMB(1024)
		.setExternalResource("compute", 1.0)
		.build();
	SlotSharingGroup ssg_sink = SlotSharingGroup.newBuilder("sink")
		.setCpuCores(1)
		.setTaskHeapMemoryMB(1024)
		.setExternalResource("sink", 1.0)
		.build();

        DataStream<Frame> frames = FrameSource.getSource(env, numExperiments, imageSize);
	// if (frames instanceof SingleOutputStreamOperator<?>) {
	((SingleOutputStreamOperator<Frame>)frames).slotSharingGroup(ssg_src);
	//}
        DataStream<RecResult> results = 
                frames.map(new RecProcessor())
                .slotSharingGroup(ssg_filter)
		// .setParallelism(4)
                .name("filterModel")
                .map(new BCSProcessor())
                .slotSharingGroup(ssg_compute)
		// .setParallelism(4)
                .name("BCSModel");

        results.addSink(new FinalSink(numExperiments)).slotSharingGroup(ssg_sink).name("Store to memory");

        env.execute("Dairy Pipeline");
    }
}
