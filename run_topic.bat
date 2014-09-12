bin\mallet import-dir --input sample-data\web\en --output tutorial.mallet --keep-sequence --remove-stopwords
bin\mallet train-topics --input tutorial.mallet --num-topics 20 --output-doc-topics tutorial_compostion.txt --output-topic-keys tutorial_topic_keys.txt --output-global-keys tutorial_global_keys.txt

bin\mallet train-topics --input tutorial.mallet --num-topics 20 --optimize-interval 11 --output-state topic-state.gz --output-topic-keys tutorial_key.txt --output-doc-topics tutorial_compostion.txt --topic-word-weights-file tutorial_topic_word_weight.txt


bin\mallet import-dir --input sample-data\web\an --output autorial.mallet --keep-sequence --remove-stopwords

bin\mallet import-dir --input sample-data\web\an --output autorial.mallet --keep-sequence --remove-stopwords --extra-stopwords stoplists\sports.txt

bin\mallet train-topics --input autorial.mallet --num-topics 20 --optimize-interval 20 --output-doc-topics autorial_compostion.txt --output-topic-keys autorial_topic_keys.txt

bin\mallet train-topics --input autorial.mallet --num-topics 20 --optimize-interval 20 --output-doc-topics autorial_compostion.txt --output-topic-keys autorial_topic_keys.txt --output-global-keys autorial_global_keys.txt

bin\mallet train-topics --input CHN.mallet --num-topics 3 --optimize-interval 10 --output-doc-topics CHN_compostion.txt --output-topic-keys CHN_topic_keys.txt --output-global-keys CHN_global_keys.txt

bin\mallet train-clusters --input CHN.mallet --num-topics 3 --optimize-interval 10 --k 2 >> Output_CHN_Cluster_N3K2.txt

Vectors2Cluster  arguments:
--input C:\Mallet\autorial.mallet --num-topics 20 --optimize-interval 20 --k 12 --output-doc-topics add_compostion.txt



rem Yahoo Digest collected data testing as Unit test

bin\mallet import-dir --input sample-data\web\YahooDigest\aggregate --output Digest_agg.mallet --keep-sequence --remove-stopwords

bin\mallet train-topics --input Digest_agg.mallet --num-topics 30 --optimize-interval 30 --output-doc-topics Digest_compostion.txt


rem Sports
bin\mallet import-dir --input sample-data\db_20140901_0903\Sports --output Sports_agg.mallet --keep-sequence --remove-stopwords

bin\mallet import-dir --input sample-data\db_20140901_0903\Sports --output Sports_agg.mallet --keep-sequence --remove-stopwords --extra-stopwords stoplists\sports.txt

bin\mallet train-clusters --input Sports_agg.mallet --num-topics 150 --optimize-interval 20 --k 300 >> Output_Sports_Cluster_N150K300_1438.txt

bin\mallet train-topics --input Sports_agg.mallet --num-topics 150 --optimize-interval 20 --output-doc-topics Sports_compostion_1438.txt --output-topic-keys Sports_topic_keys_1438.txt



Vectors2Cluster  arguments:
--input C:\Mallet\Digest_agg.mallet --num-topics 30 --optimize-interval 30 --k 11 --output-doc-topics Digest_compostion.txt


rem News
bin\mallet import-dir --input sample-data\db_20140901_0903\News --output News_agg.mallet --keep-sequence --remove-stopwords

bin\mallet train-clusters --input News_agg.mallet --num-topics 50 --optimize-interval 20 --k 250 >> Output_News_Cluster_N70K140_570.txt

bin\mallet train-topics --input News_agg.mallet --num-topics 50 --optimize-interval 20 --output-doc-topics News_compostion_570.txt --output-topic-keys News_topic_keys_570.txt

rem Technology   NO OP
bin\mallet import-dir --input sample-data\db_20140901_0903\Technology --output Technology_agg.mallet --keep-sequence --remove-stopwords

bin\mallet train-clusters --input Technology_agg.mallet --num-topics 50 --optimize-interval 20 --k 250 >> Output_Technology_Cluster_N50K250_579.txt

bin\mallet train-topics --input Technology_agg.mallet --num-topics 50 --optimize-interval 20 --output-doc-topics Technology_compostion_579.txt --output-topic-keys Technology_topic_keys_579.txt

rem Finance
bin\mallet import-dir --input sample-data\db_20140901_0903\Finance --output Finance_agg.mallet --keep-sequence --remove-stopwords

bin\mallet train-clusters --input Finance_agg.mallet --num-topics 50 --optimize-interval 20 --k 150 >> Output_Finance_Cluster_N50K150_241.txt

bin\mallet train-topics --input Finance_agg.mallet --num-topics 50 --optimize-interval 20 --output-doc-topics Finance_compostion_241.txt --output-topic-keys Finance_topic_keys_241.txt

rem Politics
bin\mallet import-dir --input sample-data\db_20140901_0903\Politics --output Politics_agg.mallet --keep-sequence --remove-stopwords

bin\mallet train-clusters --input Politics_agg.mallet --num-topics 80 --optimize-interval 20 --k 80 >> Output_Politics_Cluster_N80K80_176.txt

bin\mallet train-topics --input Politics_agg.mallet --num-topics 80 --optimize-interval 20 --output-doc-topics Politics_compostion_176.txt --output-topic-keys Politics_topic_keys_176.txt

rem Science
bin\mallet import-dir --input sample-data\db_20140901_0903\Science --output Science_agg.mallet --keep-sequence --remove-stopwords

bin\mallet train-clusters --input Science_agg.mallet --num-topics 80 --optimize-interval 20 --k 80 >> Output_Science_Cluster_N80K80_176.txt

bin\mallet train-topics --input Science_agg.mallet --num-topics 80 --optimize-interval 20 --output-doc-topics Science_compostion_176.txt --output-topic-keys Science_topic_keys_176.txt

rem Entertainment
bin\mallet import-dir --input sample-data\db_20140901_0903\Entertainment --output Entertainment_agg.mallet --keep-sequence --stoplist-file stoplists\en.txt --extra-stopwords stoplists\entertainment.txt

bin\mallet train-clusters --input Entertainment_agg.mallet --num-topics 100 --optimize-interval 20 --k 165 >> Output_Entertainment_Cluster_N100K165_259.txt

bin\mallet train-topics --input Entertainment_agg.mallet --num-topics 100 --optimize-interval 20 --output-doc-topics Entertainment_compostion_259.txt --output-topic-keys Entertainment_topic_keys_259.txt --output-global-keys Entertainment_global_keys.txt


\fnlp-core\target\classes\org\fnlp\nlp\cn\tag


java -Xmx1024m -classpath "fnlp-core/target/fnlp-core-2.0-SNAPSHOT.jar:libs/trove4j-3.0.3.jar:libs/commons-cli-1.2.jar" fnlp-core/target/classes/org.fnlp.nlp.cn.tag.POSTagger -s models/seg.m models/pos.m "周杰伦出生于台湾，生日为79年1月18日，他曾经的绯闻女友是蔡依林。"
java -Xmx1024m -classpath fnlp-core/target/fnlp-core-2.0-SNAPSHOT.jar;libs/trove4j-3.0.3.jar;libs/commons-cli-1.2.jar org.fnlp.nlp.cn.tag.POSTagger -s models/seg.m models/pos.m "周杰伦出生于台湾，生日为79年1月18日，他曾经的绯闻女友是蔡依林。"

java -Xmx1024m org.fnlp.nlp.cn.tag.POSTagger -s models/seg.m models/pos.m "周杰伦出生于台湾，生日为79年1月18日，他曾经的绯闻女友是蔡依林。"


java -Xmx1024m -classpath "C:\git_fnlp\fnlp\fnlp-core\target\fnlp-core-2.0-SNAPSHOT.jar;C:\git_fnlp\fnlp\lib\trove4j-3.0.3.jar;C:\git_fnlp\fnlp\lib\commons-cli-1.2.jar;C:\git_fnlp\fnlp\fnlp-core\target\classes" org.fnlp.nlp.cn.tag.CWSTagger -s C:\git_fnlp\fnlp\models\seg.m -f zhou.txt zhou_split.txt





