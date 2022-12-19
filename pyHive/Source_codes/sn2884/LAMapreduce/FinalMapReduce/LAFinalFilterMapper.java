import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

enum MissingLACrimeData{
	Date,
	Time,
	Lat,
	Lon,
	Location,
	Category
}

public class LAFinalFilterMapper extends Mapper<LongWritable, Text, NullWritable, Text>  {

	//private static int id = 1;
	
	static String[] crimeOrder = new String[]{"SEXUAL OFFENCES","THEFT","ASSAULT","MAJOR THEFT","FRAUD","CRIME AGAINST CHILDREN","DRUGS","LYNCHING","SHOOTING","HOMICIDE","TERRORISM","HUMAN TRAFFICKING","TRAFFIC","KIDNAPPING"};
    static HashMap<String,HashSet<String>> crimeMap = new HashMap<String,HashSet<String>>(){{
        put("SEXUAL OFFENCES",new HashSet<String>(Arrays.asList("indecentexposure","prostitution","penetration","rape","copulation","sexual","sex","incest","porn")));
        put("THEFT",new HashSet<String>(Arrays.asList("theft","pickpocket","bunco","snatching","stolen","larceny","burglar")));
        put("ASSAULT",new HashSet<String>(Arrays.asList("battery","assault","jostling","jostle","felony")));
        put("MAJOR THEFT",new HashSet<String>(Arrays.asList("grandtheft","trespas","embezzlement","prowler","robbery","vandalism")));
        put("FRAUD",new HashSet<String>(Arrays.asList("forgery","fraud","counterfeit","bribe","deceptive","deception")));
        put("CRIME AGAINST CHILDREN",new HashSet<String>(Arrays.asList("childstealing","childabandon","children","childabuse")));
        put("DRUGS",new HashSet<String>(Arrays.asList("drugs","gambling","narcotic")));
        put("LYNCHING",new HashSet<String>(Arrays.asList("lynch","riot","arson")));
        put("SHOOTING",new HashSet<String>(Arrays.asList("weaponschool","firearms","deadlyweapon","weapon","shots")));
        put("HOMICIDE",new HashSet<String>(Arrays.asList("homicide","manslaughter","murder")));
        put("TERRORISM",new HashSet<String>(Arrays.asList("bomb")));
        put("HUMAN TRAFFICKING",new HashSet<String>(Arrays.asList("humantrafficking")));
        put("TRAFFIC",new HashSet<String>(Arrays.asList("intoxicated","impaired","reckless","drunk")));
        put("KIDNAPPING",new HashSet<String>(Arrays.asList("kidnap","extortion")));
        // put("",new HashSet<String>(Arrays.asList()));
    }};
    static HashMap<String,Integer> severity = new HashMap<String,Integer>(){{
        put("SEXUAL OFFENCES",4);
        put("THEFT",2);
        put("ASSAULT",2);
        put("MAJOR THEFT",3);
        put("FRAUD",2);
        put("CRIME AGAINST CHILDREN",4);
        put("DRUGS",4);
        put("LYNCHING",3);
        put("SHOOTING",5);
        put("HOMICIDE",5);
        put("TERRORISM",5);
        put("HUMAN TRAFFICKING",5);
        put("TRAFFIC",2);
        put("KIDNAPPING",3);
        put("OTHER",1);
    }};
    
    
    static HashMap<Integer,Integer> severityMapping =  new HashMap<Integer,Integer>(){{
    	put(1,2);
    	put(2,40);
    	put(3,800);
    	put(4,16000);
    	put(5,320000);
    }};
    
	@Override
	  public void map(LongWritable key, Text value, Context context)
	      throws IOException, InterruptedException {
		String line = value.toString();
		String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)",-1);
		if (!values[0].equals("DR_NO"))  //Ignore 1st row
		{
			String crimeTime = "";
			String crimeDate = "";
			String location="";
			String crimeDesc = "";
			String category ="";
			String crimeLat = "";
			String crimeLon = "";
			String city = "LA";
			int crimeSeverity = 1;
			int timeBucket=0;
			//String idLA = "LA_" + Integer.toString(id);
			
			System.out.println(values.length);
			
			if (values.length>=3 && values[2]!=null) {
			crimeDate = values[2].split("\\s")[0];//substring(0,10);
			}
			else {
				context.getCounter(MissingLACrimeData.Date).increment(1);
			}
			if (crimeDate!=null && crimeDate.length()>=10 && crimeDate.substring(6,10)!=null && !(crimeDate.substring(6,10).equals("2010") || crimeDate.substring(6,10).equals("2011")))
				{
					if (values.length>=4 && values[3]!=null)
						crimeTime = values[3].substring(0,2)+":"+values[3].substring(2,4)+":00";
					else {
						context.getCounter(MissingLACrimeData.Time).increment(1);
					}
				if (values.length>=6 && values[5]!=null)
					location = values[5];
				else
					context.getCounter(MissingLACrimeData.Location).increment(1);
				if (values.length>=10 && values[9]!=null)
					{
					crimeDesc = values[9];
					Iterator<Entry<String, HashSet<String>> > criMapIter
		            = crimeMap.entrySet().iterator();
					
					while (criMapIter.hasNext()) {
						Map.Entry<String, HashSet<String>> newMap
		                = (Map.Entry<String, HashSet<String>>)
		                		criMapIter.next();
						
						HashSet<String> new_set= (HashSet<String>) newMap.getValue();
						
						for (String crime : new_set ) {
							if (crimeDesc.toLowerCase().contains(crime)){
								category = newMap.getKey();
								break;
							}
						}
						
						if (category!="")
								break;
						
					}
					if (category=="")
						category = "OTHER";
					}
				else
					{
					context.getCounter(MissingLACrimeData.Category).increment(1);
					category = "OTHER";
					}
				if (values.length>=27 && values[26]!=null)
					crimeLat = values[26];
				else
					context.getCounter(MissingLACrimeData.Lat).increment(1);
				if (values.length>=28 && values[27]!=null)
					crimeLon = values[27];
				else
					context.getCounter(MissingLACrimeData.Lon).increment(1);
				
				crimeSeverity = severityMapping.get(severity.get(category));
				
				if (location!="" && crimeTime!="")
				timeBucket = Integer.parseInt(crimeTime.split(":")[0]);
				context.write(NullWritable.get(), new Text(city+","+crimeDate + "," + crimeTime+"," + location + "," + category + "," + crimeLat + "," + crimeLon +"," + crimeSeverity + "," + timeBucket));
				
				}
			
		}
		
	}
	
}
