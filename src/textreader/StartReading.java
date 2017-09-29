/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package textreader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StartReading  { 

    private final ConcurrentHashMap<Integer, Long> map;

    private final AtomicInteger counter = new AtomicInteger(1);
    
    private static final Logger LOG = Logger.getLogger(StartReading.class.getName());


    public StartReading() {
        this.map = new ConcurrentHashMap<>();
    }

    public void readFile(String uriPath) throws IOException {
        
        ExecutorService es = Executors.newFixedThreadPool(5);

        try (Stream<String> streamTextFiles = Files.lines(Paths.get(uriPath))) {
            streamTextFiles.forEach((row) -> {
                es.submit(() -> {
                    map.put(counter.getAndIncrement(), Pattern.compile("[\\P{L}]+").splitAsStream(row).count());
                });
            });

        }
        es.shutdown();
        try {
            es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            
        }
    }

    public void sortByRowNumber() {
        Map<Integer, Long> result = map.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        printResult(result);
    }

    public void sortByCounter() {
        Map<Integer, Long> result = map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        printResult(result);
    }

    private void printResult(Map<Integer, Long> mapToPrint) {
        mapToPrint.forEach((key, value) -> {
//            System.out.printf("%d - %d;\n", value, key);
        LOG.log(Level.INFO, "{0} - {1}", new Object[]{value, key});

        });
    }

    public static void main(String[] args) throws InterruptedException {
        try {
            
            StartReading sr = new StartReading();
            
            sr.readFile(args[0]);
            
            sr.sortByRowNumber();
            
            LOG.log(Level.INFO,"\n------------------------------------\n");
            
            sr.sortByCounter();
            
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex.getMessage());
        }
    }

}
