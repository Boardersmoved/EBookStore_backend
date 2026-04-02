package com.example.ebookstore_backend.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * MapReduce Driver类：配置和运行关键词统计作业
 */
public class KeywordCountDriver extends Configured implements Tool {

    @Override
    public int run(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: KeywordCountDriver <input path> <output path> [keyword file path]");
            return -1;
        }

        Configuration conf = getConf();
        
        // 如果提供了关键词文件路径，添加到配置中
        if (args.length >= 3) {
            conf.set("keyword.file.path", args[2]);
        }

        // 创建Job实例
        Job job = Job.getInstance(conf, "Keyword Count");
        job.setJarByClass(KeywordCountDriver.class);

        // 设置Mapper和Reducer类
        job.setMapperClass(KeywordCountMapper.class);
        job.setCombinerClass(KeywordCountReducer.class); // 使用Combiner优化性能
        job.setReducerClass(KeywordCountReducer.class);

        // 设置输出键值类型
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        // 设置输入输出路径
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // 提交作业并等待完成
        boolean success = job.waitForCompletion(true);
        return success ? 0 : 1;
    }

    /**
     * 主方法：程序入口
     */
    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new Configuration(), new KeywordCountDriver(), args);
        System.exit(exitCode);
    }

    /**
     * 编程方式运行MapReduce作业
     * @param inputPath 输入路径
     * @param outputPath 输出路径
     * @param keywordFilePath 关键词文件路径
     * @return 作业是否成功
     */
    public static boolean runJob(String inputPath, String outputPath, String keywordFilePath) throws Exception {
        Configuration conf = new Configuration();
        
        // Windows环境下的Hadoop配置 - 使用环境变量HADOOP_HOME
        String hadoopHome = System.getenv("HADOOP_HOME");
        if (hadoopHome != null && !hadoopHome.isEmpty()) {
            System.setProperty("hadoop.home.dir", hadoopHome);
            System.out.println("使用HADOOP_HOME: " + hadoopHome);
        } else {
            System.err.println("警告: 未设置HADOOP_HOME环境变量，可能导致错误");
        }
        
        // 禁用Hadoop的安全管理器检查（解决Java 17+兼容性问题）
        System.setProperty("HADOOP_USER_NAME", "hadoop");
        conf.set("hadoop.security.authentication", "simple");
        conf.set("hadoop.security.authorization", "false");
        
        // 设置为本地模式运行
        conf.set("mapreduce.framework.name", "local");
        conf.set("fs.defaultFS", "file:///");
        
        // 禁用Windows原生IO（解决hadoop.dll问题）
        conf.set("io.native.lib.available", "false");
        conf.setBoolean("dfs.client.use.datanode.hostname", true);
        
        // 禁用不必要的安全检查
        conf.set("ipc.client.fallback-to-simple-auth-allowed", "true");
        
        if (keywordFilePath != null && !keywordFilePath.isEmpty()) {
            conf.set("keyword.file.path", keywordFilePath);
        }

        Job job = Job.getInstance(conf, "Keyword Count Job");
        job.setJarByClass(KeywordCountDriver.class);

        job.setMapperClass(KeywordCountMapper.class);
        job.setCombinerClass(KeywordCountReducer.class);
        job.setReducerClass(KeywordCountReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        return job.waitForCompletion(true);
    }
}