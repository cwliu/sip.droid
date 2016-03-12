package com.wiadvance.sipdemo;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ContactRaw {

    @SerializedName("@odata.context")
    String data;

    ArrayList<InnerDict> value;

    class InnerDict {
        String displayName;
        ArrayList<String> businessPhones;
    }
}
