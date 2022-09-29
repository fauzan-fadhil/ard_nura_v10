package com.arindo.nura;

import java.io.File;

/**
 * Created by bmaxard on 09/08/2017.
 */

public class ClearCache {
    String PATH = MyConfig.path();
    public void CacheClear(){
        try {
            File cachefolder = new File(PATH + "/Android/data/com.arindo.nura/cache");
            if (cachefolder.isDirectory()) {
                String[] children = cachefolder.list();
                for (int i = 0; i < children.length; i++) {
                    new File(cachefolder, children[i]).delete();
                }
            }
        }catch (Exception e){}
    }
}
