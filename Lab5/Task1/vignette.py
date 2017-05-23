import pyspark  
from pyspark.mllib.regression import LabeledPoint
from pyspark.mllib.classification import LogisticRegressionWithSGD
from pyspark.mllib.tree import DecisionTree

sc = pyspark.SparkContext()
raw_rdd = sc.textFile("titanic.csv")
raw_rdd.count()
raw_rdd.take(5)
header = raw_rdd.first()
data_rdd = raw_rdd.filter(lambda line: line != header)
data_rdd.takeSample(False, 5, 0)
def row_to_labeled_point(line):
    '''
    Builds a LabelPoint consisting of:
    
    survival (truth): 0=no, 1=yes
    ticket class: 0=1st class, 1=2nd class, 2=3rd class
    age group: 0=child, 1=adults
    gender: 0=man, 1=woman
    '''
    passenger_id, klass, age, sex, survived = [segs.strip('"') for segs in line.split(',')]
    klass = int(klass[0]) - 1
    
    if (age not in ['adults', 'child'] or 
        sex not in ['man', 'women'] or
        survived not in ['yes', 'no']):
        raise RuntimeError('unknown value')
    
    features = [
        klass,
        (1 if age == 'adults' else 0),
        (1 if sex == 'women' else 0)
    ]
    return LabeledPoint(1 if survived == 'yes' else 0, features)
labeled_points_rdd = data_rdd.map(row_to_labeled_point)
labeled_points_rdd.takeSample(False, 5, 0)
training_rdd, test_rdd = labeled_points_rdd.randomSplit([0.7, 0.3], seed = 0)
training_count = training_rdd.count()
test_count = test_rdd.count()
training_count, test_count
model = DecisionTree.trainClassifier(training_rdd, 
                                     numClasses=2, 
                                     categoricalFeaturesInfo={
                                        0: 3,
                                        1: 2,
                                        2: 2
                                     })
predictions_rdd = model.predict(test_rdd.map(lambda x: x.features))
truth_and_predictions_rdd = test_rdd.map(lambda lp: lp.label).zip(predictions_rdd)
accuracy = truth_and_predictions_rdd.filter(lambda v_p: v_p[0] == v_p[1]).count() / float(test_count)
print('Accuracy =', accuracy)
print(model.toDebugString())
model = LogisticRegressionWithSGD.train(training_rdd)
predictions_rdd = model.predict(test_rdd.map(lambda x: x.features))
labels_and_predictions_rdd = test_rdd.map(lambda lp: lp.label).zip(predictions_rdd)
accuracy = labels_and_predictions_rdd.filter(lambda v_p: v_p[0] == v_p[1]).count() / float(test_count)
print('Accuracy =', accuracy)                                         