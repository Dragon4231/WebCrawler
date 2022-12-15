package com.example.webcrawler.data;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;

@Data
public class Statistics {
    String link;

    ArrayList<Pair<String,Integer>> pairs = new ArrayList<>();

    public String[] toCsvFormat(){
        ArrayList<String> record = new ArrayList<>();
        record.add(link);
        for(Pair<String,Integer> p : pairs){
            record.add(p.second+"");
        }
        return record.toArray(new String[record.size()]);
    }

}
