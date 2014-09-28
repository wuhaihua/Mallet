rem CHN sports

bin\mallet import-dir --input sample-data\db_chn_20140922_0924_jieba\Sports_chn --output Sports_chn_jieba_agg.mallet --keep-sequence --stoplist-file stoplists\chn.txt --extra-stopwords stoplists\sports_chn.txt
bin\mallet train-topics --input Sports_chn_jieba_agg.mallet --num-topics 200 --optimize-interval 10 --output-doc-topics Sports_chn_jieba_compostion.txt --output-topic-keys Sports_chn_topic_jieba_keys.txt --output-global-keys Sports_chn_global_jieba_keys.txt
bin\mallet train-clusters --input Sports_chn_jieba_agg.mallet --num-topics 200 --optimize-interval 10 --k 400 --output-cluster-results cluster_results_Sports_chn_jieba_Cluster_N200K400_2792.txt

bin\mallet import-dir --input sample-data\db_chn_20140922_0924_fnlp\Sports_chn --output Sports_chn_fnlp_agg.mallet --keep-sequence --stoplist-file stoplists\chn.txt --extra-stopwords stoplists\sports_chn.txt
bin\mallet train-topics --input Sports_chn_fnlp_agg.mallet --num-topics 200 --optimize-interval 10 --output-doc-topics Sports_chn_fnlp_compostion.txt --output-topic-keys Sports_chn_topic_fnlp_keys.txt --output-global-keys Sports_chn_global_fnlp_keys.txt
bin\mallet train-clusters --input Sports_chn_fnlp_agg.mallet --num-topics 200 --optimize-interval 10 --k 400 --output-cluster-results cluster_results_Sports_chn_fnlp_Cluster_N200K400_2792.txt

bin\mallet train-topics --input Sports_chn_agg.mallet --num-topics 200 --optimize-interval 20 --output-doc-topics Sports_chn_compostion.txt --output-topic-keys Sports_chn_topic_keys.txt --output-global-keys Sports_chn_global_keys.txt
bin\mallet train-topics --input Sports_chn_agg.mallet --num-topics 80 --optimize-interval 20 --output-doc-topics Sports_chn_compostion.txt --output-topic-keys Sports_chn_topic_keys.txt --output-global-keys Sports_chn_global_keys.txt
bin\mallet train-topics --input Sports_chn_agg.mallet --num-topics 150 --optimize-interval 10 --output-doc-topics Sports_chn_compostion.txt --output-topic-keys Sports_chn_topic_keys.txt --output-global-keys Sports_chn_global_keys.txt
bin\mallet train-clusters --input Sports_chn_agg.mallet --num-topics 200 --optimize-interval 20 --k 400 --output-cluster-results cluster_results_Sports_chn_Cluster_N200K400_1635.txt
bin\mallet train-clusters --input Sports_chn_agg.mallet --num-topics 80 --optimize-interval 20 --k 170 --output-cluster-results cluster_results_Sports_chn_Cluster_N80K170_727.txt


rem CHN news

bin\mallet import-dir --input sample-data\db_chn_20140922_0924\News_chn --output News_chn_agg.mallet --keep-sequence --stoplist-file stoplists\chn.txt --extra-stopwords stoplists\news_chn.txt
bin\mallet train-topics --input News_chn_agg.mallet --num-topics 200 --optimize-interval 20 --output-doc-topics News_chn_compostion.txt --output-topic-keys News_chn_topic_keys.txt --output-global-keys News_chn_global_keys.txt

rem Military news

bin\mallet import-dir --input sample-data\db_chn_20140922_0924_jieba\Military_chn --output Military_chn_jieba_agg.mallet --keep-sequence --stoplist-file stoplists\chn.txt --extra-stopwords stoplists\military_chn.txt
bin\mallet train-topics --input Military_chn_jieba_agg.mallet --num-topics 80 --optimize-interval 10 --output-doc-topics Military_chn_jieba_compostion.txt --output-topic-keys Military_chn_topic_jieba_keys.txt --output-global-keys Military_chn_global_jieba_keys.txt
bin\mallet train-clusters --input Military_chn_jieba_agg.mallet --num-topics 80 --optimize-interval 10 --k 100 --output-cluster-results cluster_results_Military_chn_jieba_Cluster_N80K100_303.txt