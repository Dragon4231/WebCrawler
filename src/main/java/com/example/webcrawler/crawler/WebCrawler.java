package com.example.webcrawler.crawler;

import au.com.bytecode.opencsv.CSVWriter;
import com.example.webcrawler.data.Message;
import com.example.webcrawler.data.Pair;
import com.example.webcrawler.data.Statistics;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebCrawler {

    int maxLink;
    String generatePath;
    String generatePathSort;
    final int threadCount = 40;

    TreeSet<String> linkedList = new TreeSet<>();

    ArrayDeque<Pair<String, Integer>> links = new ArrayDeque<>();

    ArrayList<String> forCompare = new ArrayList<>();

    ArrayList<String[]> records = new ArrayList<>();

    public void processingLink(Message message) throws IOException, InterruptedException {
        links.add(new Pair<>(message.getUrlSeed(), 0));
        linkedList.add(message.getUrlSeed());
        maxLink = message.getMaxLinks();
        forCompare = message.getForCompare();
        Document document;
        Elements elements;
        while (linkedList.size() < maxLink && !links.isEmpty()) {
            Pair<String, Integer> newLink = links.pop();
            if (newLink.second > 8) break;
            document = Jsoup.connect(newLink.first).get();
            elements = document.select("a[href]");
            for (Element element : elements) {
                if (linkedList.size() >= maxLink) break;
                links.add(new Pair<>(element.absUrl("href"), (newLink.second + 1)));
                linkedList.add(element.absUrl("href"));
            }
        }
        List<Future<?>> futures = new ArrayList<Future<?>>();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            Future<?> f = executorService.submit(new StatisticThread());
            futures.add(f);
        }
        while (true) {
            boolean isDone = true;
            for (Future<?> f : futures) {
                if (!f.isDone()) isDone = false;
            }
            if (isDone) break;
            Thread.sleep(1000);
        }
        executorService.shutdown();
        saveToCsv();
    }

    synchronized public int getLinkCount() {
        return linkedList.size();
    }

    synchronized public String getFirstLink() {
        if (linkedList.isEmpty()) return null;
        else return linkedList.pollFirst();
    }

    public void saveToCsv() throws IOException {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        generatePath = new String(gregorianCalendar.get(Calendar.HOUR) + "." +
                gregorianCalendar.get(Calendar.MINUTE) + "." + gregorianCalendar.get(Calendar.SECOND) + ".csv");
        generatePathSort = new String(gregorianCalendar.get(Calendar.HOUR) + "." +
                gregorianCalendar.get(Calendar.MINUTE) + "." + gregorianCalendar.get(Calendar.SECOND) + "(sortTotal)" + ".csv");
        ArrayList<String> row = new ArrayList<>();
        row.add("Link");
        for (int i = 0; i < forCompare.size(); i++) {
            row.add(forCompare.get(i));
        }
        row.add("Total");
        CSVWriter csvWriter = new CSVWriter(new FileWriter(generatePath));
        csvWriter.writeNext(row.toArray(new String[row.size()]));
        for (String[] str : records) {
            csvWriter.writeNext(str);
        }
        csvWriter.close();

        csvWriter = new CSVWriter(new FileWriter(generatePathSort));
        records.sort(new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                if (Integer.valueOf(o1[o1.length - 1]) < Integer.valueOf(o2[o2.length - 1])) return 1;
                else if(Integer.valueOf(o1[o1.length - 1]) > Integer.valueOf(o2[o2.length - 1])) return -1;
                return 0;
            }
        });
        csvWriter.writeNext(row.toArray(new String[row.size()]));
        for (String[] str : records) {
            csvWriter.writeNext(str);
        }
        csvWriter.close();
    }

    class StatisticThread implements Runnable {
        @Override
        public void run() {
            String tempUrl;
            while (!linkedList.isEmpty()) {
                tempUrl = getFirstLink();
                if (tempUrl != null) {
                    try {
                        Document document = Jsoup.connect(tempUrl).get();
                        processingTextFromLink(tempUrl, document.text());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            Thread.currentThread().interrupt();
        }
    }

    public Statistics processingTextFromLink(String link, String text) {
        Statistics statistics = new Statistics();
        ArrayList<Pair<String, Integer>> pairs = new ArrayList<>();
        statistics.setLink(link);
        int allCount = 0;
        for (String compareString : forCompare) {
            int count = 0;
            Pattern pattern = Pattern.compile("(\\w*)" + compareString + "(\\w*)");
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) count++;
            Pair<String, Integer> pair = new Pair<>();
            pair.first = compareString;
            pair.second = count;
            pairs.add(pair);
            allCount += count;
        }
        Pair<String, Integer> pair = new Pair<>();
        pair.first = "Total";
        pair.second = allCount;
        pairs.add(pair);
        statistics.setPairs(pairs);
        records.add(statistics.toCsvFormat());
        return statistics;
    }

}