/*
 * Copyright (C) 2015 alchemystar, Inc. All Rights Reserved.
 */
package avalon.plugin.plugins;

import java.io.IOException;
import java.util.ArrayList;

import avalon.mysql.proto.*;
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
    public boolean read_query(ConContext context) throws IOException {
        Com_Query query = Com_Query.loadFromPacket(context.packet);
        switch (Packet.getType(context.packet)) {
            case Flags.COM_QUIT:
                System.out.println("COM_QUIT");
                return false;
            // Extract out the new default schema
            case Flags.COM_INIT_DB:
                System.out.println("COM_INIT_DB");
                context.schema = Com_Initdb.loadFromPacket(context.packet).schema;
                System.out.println(Packet.getSequenceId(context.packet));
                ArrayList<byte[]> okPacket = new ArrayList<byte[]>();
                okPacket.add(new byte[] { 7, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0 });
                context.buffer= okPacket;
                System.out.println("cominit"+context.buffer.size());
                return false;

            // Query
            case Flags.COM_QUERY:
                System.out.println("query=" + query.query);
                context.query=query.query;
                return true;

            case Flags.COM_FIELD_LIST:
                FieldListResult rs = new FieldListResult();
                Column col = new Column("DataBases");
                rs.addColumn(col);
                context.buffer=rs.toPackets();
                return false;
            default:
                System.out.println("Type="+Packet.getType(context.packet));
                return false;
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
