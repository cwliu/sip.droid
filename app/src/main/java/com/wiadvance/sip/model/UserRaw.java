package com.wiadvance.sip.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class UserRaw {

    @SerializedName("@odata.context")
    public String data;

    public ArrayList<InnerDict> value;

    public class InnerDict {
        public String displayName;
        public ArrayList<String> businessPhones;
        public String mobilePhone;
        public String mail;
    }
}
