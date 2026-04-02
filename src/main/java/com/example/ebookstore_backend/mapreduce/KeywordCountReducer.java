package com.example.ebookstore_backend.mapreduce;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * Reducer类：汇总每个关键词的出现次数
 * 输入：<关键词, [1, 1, 1, ...]>
 * 输出：<关键词, 总次数>
 */
public class KeywordCountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

    private IntWritable result = new IntWritable();

    /**
     * Reduce函数：对每个关键词的计数进行求和
     */
    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context)
            throws IOException, InterruptedException {
        
        int sum = 0;
        
        for (IntWritable val : values) {
            sum += val.get();
        }
        
        result.set(sum);
        
        // 输出关键词及其总出现次数
        context.write(key, result);
        
        // 记录日志
        System.out.println("Keyword: " + key.toString() + ", Count: " + sum);
    }
}