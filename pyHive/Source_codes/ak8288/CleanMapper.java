import java.io.IOException;
import java.util.Arrays;
import java.text.SimpleDateFormat;  
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.text.DateFormat;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

enum ANOMALY{
    MISSING
}

public class CleanMapper extends Mapper<LongWritable,Text,NullWritable,Text>{
    static int id = 1;
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
    static HashMap<String,Integer> severityMap = new HashMap<String,Integer>(){{
        put("SEXUAL OFFENCES",4);
        put("THEFT",2);
        put("ASSAULT",3);
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
    static HashMap<Integer,Integer> categoryToSeveritymap = new HashMap<Integer,Integer>(){{
        put(1,2);
        put(2,40);
        put(3,800);
        put(4,16000);
        put(5,320000);
    }};

    public boolean isValidRow(String date,String time,String category,String loc,String lat,String lon){
        if(date.equals("")) return false;
        if(time.equals("")) return false;
        if(category.equals("")) return false;
        if(loc.equals("")) return false;
        if(lat.equals("")) return false;
        if(lon.equals("")) return false;

        // filter out last ten years data by time
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        try{
            Date cal_date = formatter.parse(date);
            int year = cal_date.getYear() + 1900;
            if(year < 2012)
                return false;
        }catch (Exception e){
            System.out.println("exception : "+e);
            return false;
        }
        return true;
    }

    public String getCategoryFromCrimeDescription(String crimeDesc){
        String category = "OTHER";
        String temp = crimeDesc.replaceAll("[\\-+.:/,&=()\n$\"'\'\\d]","");
        temp = temp.toLowerCase().trim();
        for(String crime : crimeOrder){
            for(String keyWord : crimeMap.get(crime)){
                if(temp.contains(keyWord)){
                    return crime;
                }
            }
        }
        return category;
    }

    public static String getTimeBucketFromTimeStamp(String timestamp){
        DateFormat format = new SimpleDateFormat("HH:mm:ss");
        try{
            Date date = format.parse(timestamp);
            int hours = date.getHours();
            return Integer.toString(hours);
        }
        catch (Exception e){
            //
        }
        return "ERROR";
    }

    @Override
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException{
        String input = value.toString();
        String[] row = input.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

        String rowID = "NYC_"+Integer.toString(id++);
        String CITY = "NYC";
        String CRIME_DATE = row[1];
        String CRIME_TIME = row[2];
        String CRIME_DESC = row[8];
        String LOCATION = row[13];
        String LATITUDE = row[row.length-8];
        String LONGITUDE = row[row.length-7];
        String CATEGORY = "";
        String SEVERITY = "";
        String TIME_BUCKET = "";                             // Put the crime_time (timestamp) to one of 24 buckets, each for an hour of the day

        if(isValidRow(CRIME_DATE,CRIME_TIME,CRIME_DESC,LOCATION,LATITUDE,LONGITUDE)){
            CATEGORY = getCategoryFromCrimeDescription(CRIME_DESC);
            int mainCategory = severityMap.get(CATEGORY);  // very low, low, medium, high and very high
            SEVERITY = Integer.toString(categoryToSeveritymap.get(mainCategory));
            TIME_BUCKET = getTimeBucketFromTimeStamp(CRIME_TIME);

            // city, date , time, loc, category, lat ,lon, severity_by_cateogry [final], time_bucket ==> Final format
            String output = CITY+","+CRIME_DATE+","+CRIME_TIME+","+LOCATION+","+CATEGORY+","+LATITUDE+","+LONGITUDE+","+SEVERITY+","+TIME_BUCKET;
            context.write(NullWritable.get(),new Text(output));
        }
        else context.getCounter(ANOMALY.MISSING).increment(1);
    }

}