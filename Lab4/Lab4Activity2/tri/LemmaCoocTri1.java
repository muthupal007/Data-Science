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


public class LemmaCoocTri1 {
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
            for (int i = 0; i < tokens.length - 2; i++) {
              int j = i + 1;
              int k = i + 2;
                //tokens[i] = tokens[i].replaceAll("[^a-zA-Z ]", "").toLowerCase();
              //for (int j = 0; j < tokens.length; j++) {
                //  for(int k = 0; k < tokens.length; k++) {
                //    if (i == j && j == k) continue;
                /*tokens[i] = tokens[i].replaceAll("j","i");
                tokens[i] = tokens[i].replaceAll("v","u");
                tokens[i+1] = tokens[i+1].replaceAll("j","i");
                tokens[i+1] = tokens[i+1].replaceAll("v","u"); */
                ArrayList<String> w1_lemmas = new ArrayList<String>();
                ArrayList<String> w2_lemmas = new ArrayList<String>();
                ArrayList<String> w3_lemmas = new ArrayList<String>();

                if(lemmaMap.containsKey(tokens[i]) && lemmaMap.containsKey(tokens[j]) && lemmaMap.containsKey(tokens[k])) {
                    w1_lemmas = lemmaMap.get(tokens[i]);
                    w2_lemmas = lemmaMap.get(tokens[j]);
                    w3_lemmas = lemmaMap.get(tokens[k]);
                    for(String w1_lemma : w1_lemmas) {
                      for(String w2_lemma : w2_lemmas){
                          for(String w3_lemma : w3_lemmas){
                          word.set(w1_lemmas + "," + w2_lemmas + "," + w3_lemmas);
                          context.write(word, location); 
                          }      
                          }                 
                    }
                }
                else if(lemmaMap.containsKey(tokens[i]) && lemmaMap.containsKey(tokens[j]) && !lemmaMap.containsKey(tokens[k])){
                    if(tokens[k].isEmpty())
                    continue;
                    w1_lemmas = lemmaMap.get(tokens[i]);
                    w2_lemmas = lemmaMap.get(tokens[j]);
                    for(String w1_lemma:w1_lemmas){
                        for(String w2_lemma:w2_lemmas){
                          word.set(w1_lemma+","+w2_lemma+","+tokens[k]);
                          context.write(word, location);
              } 
            }
          }
                else if(lemmaMap.containsKey(tokens[i]) && !lemmaMap.containsKey(tokens[j]) && lemmaMap.containsKey(tokens[k])){
                    if(tokens[j].isEmpty())
                    continue;
                    w1_lemmas = lemmaMap.get(tokens[i]);
                    w2_lemmas = lemmaMap.get(tokens[k]);
                    for(String w1_lemma:w1_lemmas){
                        for(String w2_lemma:w2_lemmas){
                          word.set(w1_lemma+","+tokens[j]+","+w2_lemma);
                          context.write(word, location);
              } 
            }
          }
                else if(!lemmaMap.containsKey(tokens[i]) && lemmaMap.containsKey(tokens[j]) && lemmaMap.containsKey(tokens[k])){
                    if(tokens[i].isEmpty())
                    continue;
                    w1_lemmas = lemmaMap.get(tokens[k]);
                    w2_lemmas = lemmaMap.get(tokens[j]);
                    for(String w1_lemma:w1_lemmas){
                        for(String w2_lemma:w2_lemmas){
                          word.set(tokens[i]+","+w2_lemma+","+w1_lemma);
                          context.write(word, location);
              } 
            }
          }
                else if(lemmaMap.containsKey(tokens[i]) && !lemmaMap.containsKey(tokens[j]) && !lemmaMap.containsKey(tokens[k])){
                    if(tokens[k].isEmpty())
                    continue;
                    if(tokens[j].isEmpty())
                    continue;
                    w1_lemmas = lemmaMap.get(tokens[i]);
                    for(String w1_lemma:w1_lemmas){
                      word.set(w1_lemma+","+tokens[j]+","+tokens[k]);
                          context.write(word, location);
              
            }
          }
                else if(!lemmaMap.containsKey(tokens[i]) && lemmaMap.containsKey(tokens[j]) && !lemmaMap.containsKey(tokens[k])){
                    if(tokens[k].isEmpty())
                    continue;
                    if(tokens[i].isEmpty())
                    continue;
                    w1_lemmas = lemmaMap.get(tokens[j]);
                    for(String w1_lemma:w1_lemmas){
                      word.set(tokens[i]+","+w1_lemma+","+tokens[k] );
                          context.write(word, location);
              
            }
          }
                else if(!lemmaMap.containsKey(tokens[i]) && !lemmaMap.containsKey(tokens[j]) && lemmaMap.containsKey(tokens[k])){
                    if(tokens[i].isEmpty())
                    continue;
                    if(tokens[j].isEmpty())
                    continue;
                    w1_lemmas = lemmaMap.get(tokens[k]);
                    for(String w1_lemma:w1_lemmas){
                      word.set(tokens[i]+","+tokens[j]+","+w1_lemma);
                          context.write(word, location);
              
            }
          }
                
                else {
                  if(tokens[i].isEmpty()) continue;
                  if(tokens[j].isEmpty()) continue;
                  if(tokens[k].isEmpty()) continue;
                    word.set(tokens[i] + "," + tokens[j] + "," + tokens[k]);
                    context.write(word,location);
                  
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
            LemmaCoocTri1.lemmaMap.put(word,lemmas);
        }
    Job job = Job.getInstance(conf, "Lemma");
    job.setJarByClass(LemmaCoocTri1.class);
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