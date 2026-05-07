package com.nexaworks.rafiq.service.call;

public interface RtcProvider {
    String generateToken(String channelName, int expiration);
}
