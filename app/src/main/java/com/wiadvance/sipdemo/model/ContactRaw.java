package com.wiadvance.sipdemo.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ContactRaw {

    @SerializedName("@odata.context")
    public String data;

    public ArrayList<InnerDict> value;

    public class InnerDict {
        public String displayName;
        public ArrayList<String> businessPhones;
    }
}
