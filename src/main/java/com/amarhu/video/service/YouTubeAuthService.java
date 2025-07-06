package com.amarhu.video.service;

import com.google.api.client.auth.oauth2.Credential;
import com.amarhu.config.YoutubeConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class YouTubeAuthService {

    @Autowired
    private YoutubeConfig youtubeConfig;

    public Credential getCredential() throws Exception {
        return youtubeConfig.authorize();
    }
}
