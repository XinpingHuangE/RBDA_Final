
import java.io.IOException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

public class CrimeDescReducer  extends Reducer<Text, IntWritable, Text, IntWritable> {
	
		
	  @Override
	  public void reduce(Text key, Iterable<IntWritable> values, Context context)
	      throws IOException, InterruptedException {
	    
		   int noOfCrimes = 0;
	    for (IntWritable value : values) {
	    	
	    	noOfCrimes +=value.get();
	    }
	    
	    context.write(key, new IntWritable(noOfCrimes));

}

}
