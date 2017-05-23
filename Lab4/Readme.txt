Data Intensive Computing
Lab4


WordCount

	1. Open Lab4Tweets.ipynb.
	2. Collect the tweets by using a Twitter API key.(Cell 1 in the Notebook).
	3. A text file named 'tweets.txt' is generated.
	4. Put file into hadoop file system.
	5. Run - hadoop jar wc.jar WordCount "inputpath" "outputpath"
	6. Get the output to local file system from the hadoop file system.
	7. Run the tagcloud cell in Lab4WordCloud.ipynb by placing the output.


Pairs & Stripes

	1. 	Open Lab4Tweets.ipynb.
	2.	A text file named 'tweets2.txt' is generated.
	3. 	Put file into hadoop file system.
	4. 	FOR PAIRS:
			1.	Run hadoop jar pairs.jar Pairs "inputpath" "outputpath"
			2. The part file can be retrived from hadoop file system to local file system.
	5.	FOR STRIPES:
			1. Run hadoop jar Stripes.jar Stripes "inputpath" "outputpath"
			2. The part file can be retrived from hadoop file system to local file system. 

Activity 1

	1. The tess files and csv file are present in the folder.
	2. Put the folder in the input folder of this activity in hdfs inputpath.
	3. Run hadoop jar Lemma.jar Lemma "tessfiles/" "outputpath"
	4. The obtained part file can be obtained from hdfs file system to local file system.

Activity 2
	
	1. The tess files and csv file are present in the folder.
	2. Put the folder in the input folder of this activity in hdfs inputpath.
	3. FOR BI-GRAMS:
			1. Run - hadoop jar bigrams.jar BiGrams "/tessfiles/" "outputpath"
			2. The part file can be retrived from hadoop file system to local file system.
	4. FOR TRI-GRAMS:
			1. Run - hadoop jar trigrams.jar TriGrams "/tessfiles/" "outputpath"
			2. The part file can be retrieved from hadoop file system to lcoal file system.
	5. Graph is available.
