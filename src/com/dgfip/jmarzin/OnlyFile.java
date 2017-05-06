package com.dgfip.jmarzin;

import java.io.File;
import java.io.FilenameFilter;

public class OnlyFile implements FilenameFilter {
    private String ext;

    OnlyFile(String ext) {
        this.ext = "." + ext;
    }
    public boolean accept(File dir, String name) {
        return name.endsWith(ext);
    }
}
