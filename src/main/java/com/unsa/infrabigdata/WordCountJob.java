package com.unsa.infrabigdata;

import java.io.IOException;
import java.util.Locale;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/** WordCount propio para Hadoop MapReduce 3.4.2. */
public final class WordCountJob {

    private WordCountJob() {
    }

    public static class FruitMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

        private static final IntWritable ONE = new IntWritable(1);
        private final Text outputWord = new Text();

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {

            String normalized = value.toString()
                    .toLowerCase(Locale.ROOT)
                    .replaceAll("[^\\p{L}\\p{N}]+", " ")
                    .trim();

            if (normalized.isEmpty()) {
                return;
            }

            for (String word : normalized.split("\\s+")) {
                outputWord.set(word);
                context.write(outputWord, ONE);
            }
        }
    }

    public static class SumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

        private final IntWritable outputTotal = new IntWritable();

        @Override
        protected void reduce(Text word, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {

            int total = 0;
            for (IntWritable value : values) {
                total += value.get();
            }
            outputTotal.set(total);
            context.write(word, outputTotal);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2 || args.length > 3
                || (args.length == 3 && !"true".equalsIgnoreCase(args[2])
                    && !"false".equalsIgnoreCase(args[2]))) {
            System.err.println("Uso: WordCountJob <entrada> <salida> [combiner: true|false]");
            System.exit(2);
        }

        boolean useCombiner = args.length == 2 || Boolean.parseBoolean(args[2]);
        Configuration configuration = new Configuration();
        Job job = Job.getInstance(configuration,
                useCombiner ? "my-wordcount-con-combiner" : "my-wordcount-sin-combiner");

        job.setJarByClass(WordCountJob.class);
        job.setMapperClass(FruitMapper.class);
        if (useCombiner) {
            job.setCombinerClass(SumReducer.class);
        }
        job.setReducerClass(SumReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
