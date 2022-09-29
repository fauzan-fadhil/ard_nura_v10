package com.arindo.nura;

/**
 * Created by bmaxard on 20/02/2017.
 */

public class LazyAdapterRestoDetailModel {
    public String name;
    public String id;
    //public int img;

    //public LazyAdapterRestoDetailModel(String id, String name, int img){
    public LazyAdapterRestoDetailModel(String id, String name){
        this.name = name;
        //this.img = img;
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

   // public int getImg() {
   //     return img;
   // }

    //public void setImg(int img) {
    //    this.img = img;
    //}
}
