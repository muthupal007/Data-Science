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

import java.util.Locale;

import java.sql.Timestamp;

import java.text.SimpleDateFormat;


public class LemmaCooccur {
  private static HashMap<String,ArrayList<String>> lemmaMap = new HashMap<String,ArrayList<String>>();
  public static class LemmaMapper extends Mapper<Object, Text, Text, Text> {
      //private IntWritable ONE = new IntWritable(1);

      @Override
      protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
          String[] splitStr = value.toString().split(">");        
          try {
          String loc = splitStr[0] + ">";
          Text location = new Text();
          location.set(loc);
          Text word = new Text();
          String[] tokens = splitStr[1].replaceAll("[^a-zA-Z ]", "").toLowerCase().replaceAll("j","i").replaceAll("v","u").split("\\s+|\\t+");
          if (tokens.length > 1) {
            for (int i = 0; i < tokens.length; i++) {
                //tokens[i] = tokens[i].replaceAll("[^a-zA-Z ]", "").toLowerCase();
              for (int j = 0; j < tokens.length; j++) {
                /*tokens[i] = tokens[i].replaceAll("j","i");
                tokens[i] = tokens[i].replaceAll("v","u");
                tokens[i+1] = tokens[i+1].replaceAll("j","i");
                tokens[i+1] = tokens[i+1].replaceAll("v","u"); */
                ArrayList<String> lemmas1 = new ArrayList<String>();
                ArrayList<String> lemmas2 = new ArrayList<String>();
                if(lemmaMap.containsKey(tokens[i]) && lemmaMap.containsKey(tokens[j])) {
                    lemmas1 = lemmaMap.get(tokens[i]);
                    lemmas2 = lemmaMap.get(tokens[j]);
                    for(String lemma1 : lemmas1) {
                      for(String lemma2 : lemmas2){
                          word.set(lemma1 + " " + lemma2);
                          context.write(word, location); 
                          }                       
                    }
                }
                else if (lemmaMap.containsKey(tokens[i]) && !lemmaMap.containsKey(tokens[j])){
                    if(tokens[j].isEmpty()) continue;
                    lemmas1 = lemmaMap.get(tokens[i]);
                    for(String lemma1 : lemmas1) {
                      word.set(lemma1 + " " + tokens[j]);
                      context.write(word, location);
                    }

                }
                else if(!lemmaMap.containsKey(tokens[i]) && lemmaMap.containsKey(tokens[j])) {
                    if(tokens[i].isEmpty()) continue;
                    lemmas2 = lemmaMap.get(tokens[j]);
                    for(String lemma2 : lemmas2) {
                      word.set(tokens[i] + " " + lemma2);
                      context.write(word, location);
                }
              }
                else {
                  if(tokens[i].isEmpty()) continue;
                  if(tokens[j].isEmpty()) continue;
                    word.set(tokens[i] + " " + tokens[j]);
                    context.write(word,location);
                  
                }
                }
              }
            }
          }
        
          catch (Exception e){

          }

             
    }
  }
  
  public static class LemmaReducer extends Reducer<Text,Text,Text,Text> {
      private IntWritable totalCount = new IntWritable();
      
      @Override
      protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
          String str = "";
          int count = 0;
      
          for (Text value : values) {
               str = str + value.toString() + " "; 
               count++;
          }
          String result = str + " " + "Count = " + count;
          Text out = new Text(result);
          context.write(key,out);

      }
      
  }

public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    BufferedReader br = new BufferedReader(new FileReader("new_lemmatizer.csv"));
        String newLine;
        while ((newLine = br.readLine()) != null) {
            String[] splitLine = newLine.split(",");
            String word = splitLine[0].toLowerCase();
            ArrayList<String> lemmas = new ArrayList<String>();
            for(int i = 1; i < splitLine.length; i++) {
              lemmas.add(splitLine[i].toLowerCase());
            }
            LemmaCooccur.lemmaMap.put(word,lemmas);
        }
    Job job = Job.getInstance(conf, "Lemma");
    job.setJarByClass(LemmaCooccur.class);
    job.setMapperClass(LemmaMapper.class);
    //job.setCombinerClass(PairsReducer.class);
    job.setReducerClass(LemmaReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    //FileOutputFormat.setOutputPath(job, new Path(args[1]));
    String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss", Locale.US).format(new Timestamp(System.currentTimeMillis()));
    FileOutputFormat.setOutputPath(job, new Path(args[1] + "/" + timeStamp));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
  
}