package com.lorduza.pingstabilizer.client.config;

import com.google.gson.annotations.Expose;

public class PingStabilizerConfig {

    @Expose public boolean tcpNoDelay = true;
    @Expose public boolean priorityFlush = true;

    @Expose public boolean customBufferSize = false;
    @Expose public int sendBufferKB = 128; // Default 128KB
    @Expose public int receiveBufferKB = 128;

    @Expose public boolean smartQueue = false;
    @Expose public boolean adaptiveThrottle = false;
    @Expose public int maxHoldMs = 100;
    @Expose public boolean disableCompression = false;

    @Expose public boolean enableDebugLog = false;

    @Expose public boolean hudEnabled = true;
    @Expose public boolean showPing = true;
    @Expose public boolean showJitter = true;
    @Expose public boolean showPacketLoss = true;
    @Expose public boolean showPacketStats = true;
    @Expose public boolean showNetworkQuality = true;
    
    @Expose public int keepAliveMs = 5000;

    @Expose public int pingX = 10;
    @Expose public int pingY = 10;
    
    @Expose public int jitterX = 10;
    @Expose public int jitterY = 22;
    
    @Expose public int lossX = 10;
    @Expose public int lossY = 34;
    
    @Expose public int qualityX = 10;
    @Expose public int qualityY = 46;
    
    @Expose public int ppsX = 10;
    @Expose public int ppsY = 58;
}


