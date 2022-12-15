package com.example.webcrawler.data;

import lombok.Data;

import java.util.ArrayList;

@Data
public class Message {
    String urlSeed;

    ArrayList<String> forCompare;

    int maxLinks;
}
