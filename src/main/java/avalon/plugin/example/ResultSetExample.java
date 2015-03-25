/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package avalon.plugin.example;

/*
 * Example plugin. Return a fake result set for every query
 */

import java.util.Date;
import org.apache.log4j.Logger;

import avalon.Engine;
import avalon.mysql.proto.Column;
import avalon.mysql.proto.Flags;
import avalon.mysql.proto.ResultSet;
import avalon.mysql.proto.Row;
import avalon.plugin.Base;

public class ResultSetExample extends Base {

    public void init(Engine context) {
        this.logger = Logger.getLogger("Plugin.Example.ResultSetExample");
    }
    
    public void read_query_result(Engine context) {
        this.logger.info("Plugin->read_query");

        if(context.query.indexOf("select")>=0) {

            ResultSet rs = new ResultSet();

            Column col = new Column("Fake Data");
            rs.addColumn(col);

            rs.addRow(new Row("You ars SB！！！"));
            context.clear_buffer();
            context.buffer = rs.toPackets();
            context.nextMode = Flags.MODE_SEND_QUERY_RESULT;
        }
    }
}
