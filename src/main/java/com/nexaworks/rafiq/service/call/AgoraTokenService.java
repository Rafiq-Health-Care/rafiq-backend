package com.nexaworks.rafiq.service.call;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.agora.media.RtcTokenBuilder2;

@Service
@Qualifier("agora")
public class AgoraTokenService implements RtcProvider {
    @Value("${agora.app-id}")
    private String appId;

    @Value("${agora.app-certificate}")
    private String appCertificate;

    public String generateToken(String channelName, int expiration) {
        RtcTokenBuilder2 builder = new RtcTokenBuilder2();
        return builder.buildTokenWithUid(appId, appCertificate, channelName, 0,
                RtcTokenBuilder2.Role.ROLE_PUBLISHER, expiration, expiration);
    }

}
