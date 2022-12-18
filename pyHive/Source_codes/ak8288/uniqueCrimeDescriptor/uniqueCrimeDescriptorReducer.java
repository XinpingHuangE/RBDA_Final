import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class uniqueCrimeDescriptorReducer extends Reducer<Text,IntWritable,Text,IntWritable>{

    @Override
    public void reduce(Text key, Iterable<IntWritable> values, Context context)
            throws IOException, InterruptedException {
        int totalOffenceCount = 0;
        for(IntWritable value : values){
            totalOffenceCount += value.get();
        }
        context.write(key,new IntWritable(totalOffenceCount));
    }

}