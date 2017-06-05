package kr.ac.kookmin.cs.bigdata;

import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.TextFormat.ParseException;

import edu.stanford.nlp.simple.Sentence;

public class SentimentScore extends Configured implements Tool {
	private static SentiWordNet sentiWordNet ;
    public static void main(String[] args) throws Exception {
    	System.out.println(Arrays.toString(args));
        int res = ToolRunner.run(new Configuration(), new SentimentScore(), args);
        System.exit(res);
    }

    @Override
    public int run(String[] args) throws Exception {
    	sentiWordNet = new SentiWordNet("SentiWordNet_3.0.0_20130122.txt");
    	 Path p1 = new Path(args[0]);
         Path p2 = new Path(args[1]);
         Path out = new Path(args[2]);
         
         Job job = Job.getInstance(getConf());
         job.setJarByClass(SentimentScore.class);
         job.setOutputKeyClass(Text.class);
         job.setOutputValueClass(Text.class);

         job.setMapperClass(Map.class);
         job.setReducerClass(Reduce.class);

        job.setOutputFormatClass(TextOutputFormat.class);
         
        MultipleInputs.addInputPath(job, p1, TextInputFormat.class, Map.class);
        MultipleInputs.addInputPath(job, p2, TextInputFormat.class, Map.class);
        
        FileOutputFormat.setOutputPath(job, out);
        job.waitForCompletion(true);
 		return 0;



    }
   
    public static class Map extends Mapper<LongWritable, Text, Text, Text> {
        private Text asin = new Text();
        private Text text = new Text();

        @Override
        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
        	try{
            JsonObject jsonObject = new JsonParser().parse(value.toString()).getAsJsonObject();
            asin.set(jsonObject.get("asin").toString());
            if(jsonObject.has("reviewText"))
            	text.set(jsonObject.get("reviewText").toString());
            else if(jsonObject.has("salesRank"))
            	text.set(jsonObject.getAsJsonObject("salesRank").get("Books").toString());
            else
            	text.set("-1");
            
            context.write(asin, text);
        	}
        	catch(ParseException e){
        		e.printStackTrace();
        	}
        }
    }

    public static class Reduce extends Reducer<Text, Text, Text, Text> {
    	
    	Text mergeText = new Text();
    	String merge = "";
    	 public void reduce(Text key, Iterable<Text> values, Context context)
                 throws IOException, InterruptedException {
    		 	int count = 0;
    		 	double score = 0;
    		 	long rankScore = -1;
    		 	for (Text value : values) {
    		 	    char token = 'o';
    		 	    count++;
    		 	    String str = value.toString();
    		 	  
    		 	   if(!str.isEmpty())
    		 		  token = str.charAt(0);
    		 	  
 	        	   if(Character.isDigit(token))
 	        		  rankScore = Long.valueOf(str);
 	        	   else
 	        	   {
 	        		  count++;
 	        		  score += sentiWordNet.getSentiScore(new Sentence(str));
 	        	   }
 	           }
    		 	double avg = score / count;
    		 	merge = String.valueOf(avg) + "	::	" + String.valueOf(rankScore);
    		 	mergeText.set(merge);
 	           context.write(key, mergeText);
        }
    }
}