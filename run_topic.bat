bin\mallet import-dir --input sample-data\web\en --output tutorial.mallet --keep-sequence --remove-stopwords
bin\mallet train-topics --input tutorial.mallet --num-topics 20 --output-doc-topics tutorial_compostion.txt

bin\mallet train-topics --input tutorial.mallet --num-topics 20 --optimize-interval 20 --output-state topic-state.gz --output-topic-keys tutorial_key.txt --output-doc-topics tutorial_compostion.txt --topic-word-weights-file tutorial_topic_word_weight.txt


bin\mallet import-dir --input sample-data\web\an --output autorial.mallet --keep-sequence --remove-stopwords

bin\mallet train-topics --input autorial.mallet --num-topics 20 --optimize-interval 20 --output-doc-topics autorial_compostion.txt
