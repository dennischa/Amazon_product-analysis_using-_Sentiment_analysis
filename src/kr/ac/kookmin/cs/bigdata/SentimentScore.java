package kr.ac.kookmin.cs.bigdata;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
    	System.out.println(Arrays.toString(args));
    	sentiWordNet = new SentiWordNet("SentiWordNet_3.0.0_20130122.txt");

        Job job = Job.getInstance(getConf());
        job.setJarByClass(SentimentScore.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setMapperClass(Map.class);
        job.setReducerClass(Reduce.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);
      
		return 0;

    }
   
    public static class Map extends Mapper<LongWritable, Text, Text, Text> {
        private Text asin = new Text();
	private Text review = new Text();

        @Override
        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
        	JsonParser jsonParser = new JsonParser(); 
        	JsonObject jsonObject = (JsonObject) jsonParser.parse(value.toString());
        	
        	asin.set(jsonObject.get("asin").toString());
        	review.set(jsonObject.get("reviewText").toString());
        	context.write(asin, review);
        }
    }

    public static class Reduce extends Reducer<Text, Text, Text, DoubleWritable> {
    	 public void reduce(Text key, Iterable<Text> values, Context context)
                 throws IOException, InterruptedException {
    		 	int count = 0;
    		 	double score = 0;
    	           for (Text val : values) {
    	        	   count++;
    	        	   String str = val.toString();
    	        	   score += sentiWordNet.getSentiScore(new Sentence(str));
    	           }
    	           score /= count;
    	           context.write(key, new DoubleWritable(score));
        }
    }
}
