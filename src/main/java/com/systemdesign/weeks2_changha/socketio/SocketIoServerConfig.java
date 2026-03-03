package com.systemdesign.weeks2_changha.socketio;

import com.systemdesign.weeks2_changha.friend.FriendRelationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({SocketIoProperties.class, FriendRelationProperties.class})
public class SocketIoServerConfig {
}
