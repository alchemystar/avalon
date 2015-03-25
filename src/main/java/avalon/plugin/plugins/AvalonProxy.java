/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package avalon.plugin.plugins;

import java.io.IOException;

import avalon.mysql.proto.Com_Initdb;
import avalon.mysql.proto.Com_Query;
import avalon.mysql.proto.Flags;
import avalon.mysql.proto.Handshake;
import avalon.mysql.proto.HandshakeResponse;
import avalon.mysql.proto.Packet;
import avalon.net.context.ConContext;

/**
 * Created by lizhuyang on 2015/3/23.
 */
public class AvalonProxy extends AvalonPluginBase {
    @Override
    public void init(ConContext context) throws IOException {
        super.init(context);
    }

    @Override
    public void read_handshake(ConContext context) throws IOException {
        Handshake handshake = Handshake.loadFromPacket(context.packet);
        System.out.println(handshake.protocolVersion);
        System.out.println(handshake.authPluginName);
        System.out.println(handshake.serverVersion);
    }

    @Override
    public void send_handshake(ConContext context) throws IOException {
        super.send_handshake(context);
    }

    @Override
    public void read_auth(ConContext context) throws IOException {
        super.read_auth(context);
    }

    @Override
    public void send_auth(ConContext context) throws IOException {
        super.send_auth(context);
    }

    @Override
    public void read_auth_result(ConContext context) throws IOException {
        super.read_auth_result(context);
    }

    @Override
    public void send_auth_result(ConContext context) throws IOException {
        super.send_auth_result(context);
    }

    @Override
    public void read_query(ConContext context) throws IOException {
        Com_Query query = Com_Query.loadFromPacket(context.packet);
        switch (Packet.getType(context.packet)) {
            case Flags.COM_QUIT:
                this.logger.trace("COM_QUIT");
                break;

            // Extract out the new default schema
            case Flags.COM_INIT_DB:
                this.logger.trace("COM_INIT_DB");
                context.schema = Com_Initdb.loadFromPacket(context.packet).schema;
                break;

            // Query
            case Flags.COM_QUERY:
                this.logger.trace("COM_QUERY");
                if (query.query.indexOf("select") >= 0) {
                    query.query = "select * from t_sequence";
                } else {
                    System.out.println("query=" + query.query);
                }
                context.packet=query.toPacket();
                break;

            default:
                break;
        }
    }

    @Override
    public void send_query(ConContext context) throws IOException {
        super.send_query(context);
    }

    @Override
    public void read_query_result(ConContext context) throws IOException {
        super.read_query_result(context);
    }

    @Override
    public void send_query_result(ConContext context) throws IOException {
        super.send_query_result(context);
    }
}
