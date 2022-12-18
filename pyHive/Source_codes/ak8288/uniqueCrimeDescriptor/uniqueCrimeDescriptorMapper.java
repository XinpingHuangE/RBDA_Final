import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

enum ANOMALY{
    MISSING
}

public class uniqueCrimeDescriptorMapper extends Mapper<LongWritable,Text,Text,IntWritable>{
    
    @Override
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException{
        String input = value.toString();
        String[] row = input.split(",");
        String CATEGORY = row[8];

        context.write(new Text(CATEGORY),new IntWritable(1));
    }

}