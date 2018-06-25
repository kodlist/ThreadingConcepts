package com.forkjoinpool.examples;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by mkoduri on 6/20/2018.
 * example one on ForkJoinPool
 */

class FolderProcessor extends RecursiveTask<List<String>>{

    private final String path;
    private final String fileExtension;

    public FolderProcessor(String path, String fileExtension) {
        this.path = path;
        this.fileExtension = fileExtension;
    }

    @Override
    protected List<String> compute() {

        //===========List to store the names of the files stored in the folder.
        List<String> list = new ArrayList<>();
        //===========List of FolderProcessor tasks that are going to store sub tasks, it is nothing but sub folders stored in parent folder.
        List<FolderProcessor> tasks = new ArrayList<>();

        //Get the list of files and folders in current folder
        File file = new File(path);
        File[] content = file.listFiles();

        //For each sub folder we create new FolderProcessor task to process and that should run in parallel using work stealing algorithm.
        //As when the fork starts it will trigger compute() method. Then process repeats in recursive model.
        if(content != null)
        {
            for(int i = 0; i < content.length ; i++)
            {
                if(content[i].isDirectory())
                {
                    FolderProcessor task = new FolderProcessor(content[i].getAbsolutePath(), fileExtension);
                    // executing fork method. this will send task to the thread pool.
                    // fork() will trigger compute () method.
                    //Arranges to asynchronously execute this task in the pool (please see line 83) the current task is running in,
                    // if applicable, or using the ForkJoinPool.commonPool() if not inForkJoinPool()
                    task.fork();
                    //add task to a list and finally you can iterate each task and call join method. here we will call join in addResultsFromTask()
                    tasks.add(task);
                }else{
                    if(checkFileExtension(content[i].getName()))
                    {
                        list.add(content[i].getAbsolutePath());
                    }
                }
            }
        }
        addResultsFromTask(list, tasks);

        return list;
    }

    public void addResultsFromTask(List<String> list, List<FolderProcessor> tasks)
    {
        for(FolderProcessor item : tasks)
        {
            list.addAll(item.join());
            // join method call will wait for its finalization and then will return the result of the task. And add the result to
            // the list of Strings (addAll takes collection of List<String>.
        }
    }


    public boolean checkFileExtension(String fileName)
    {
        return fileName.endsWith(fileExtension);
    }
}

public class ForkJoinPoolClass {

    public static void main(String[] args) {
        System.out.println("Thread name : "+Thread.currentThread().getName());

        //ForkJoinPool pool = new ForkJoinPool();
        ForkJoinPool pool = new ForkJoinPool(2); //Creates a ForkJoinPool with parallelism equal to Runtime.availableProcessors() or
        // number given in argument. in here 2 core's will be used.

        FolderProcessor f1 = new FolderProcessor("C:\\Users\\mkoduri\\apache-maven-3.5.3\\lib", "license");
        FolderProcessor f2 = new FolderProcessor("C:\\Users\\mkoduri\\Java 8 Exam\\parent","log.txt");
        FolderProcessor f3 = new FolderProcessor("C:\\Users\\mkoduri\\Downloads\\apache-maven-3.5.3-bin\\apache-maven-3.5.3\\lib","jar");
        // execute the two tasks
        pool.execute(f1);
        pool.execute(f2);
        pool.execute(f3);


        //Write to the console information about the status of the pool every second
        //until the three tasks have finished their execution.
        do
        {
            System.out.printf("******************************************\n");
            System.out.printf("Main: Parallelism: %d\n", pool.getParallelism());
            System.out.printf("Main: Active Threads: %d\n", pool.getActiveThreadCount());
            System.out.printf("Main: Task Count: %d\n", pool.getQueuedTaskCount());
            System.out.printf("Main: Steal Count: %d\n", pool.getStealCount());
            System.out.println("Thread name : "+Thread.currentThread().getName());
            System.out.printf("******************************************\n");
            try
            {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        } while ( (!f1.isDone()) || (!f2.isDone())  || (!f3.isDone()) );

        pool.shutdown();

        List<String> results;
        results = f1.join();
        System.out.printf("f1: %d files with ext license found. \n ", results.size());
        results = f2.join();
        System.out.printf("f2: %d files with log.txt found. \n ", results.size());
        results = f3.join();
        System.out.printf("f3: %d files with jar found. \n", results.size());

    }
}
