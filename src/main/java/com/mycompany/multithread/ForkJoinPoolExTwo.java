package com.mycompany.multithread;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

/**
 * Created by mkoduri on 6/21/2018.
 * example two on ForkJoinPool
 *
 */


class FolderProcessorTwo extends RecursiveTask<List<String>> {

    private final String path;
    private final String fileExtension;

    public FolderProcessorTwo(String path, String fileExtension) {
        this.path = path;
        this.fileExtension = fileExtension;
    }

    @Override
    protected List<String> compute() {

        //===========List to store the names of the files stored in the folder.
        List<String> list = new ArrayList<>();
        //===========List of FolderProcessor tasks that are going to store sub tasks, it is nothing but sub folders stored in parent folder.
        List<FolderProcessorTwo> tasks = new ArrayList<>();

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
                    FolderProcessorTwo task = new FolderProcessorTwo(content[i].getAbsolutePath(), fileExtension);
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
        //addResultsFromTask(list, tasks);

        return list;
    }

    public void addResultsFromTask(List<String> list, List<FolderProcessorTwo> tasks)
    {
        for(FolderProcessorTwo item : tasks)
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


public class ForkJoinPoolExTwo {


}
