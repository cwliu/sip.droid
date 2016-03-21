package com.wiadvance.sipdemo;

public abstract class WiSipManager {

    public abstract void register(String account);

    public abstract boolean unregister(String account);

    public abstract boolean isSupported();

    public abstract void makeCall(String account);

    public abstract void endCall();

    public abstract void listenIncomingCall();

    public abstract void unlistenIncomingCall();
}
