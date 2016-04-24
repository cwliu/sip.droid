package com.wiadvance.sip;

import com.wiadvance.sip.model.Contact;

public abstract class WiSipManager {

    public abstract void register(String account, String password, String domain);

    public abstract boolean unregister(String account);

    public abstract boolean isSupported();

    public abstract void makeCall(Contact contact);

    public abstract void endCall();

    public abstract void listenIncomingCall();

    public abstract void unlistenIncomingCall();
}
