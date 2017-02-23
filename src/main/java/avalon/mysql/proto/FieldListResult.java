package avalon.mysql.proto;

import java.util.ArrayList;

/**
 * Created by alchemystar on 16/1/5.
 */
public class FieldListResult {
    public long sequenceId = 1;
    public static long characterSet = 0;

    public ArrayList<Column> columns = new ArrayList<Column>();
    public ArrayList<Row> rows = new ArrayList<Row>();


    public ArrayList<byte[]> toPackets() {
        ArrayList<byte[]> packets = new ArrayList<byte[]>();

        long maxRowSize = 0;

        for (Column col: this.columns) {
            long size = col.toPacket().length;
            if (size > maxRowSize)
                maxRowSize = size;
        }

        maxRowSize = 0;

        for (Column col: this.columns) {
            col.sequenceId = this.sequenceId;
            col.columnLength = maxRowSize;
            this.sequenceId++;
            packets.add(col.toPacket());
        }

        EOF eof = new EOF();
        eof.sequenceId = this.sequenceId;
        this.sequenceId++;
        packets.add(eof.toPacket());

        return packets;
    }

    public void addColumn(Column column) {
        this.columns.add(column);
    }

    public void addRow(Row row) {
        this.rows.add(row);
    }
}
