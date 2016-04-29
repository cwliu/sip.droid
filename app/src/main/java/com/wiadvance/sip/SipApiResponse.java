package com.wiadvance.sip;

public class SipApiResponse {

    public String email;
    public String backend_access_token;
    public String sip_account;
    public String sip_password;
    public String proxy_address;
    public String proxy_port;
    public SipAccount[] sip_list;

    public static class SipAccount {
        public String email;
        public String sip_account;
        public String phone;
    }
}
