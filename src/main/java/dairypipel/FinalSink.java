/*
 * Demo Dairy
 */

package dairypipel;

import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList; 
import java.util.Date;
import java.util.HashMap; 


/** A sink for outputting rec result. */
@PublicEvolving
@SuppressWarnings("unused")
public class FinalSink implements SinkFunction<RecResult> {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(FinalSink.class);
    // hashmap:  ID -> BCS

    public static HashMap<Long, Float> results = new HashMap<Long, Float>();
    public int TOTAL_FRAMES = 500;
    static ArrayList<String> StartProcessTimes = new ArrayList<String>();
    static ArrayList<String> endProcessTimes = new ArrayList<String>();

    public FinalSink(int total_frames){
        this.TOTAL_FRAMES = total_frames;
    }

    @Override
    public void invoke(RecResult value, Context context) {

        Instant instant = Clock.systemUTC().instant();
        long nano = instant.getEpochSecond() * 1000000000 + instant.getNano();
        String arriveTimeStamp = Long.toString(value.getId()) + "," + Long.toString(nano);
        StartProcessTimes.add(arriveTimeStamp);

        Date date = new Date();
        long timeMilliRec = date.getTime();
        results.put(value.getId(),value.getBCSResult());

        instant = Clock.systemUTC().instant();
        nano = instant.getEpochSecond() * 1000000000 + instant.getNano();
        String endTimeStamp = Long.toString(value.getId()) + "," + Long.toString(nano);
        endProcessTimes.add(endTimeStamp);


        if(StartProcessTimes.size() == TOTAL_FRAMES ){
            for(int i = 0 ; i < StartProcessTimes.size(); i++){
                LOG.info("\n---4.Before save:" + StartProcessTimes.get(i));
                LOG.info("\n---4.Finish save:" + endProcessTimes.get(i));
            }
        }
        LOG.info(value.toString());
    }
}
