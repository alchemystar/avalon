/*
 * Copyright (C) 2015 alchemystar, Inc. All Rights Reserved.
 */
package avalon.plugin.example;

/*
 * Example plugin. Return a fake result set for every query
 */

import avalon.mysql.proto.*;
import avalon.net.context.ConContext;
import avalon.plugin.plugins.AvalonPluginBase;

import java.util.ArrayList;


public class ResultSetExample extends AvalonPluginBase {

    
    public boolean read_query(ConContext context) {
        ResultSet rs = new ResultSet();
        rs.sequenceId = Packet.getSequenceId(context.packet) + 1;
        if(context.query.equals("show databases")){
            Column col = new Column("DataBases");
            rs.addColumn(col);
            rs.addRow(new Row("saber"));
            rs.addRow(new Row("archer"));
            rs.addRow(new Row("lancer"));

        }else {
            Column col = new Column("Fake Data");
            rs.addColumn(col);

            rs.addRow(new Row("You ars SB！！！"));
        }
        context.clear_buffer();
        context.buffer = rs.toPackets();
        return true;

    }
}
