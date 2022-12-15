package com.example.webcrawler.controllers;

import au.com.bytecode.opencsv.CSVWriter;
import com.example.webcrawler.crawler.WebCrawler;
import com.example.webcrawler.data.Message;
import org.springframework.web.bind.annotation.*;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;

@RestController
@RequestMapping("/api/")
public class LinkController {

    @GetMapping("/parseLink")
    public void parseLink(@RequestBody Message message) throws IOException, InterruptedException {
        WebCrawler webCrawler = new WebCrawler();
        System.out.println(message.toString());
        webCrawler.processingLink(message);
    }

}
