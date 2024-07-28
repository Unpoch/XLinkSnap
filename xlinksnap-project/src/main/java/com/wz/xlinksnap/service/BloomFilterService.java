package com.wz.xlinksnap.service;

public interface BloomFilterService {

    public boolean containLUrl(String lurl);

    void addLUrl(String lurl);
}
