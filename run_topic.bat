bin\mallet import-dir --input sample-data\web\en --output tutorial.mallet --keep-sequence --remove-stopwords
bin\mallet train-topics --input tutorial.mallet --num-topics 20 --output-doc-topics tutorial_compostion.txt

bin\mallet train-topics --input tutorial.mallet --num-topics 20 --optimize-interval 20 --output-state topic-state.gz --output-topic-keys tutorial_key.txt --output-doc-topics tutorial_compostion.txt --topic-word-weights-file tutorial_topic_word_weight.txt


bin\mallet import-dir --input sample-data\web\an --output autorial.mallet --keep-sequence --remove-stopwords

bin\mallet train-topics --input autorial.mallet --num-topics 20 --optimize-interval 20 --output-doc-topics autorial_compostion.txt


Vectors2Cluster  arguments:
--input C:\Mallet\autorial.mallet --num-topics 20 --optimize-interval 20 --k 12 --output-doc-topics add_compostion.txt



rem Yahoo Digest collected data testing as Unit test

bin\mallet import-dir --input sample-data\web\YahooDigest\aggregate --output Digest_agg.mallet --keep-sequence --remove-stopwords

bin\mallet train-topics --input Digest_agg.mallet --num-topics 30 --optimize-interval 30 --output-doc-topics Digest_compostion.txt


rem Sports
bin\mallet import-dir --input sample-data\db_20140901_0903\Sports --output Sports_agg.mallet --keep-sequence --remove-stopwords

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