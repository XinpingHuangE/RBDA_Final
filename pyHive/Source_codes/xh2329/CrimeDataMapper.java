import java.io.IOException;
import java.util.*;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

enum MissingData
{
	 Location,
	 Date,
	 Timestamp,
	 Category,
	 Latitude,
	 Longitude
}
public class CrimeDataMapper extends Mapper<LongWritable, Text, NullWritable, Text>{
	
	@Override
	public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
	{
		HashMap<String, String> locToName = new HashMap<String, String>()
		{{
			 put("001", "Central");
			 put("002", "Wentworth");
		         put("003", "Grand Crossing");
		         put("004", "South Chicago");
			 put("005", "Calumet");
	  		 put("006", "Gresham");
			 put("007", "Englewood");
			 put("008", "Chicago Lawn");
			 put("009", "Deering");
			 put("010", "Ogden");				   
			 put("011", "Harrison");
	          	 put("012", "Near West");
			 //put("013", "Near Wst");
		         put("014", "Shakespeare");
			 put("015", "Austin");
			 put("016", "Jefferson Park");
			 put("017", "Albany Park");
			 put("018", "Near North");
			 put("019", "Town Hall");
		  	 put("020", "Lincoln");
	    	         // put("021", "Wentworth");
			 put("022", "Morgan Park");
			 //put("023", "Town Hall");
			 put("024", "Rogers Park");
			 put("025", "Grand Central");
		}};
		HashMap<String, HashSet<String>> crimes = new HashMap<String, HashSet<String>>()
		{{
			 put("SEXUAL OFFENCES", new HashSet<String>(Arrays.asList("CRIM SEXUAL ASSAULT","CRIMINAL SEXUAL ASSAULT", "SEX OFFENSE")));
            		 put("THEFT",new HashSet<String>(Arrays.asList("THEFT","MOTOR VEHICLE THEFT","BURGLARY")));
            		 put("ASSAULT",new HashSet<String>(Arrays.asList("BATTERY","ASSAULT")));
            		 put("MAJOR THEFT" , new HashSet<String>(Arrays.asList("CRIMINAL TRESPASSING","ROBBERY")));
            		 put("FRAUD", new HashSet<String>(Arrays.asList("DECEPTIVE PRACTICE")));
            		 put("CRIME AGAINST CHILDREN",new HashSet<String>(Arrays.asList("OFFENSE INNVOLVING CHILDREN")));
            		 put("DRUGS", new HashSet<String>(Arrays.asList("GAMBLING","NARCOTICS", "OTHER NARCOTIC VIOLATION")));
            		 put("LYNCHING", new HashSet<String>(Arrays.asList( "ARSON")));
            		 put("SHOOTING" ,new HashSet<String>(Arrays.asList("WEAPONS VIOLATION")));
            		 put("HOMICIDE", new HashSet<String>(Arrays.asList("HOMICIDE")));
            	         put("HUMAN TRAFFICKING", new HashSet<String>(Arrays.asList("HUMAN TRAFFICKING")));
            		 put("KIDNAPPING", new HashSet<String>(Arrays.asList("KIDNAPPING")));  
		}};
		HashMap<String, Integer> severity = new HashMap<String, Integer>()
		{{
			 put("SEXUAL OFFENCES", 16000);
			 put("THEFT", 40);
			 put("ASSAULT", 40);
			 put("MAJOR THEFT", 800);
			 put("FRAUD", 40);
			 put("CRIME AGAINST CHILDREN", 16000);
			 put("DRUGS", 16000);
			 put("LYNCHING", 800);
			 put("SHOOTING", 320000);
			 put("HOMICIDE", 320000);
			 put("HUMAN TRAFFICKING", 320000);
			 put("KIDNAPPING", 800);
			 put("OTHER", 2);
		}};	 
		String line = value.toString();
		String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)",-1);
		String location = "";
		String date = "";
		String timeStamp = "";
		String category = "";
		String crime_class = "OTHER";
		String latitude = "";
		String longitude = "";
		int time_bucket = 0;
		String location_name = "";
		//0 id, 1 case num, 2 date, 3 block, 4 iucr, 5 primary types, 6 description, 7 location
		//8 arrest, 9 domestic, 10 beat, 11 district, 12 ward, 13 community, 14 fbi code, 15 x coord
		//16 y coord, 17 year, 18 updated on, 19 latitude, 20 longitude, 21 location
		if(!data[0].equals("ID") && !data[11].isEmpty() && !data[11].equals("031")) //ignore the first line of field names. 
		{
			int year = Integer.parseInt(data[17]);
			if(year >= 2012 && year <= 2022)
			{
				if(data[11].isEmpty()) context.getCounter(MissingData.Location).increment(1);
				else
			       	{
					location = data[11];
					if(location.equals("16")) location = "016";
					location_name = locToName.get(location); 
					context.getCounter("DistrictCode", location).increment(1);
				}
				if(data[2].isEmpty()) 
				{
					context.getCounter(MissingData.Date).increment(1);
					context.getCounter(MissingData.Timestamp).increment(1);
				}
				else
				{
					String[] time = data[2].split(" ");
					date = time[0];
					timeStamp = time[1]+time[2]; 
					time_bucket = Integer.parseInt(time[1].split(":")[0]);
					if(time[2].equals("PM")) time_bucket += 12;
					if(time_bucket == 24) time_bucket = 0;
					context.getCounter("bucket", String.valueOf(time_bucket)).increment(1);
				}
				if(data[5].isEmpty()) context.getCounter(MissingData.Category).increment(1);
				else 
				{
					category = data[5];
					for(Map.Entry<String, HashSet<String>> entry: crimes.entrySet())
					{
						if(entry.getValue().contains(category)) crime_class = entry.getKey();
					}
				}
				if(data[19].isEmpty() || data[20].isEmpty())
				{	
					if(data[19].isEmpty()) context.getCounter(MissingData.Latitude).increment(1);
					if(data[20].isEmpty()) context.getCounter(MissingData.Longitude).increment(1);
				}	
				else
				{
					latitude = data[19];
					longitude = data[20];
				}
				
				context.getCounter("Crime", category).increment(1);
				String cd = "CHICAGO," + date + "," + timeStamp + "," +  location_name + "," + crime_class + "," + latitude + "," + longitude+ "," + severity.get(crime_class) + "," +  time_bucket;
				if(!latitude.isEmpty()) context.write(NullWritable.get(), new Text(cd));
			}
		}
 	}

}

