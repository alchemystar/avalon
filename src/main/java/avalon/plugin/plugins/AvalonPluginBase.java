/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package avalon.plugin.plugins;

import java.io.IOException;

import org.apache.log4j.Logger;

import avalon.Engine;
import avalon.net.context.ConContext;
import io.netty.buffer.ByteBuf;

/**
 * Created by lizhuyang on 2015/3/23.
 */
public abstract class AvalonPluginBase {
    public Logger logger = Logger.getLogger("Plugin.Base");

    public void init(ConContext context) throws IOException {}

    public void read_handshake(ConContext context) throws IOException {}
    public void send_handshake(ConContext context) throws IOException {}

    public void read_auth(ConContext context) throws IOException {}
    public void send_auth(ConContext context) throws IOException {}

    public void read_auth_result(ConContext context) throws IOException {}
    public void send_auth_result(ConContext context) throws IOException {}

    public void read_query(ConContext context) throws IOException {}
    public void send_query(ConContext context) throws IOException {}

    public void read_query_result(ConContext context) throws IOException {}
    public void send_query_result(ConContext context) throws IOException {}
}
