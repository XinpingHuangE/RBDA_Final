import java.io.IOException;


	import org.apache.hadoop.io.LongWritable;
	import org.apache.hadoop.io.IntWritable;
	import org.apache.hadoop.io.Text;
	import org.apache.hadoop.mapreduce.Mapper;


public class CrimeDescMapper extends Mapper<LongWritable, Text, Text, IntWritable>  {

		
		
		@Override
		  public void map(LongWritable key, Text value, Context context)
		      throws IOException, InterruptedException {
			String line = value.toString();
			String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)",-1);
			if (!values[0].equals("DR_NO"))  //Ignore 1st row
			{
				
				String crimeDate = "";
				String crimeDesc = "";
				
				
				System.out.println(values.length);
				
				if (values.length>=3 && values[2]!=null) {
				crimeDate = values[2].split("\\s")[0];//substring(0,10);
				}
				
				if (crimeDate!=null && crimeDate.length()>=10 && crimeDate.substring(6,10)!=null && !(crimeDate.substring(6,10).equals("2010") || crimeDate.substring(6,10).equals("2011")))
					{
						
					if (values.length>=10 && values[9]!=null)
						crimeDesc = values[9];
					
					context.write(new Text(crimeDesc), new IntWritable(1));
					}
				
			}
			
		}
		
	}


