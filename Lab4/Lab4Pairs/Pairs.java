import java.io.*;
import java.util.*;
	
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class Pairs {

	public static class PairsOccurrenceMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
	    //private WordPair wordPair = new WordPair();
	    private IntWritable ONE = new IntWritable(1);
	    //private Text text = new Text();	

	    @Override
	    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
	        //int neighbors = context.getConfiguration().getInt("neighbors", 2);
	        String[] tokens = value.toString().split("\\s+");
	        if (tokens.length > 1) {
	          for (int i = 0; i < tokens.length; i++) {
	              //text.set(tokens[i]);
			

	            // int start = (i - neighbors < 0) ? 0 : i - neighbors;
	            // int end = (i + neighbors >= tokens.length) ? tokens.length - 1 : i + neighbors;
	              for (int j = 0; j < tokens.length; j++) {
	                  	if (j == i) continue;
	                   //wordPair.setNeighbor(tokens[j]);
			   			Text text = new Text();
			   			String str = tokens[i] + "\t" + tokens[j];
			   			text.set(str);
	                   	context.write(text, ONE);
	              }
	          }
	      }
	  }
	}
	
	public static class PairsReducer extends Reducer<Text,IntWritable,Text,IntWritable> {
	    private IntWritable totalCount = new IntWritable();
	    @Override
	    protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
	        int count = 0;
	        for (IntWritable value : values) {
	             count += value.get();
	        }
	        totalCount.set(count);
	        context.write(key,totalCount);
	    }
	}

public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "pairs");
    job.setJarByClass(Pairs.class);
    job.setMapperClass(PairsOccurrenceMapper.class);
    job.setCombinerClass(PairsReducer.class);
    job.setReducerClass(PairsReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
	
}