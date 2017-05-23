import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Set;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.io.LongWritable;




public class Stripes {

  public static class StripesOccurrenceMapper extends Mapper<LongWritable,Text,Text, myMapWritable> {
      private myMapWritable occurrenceMap = new myMapWritable();
      private Text word = new Text();

     // @Override
     protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
       //int neighbors = context.getConfiguration().getInt("neighbors", 2);
       String[] tokens = value.toString().split("\\s+");
       if (tokens.length > 1) {
          for (int i = 0; i < tokens.length; i++) {
              word.set(tokens[i]);
              occurrenceMap.clear();

              //int start = (i - neighbors < 0) ? 0 : i - neighbors;
              //int end = (i + neighbors >= tokens.length) ? tokens.length - 1 : i + neighbors;
               //for (int j = start; j <= end; j++)
                for (int j = 0; j < tokens.length; j++){
                    if (j == i) continue;
                    if (terms[j].length() == 0)
                        continue;
                    Text neighbor = new Text(tokens[j]);
                    if(occurrenceMap.containsKey(neighbor)){
                       IntWritable count = (IntWritable)occurrenceMap.get(neighbor);
                       occurrenceMap.put(neighbor, new IntWritable(count.get()+1));
                       //count.set(count.get()+1);
                    }else{
                       occurrenceMap.put(neighbor,new IntWritable(1));
                    }
                    
                    
               }
               word.set(tokens[i]);
               context.write(word,occurrenceMap);
              
         }
       }
      }
    }
  
  public static class StripesReducer extends Reducer<Text, MapWritable, Text, myMapWritable> {
      private myMapWritable incrementingMap = new myMapWritable();

      //@Override
      protected void reduce(Text key, Iterable<myMapWritable> values, Context context) throws IOException, InterruptedException {
          incrementingMap.clear();
          for (myMapWritable value : values) {
              addAll(value);
          }
          context.write(key, incrementingMap);
      }

      private void addAll(myMapWritable mapWritable) {
          Set<Writable> keys = mapWritable.keySet();
          for (Writable key : keys) {
              IntWritable fromCount = (IntWritable) mapWritable.get(key);
              if (incrementingMap.containsKey(key)) {
                  IntWritable count = (IntWritable) incrementingMap.get(key);
                  count.set(count.get() + fromCount.get());
                  incrementingMap.put(key, count);
              } else {
                  incrementingMap.put(key, fromCount);
              }
          }
      }
  }
  public static class myMapWritable extends MapWritable{
   @Override
      public String toString(){
          String s = new String("{ ");
          Set<Writable> keys = this.keySet();
              for(Writable key : keys){
                      IntWritable count = (IntWritable) this.get(key);
                      s =  s + key.toString() + "=" + count.toString() + ",";
      }

          s = s + " }";
          return s;
     }
    }

  

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "stripes");
    job.setJarByClass(Stripes.class);
    job.setMapperClass(StripesOccurrenceMapper.class);
    job.setCombinerClass(StripesReducer.class);
    job.setReducerClass(StripesReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(myMapWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}