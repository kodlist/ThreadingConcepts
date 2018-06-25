package com.forkjoinpool.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;


/**
 * Created by mkoduri on 6/21/2018.
 *
 * Counting a specific word in a file. A folder might have sub folders with files in.
 *
 * we traverse all subfolders and files in it and count the word we are looking for.
 *
 * The program as for join pool constructor which will assume or take the default cores of the system you are running.
 *
 * There is a repeatCount variable in this program. Based initialization it runs that many times.
 *
 * couple of classes exist in this file.  Document, Folder, WordCounter, DocumentSearchTask,  FolderSearchTask
 *
 */

class Document {
    private final List<String> lines;

    Document(List<String> lines) {
        this.lines = lines;
    }

    List<String> getLines() {
        return this.lines;
    }

    static Document fromFile(File file) throws IOException {
        List<String> lines = new LinkedList<>();

        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        }

        return new Document(lines);
    }
}

class Folder {
    private final List<Folder> subFolders;
    private final List<Document> documents;

    Folder(List<Folder> subFolders, List<Document> documents) {
        this.subFolders = subFolders;
        this.documents = documents;
    }

    List<Folder> getSubFolders() {
        return this.subFolders;
    }

    List<Document> getDocuments() {
        return this.documents;
    }

    static Folder fromDirectory(File dir) throws IOException {
        List<Document> documents = new LinkedList<>();
        List<Folder> subFolders = new LinkedList<>();

        for (File entry : dir.listFiles()) {
            if (entry.isDirectory()) {
                subFolders.add(Folder.fromDirectory(entry));
            } else {
                documents.add(Document.fromFile(entry));
            }
        }
        return new Folder(subFolders, documents); //the constructor has list of folders and documents.
    }
}

class WordCounter {

    /*String[] wordsIn(String line) {
        return line.trim().split("(\\s|\\p{Punct})+");
    }*/
    long count = 0;

    public String[] wordsIn(String line)
    {
        return line.trim().split(" ");

    }
    public  Long occurrencesCount(Document document, String searchedWord) {
       // long count = 0;
        for (String line : document.getLines()) {

            /*for ( String s: wordsIn(line)  ) {

               // System.out.println(s +" ");
                if (searchedWord.equals(s)) {
                    count = count + 1;
                    System.out.println(s);
                }

            }*/
            for (String word : wordsIn(line)) {
                if (searchedWord.equals(word)) {
                    System.out.println(word);
                    count = count + 1;
                }
            }
        }
        return count;
    }



}


class DocumentSearchTask extends RecursiveTask<Long> {

    private final Document document;
    private final String searchedWord;
    WordCounter wordCounter = new WordCounter();

    DocumentSearchTask(Document document, String searchedWord) {
        super();
        this.document = document;
        this.searchedWord = searchedWord;
    }

    @Override
    protected Long compute() {
        return wordCounter.occurrencesCount(document, searchedWord);
    }
}


class FolderSearchTask extends RecursiveTask<Long> {
    private final Folder folder;
    private final String searchedWord;

    FolderSearchTask(Folder folder, String searchedWord) {
        super();
        this.folder = folder;
        this.searchedWord = searchedWord;
    }

    @Override
    protected Long compute() {
        long count = 0L;
        List<RecursiveTask<Long>> forks = new LinkedList<>();
        System.out.println("  task for folder : "+folder.getSubFolders().size());

        for (Folder subFolder : folder.getSubFolders()) {
            FolderSearchTask task = new FolderSearchTask(subFolder, searchedWord);

            forks.add(task);
            task.fork();

        }
        System.out.println(" documents for search : "+ folder.getDocuments().size());

        for (Document document : folder.getDocuments()) {
            DocumentSearchTask task = new DocumentSearchTask(document, searchedWord);
            forks.add(task);
            task.fork();

        }
        for (RecursiveTask<Long> task : forks) {
            count = count + task.join();
        }
        return count;
    }
}


public class CountingAWord {

    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    Long countOccurrencesInParallel(Folder folder, String searchedWord) {
        return forkJoinPool.invoke(new FolderSearchTask(folder, searchedWord));
    }

    public static void main(String[] args) throws IOException{

        WordCounter wordCounter = new WordCounter();
        CountingAWord cWord = new CountingAWord();
        Folder folder = Folder.fromDirectory(new File("C:\\Users\\mkoduri\\Documents\\parent"));

        final int repeatCount = 2;  //You run program for 2 times and see the difference. in the bottom program runs for 2 loops.
        long counts;
        long startTime;
        long stopTime;

        //long[] singleThreadTimes = new long[repeatCount];
        long[] forkedThreadTimes = new long[repeatCount];


        for (int i = 0; i < repeatCount; i++) {
            startTime = System.currentTimeMillis();
            counts = cWord.countOccurrencesInParallel( folder , "back");
            stopTime = System.currentTimeMillis();
            forkedThreadTimes[i] = (stopTime - startTime);
            System.out.println(counts + " , fork / join search took " + forkedThreadTimes[i] + "ms");
        }

    }
}
