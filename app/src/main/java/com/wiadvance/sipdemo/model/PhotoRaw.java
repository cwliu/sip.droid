package com.wiadvance.sipdemo.model;

import com.google.gson.annotations.SerializedName;

public class PhotoRaw {

    @SerializedName("@odata.context")
    public String data;

    @SerializedName("@odata.id")
    public String id;

    @SerializedName("Width")
    public int width;

    @SerializedName("Height")
    public int height;


//    public ArrayList<InnerDict> value;
//
//    public class InnerDict {
//        public String displayName;
//        public ArrayList<String> businessPhones;
//    }
}
