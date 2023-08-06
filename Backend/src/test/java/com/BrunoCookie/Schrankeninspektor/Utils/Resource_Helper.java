package com.BrunoCookie.Schrankeninspektor.Utils;

import java.io.File;

public class Resource_Helper {
    public static File getResource(String filename){
        ClassLoader classLoader = Resource_Helper.class.getClassLoader();
        return new File(classLoader.getResource(filename).getFile());
    }
}
