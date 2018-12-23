package com.example.esquecer.myapplication.filter;

import java.io.File;
import java.io.FilenameFilter;

public class musicFilter implements FilenameFilter {
    private String type;

    public musicFilter(String type) {
        this.type = type;
    }
    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith(type);
    }
}
