package com.wiadvance.sip.db;

public class CallLogDbSchema {

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
}
