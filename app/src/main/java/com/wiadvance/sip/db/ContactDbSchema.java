package com.wiadvance.sip.db;

public class ContactDbSchema {

    public static final class ContactTable{
        public static final String NAME = "contacts";

        public static final class Cols{
            public static final String ID = "_id";
            public static final String NAME = "name";
            public static final String SIP = "sip";
            public static final String PHONE = "phone";
            public static final String EMAIL = "email";
            public static final String PHOTO = "photo";
            public static final String TYPE = "type";
            public static final String CREATED_TIME = "create_time";
        }
    }
}
