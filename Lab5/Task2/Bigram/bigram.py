import csv
import sys
from pyspark import SparkContext


lemmatizer = {}
with open('new_lemmatizer.csv', encoding="utf8") as csvfile:
    read = csv.reader(csvfile, delimiter=',')
    words = [[item for item in row if item != ''] for row in read] 
    for word in words:
        lemmatizer[word[0]] = word[1:]		


def lemmatize(line):
    try:
        splitStr = line.split(">")
        loc = splitStr[0]    
        tokens = []
        out = []
        empty = []
        tokens = (splitStr[1].split())
        
        tokens = [text.lower().replace("j","i").replace("v","u") for text in tokens]
        
        for i in range(len(tokens)-1):
            
            for lem1 in lemmatizer.get(tokens[i]):

            	for j in range(i+1,len(tokens)-1):
            
	                for lem2 in lemmatizer.get(tokens[j]):
	            
	                    out.append(lem1+" "+lem2 + loc+"."+str(i+1))
            
        return out
    except:
        
        return empty      						

if  __name__ =='__main__':
	sc = SparkContext(appName = "sparklem")
	text_file = sc.textFile(sys.argv[1])
	counts = text_file.flatMap(lemmatize) \
             .map(lambda word: (word.split("<")[0],word.split("<")[1])) \
             .reduceByKey(lambda a, b: a + "," + b).sortByKey().collect()
	#counts.saveAsTextFile("trial19")
	sc.parallelize(counts).saveAsTextFile(sys.argv[2])
