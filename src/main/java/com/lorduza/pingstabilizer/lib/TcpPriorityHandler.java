package com.lorduza.pingstabilizer.lib;

import com.lorduza.pingstabilizer.PingStabilizerMod;
import com.lorduza.pingstabilizer.client.config.ConfigManager;
import com.lorduza.pingstabilizer.client.config.PingStabilizerConfig;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;

public class TcpPriorityHandler {
    
    public static void apply(Channel channel) {
        PingStabilizerConfig config = ConfigManager.get();
        
        try {
            if (config.tcpNoDelay) {
                channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                PingStabilizerMod.LOGGER.info("TCP_NODELAY enabled");
            }


            try {
                channel.config().setOption(ChannelOption.IP_TOS, 0x10);
                PingStabilizerMod.LOGGER.info("IP_TOS set to 0x10 (Strict Low Delay)");
            } catch (Exception e) {

                try {
                    channel.config().setOption(ChannelOption.IP_TOS, 0x18);
                } catch (Exception ignored) {}
            }

            try {
                channel.config().setOption(ChannelOption.TCP_FASTOPEN, 1);
                PingStabilizerMod.LOGGER.info("TCP Fast Open enabled");
            } catch (Exception e) {

            }
            
            if (config.customBufferSize) {
                int sendBuf = config.sendBufferKB * 1024;
                int recvBuf = config.receiveBufferKB * 1024;
                
                channel.config().setOption(ChannelOption.SO_SNDBUF, sendBuf);
                channel.config().setOption(ChannelOption.SO_RCVBUF, recvBuf);
                
                PingStabilizerMod.LOGGER.info("Buffer sizes set - Send: {}KB, Receive: {}KB", 
                    config.sendBufferKB, config.receiveBufferKB);
            }
            
            channel.config().setOption(ChannelOption.SO_KEEPALIVE, true);
            
            SmartQueueManager.setChannel(channel);
            
        } catch (Exception e) {
            PingStabilizerMod.LOGGER.warn("TcpPriority apply failed", e);
        }
    }

    public static void forceFlush(Channel channel) {
        if (channel != null && channel.isActive()) {
            channel.flush();
        }
    }
}


