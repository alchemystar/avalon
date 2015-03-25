/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package avalon.mysql.proto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.HexDump;
import org.apache.log4j.Logger;

import avalon.net.context.ConContext;
import io.netty.buffer.ByteBuf;

/**
 * Packet
 * +Header
 * +++++Length  3bytes
 * +++++sequenceId 1byte
 * Body
 */
public abstract class Packet {
    public long sequenceId = 0;

    public abstract ArrayList<byte[]> getPayload();

    public byte[] toPacket() {
        ArrayList<byte[]> payload = this.getPayload();

        int size = 0;
        for (byte[] field : payload) {
            size += field.length;
        }

        byte[] packet = new byte[size + 4];

        System.arraycopy(Proto.build_fixed_int(3, size), 0, packet, 0, 3);
        System.arraycopy(Proto.build_fixed_int(1, this.sequenceId), 0, packet, 3, 1);

        int offset = 4;
        for (byte[] field : payload) {
            System.arraycopy(field, 0, packet, offset, field.length);
            offset += field.length;
        }

        return packet;
    }

    public static int getSize(byte[] packet) {
        int size = (int) new Proto(packet).get_fixed_int(3);
        return size;
    }

    public static byte getType(byte[] packet) {
        return packet[4];
    }

    public static long getSequenceId(byte[] packet) {
        return new Proto(packet, 3).get_fixed_int(1);
    }

    public static final void dump(byte[] packet) {
        Logger logger = Logger.getLogger("MySQL.Packet");

        if (!logger.isTraceEnabled()) {
            return;
        }

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            HexDump.dump(packet, 0, out, 0);
            logger.trace("Dumping packet\n" + out.toString());
        } catch (IOException e) {
            return;
        }
    }

    public static final void dump_stderr(byte[] packet) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            HexDump.dump(packet, 0, out, 0);
            System.err.println("Dumping packet\n" + out.toString());
        } catch (IOException e) {
            return;
        }
    }

    public static byte[] read_packet(ByteBuf in) throws IOException {
        int b = 0;
        int size = 0;
        byte[] packet = new byte[3];
        if (in.readableBytes() < 3) {
            return null;
        }
        //如果出现半包问题，回溯
        in.markReaderIndex();
        // Read size (3)
        int offset = 0;
        int target = 3;
        in.readBytes(packet, offset, (target - offset));
        size = Packet.getSize(packet);

        byte[] packet_tmp = new byte[size + 4];
        System.arraycopy(packet, 0, packet_tmp, 0, 3);
        packet = packet_tmp;
        packet_tmp = null;

        offset = offset + target;
        target = packet.length;
        if (in.readableBytes() < (target - offset)) {
            in.resetReaderIndex();
            return null;
        }
        in.readBytes(packet, offset, (target - offset));
        return packet;
    }

   /* public static ArrayList<byte[]> read_full_result_set(ByteBuf in, Channel out, ArrayList<byte[]> buffer,
                                                         boolean bufferResultSet) throws IOException {
        // Assume we have the start of a result set already

        byte[] packet = buffer.get((buffer.size() - 1));
        //结果集头部包
        long colCount = ColCount.loadFromPacket(packet).colCount;

        // Read the columns and the EOF field
        //字段包
        for (int i = 0; i < (colCount + 1); i++) {
            packet = Packet.read_packet(in, );
            if (packet == null) {
                throw new IOException();
            }
            buffer.add(packet);

            // Evil optimization
            if (!bufferResultSet) {
                Packet.write(out, buffer);
                out.flush();
                buffer.clear();
            }
        }

        int packedPacketSize = 65535;
        //这边考虑做成内存池的形式
        byte[] packedPacket = new byte[packedPacketSize];
        int position = 0;
        while (true) {
            packet = Packet.read_packet(in, );

            if (packet == null) {
                throw new IOException();
            }

            int packetType = Packet.getType(packet);

            if (packetType == Flags.EOF || packetType == Flags.ERR) {
                byte[] newPackedPacket = new byte[position];
                System.arraycopy(packedPacket, 0, newPackedPacket, 0, position);
                buffer.add(newPackedPacket);
                packedPacket = packet;
                break;
            }

            if (position + packet.length > packedPacketSize) {
                int subsize = packedPacketSize - position;
                System.arraycopy(packet, 0, packedPacket, position, subsize);
                buffer.add(packedPacket);

                // Evil optimization
                if (!bufferResultSet) {
                    Packet.write(out, buffer);
                    out.flush();
                    buffer.clear();
                }

                packedPacket = new byte[packedPacketSize];
                position = 0;
                System.arraycopy(packet, subsize, packedPacket, position, packet.length - subsize);
                position += packet.length - subsize;
            } else {
                System.arraycopy(packet, 0, packedPacket, position, packet.length);
                position += packet.length;
            }
        }
        buffer.add(packedPacket);

        // Evil optimization
        if (!bufferResultSet) {
            Packet.write(out, buffer);
            buffer.clear();
            out.flush();
        }

        if (Packet.getType(packet) == Flags.ERR) {
            return buffer;
        }

        if (EOF.loadFromPacket(packet).hasStatusFlag(Flags.SERVER_MORE_RESULTS_EXISTS)) {
            buffer.add(Packet.read_packet(in, ));
            buffer = Packet.read_full_result_set(in, out, buffer, bufferResultSet);
        }
        return buffer;
    }

    public static void write(Channel out, ArrayList<byte[]> buffer) throws IOException {
        for (byte[] packet : buffer) {
            out.write(packet);
        }
    }*/

}
