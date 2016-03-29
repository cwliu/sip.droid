package com.wiadvance.sipdemo;

import com.wiadvance.sipdemo.model.Contact;

public abstract class WiSipManager {

    public abstract void register(String account, String password, String domain);

    public abstract boolean unregister(String account);

    public abstract boolean isSupported();

    public abstract void makeCall(Contact contact);

    public abstract void endCurrentCall();

    public abstract void listenIncomingCall();

    public abstract void unlistenIncomingCall();
}
