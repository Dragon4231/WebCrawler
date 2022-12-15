package com.example.webcrawler.data;

import lombok.Data;

@Data
public class Pair<T,E>{
    public T first;
    public E second;

    public Pair() {

    }

    public Pair(T first, E second) {
        this.first = first;
        this.second = second;
    }

}
