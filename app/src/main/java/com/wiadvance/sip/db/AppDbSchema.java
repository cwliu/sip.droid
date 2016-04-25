package com.wiadvance.sip.db;

public class AppDbSchema {

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

    public static final class CallLogTable{
        public static final String NAME = "calllogs";

        public static final class Cols{
            public static final String ID = "_id";
            public static final String CALL_TIME = "call_time";
            public static final String CALL_DURATION = "call_duration";
            public static final String CALL_TYPE = "callType";
            public static final String CONTACT = "contact";
        }
    }

    public static final class RegularContactTable {
        public static final String NAME = "regular_contacts";

        public static final class Cols{
            public static final String ID = "_id";
            public static final String CONTACT = "contact";
            public static final String COUNT = "count";
            public static final String UPDATED_TIME = "updated_time";
        }
    }
}
