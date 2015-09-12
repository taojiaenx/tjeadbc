package org.adbcj.mysql.codec.packets;

import org.adbcj.DbException;
import org.adbcj.mysql.codec.MysqlCharacterSet;
import org.adbcj.support.CancellationToken;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * @author roman.stoffel@gamlor.info
 * @since 11.04.12
 */
public class StringCommandRequest extends CommandRequest {
    private final String payload;
    private byte[] dataAsBytes = null;

    public StringCommandRequest(Command command, String payload, CancellationToken cancelSupport) {
        super(command,cancelSupport);
        this.payload = payload;
    }


    @Override
    public int getLength() {
        return 1 + payloadAsBinary().length;
    }

    @Override
    protected boolean hasPayload() {
        return payload != null;
    }

    @Override
    protected void writePayLoad(OutputStream out) throws IOException {
        out.write(payloadAsBinary());
    }

    @Override
    public String toString() {
        return "StringCommandRequest{" +
                "payload='" + payload + '\'' +
                ", dataAsBytes=" + payloadAsBinary() +
                '}';
    }

    private byte[] payloadAsBinary() {
        if(null!=dataAsBytes){
            return dataAsBytes;
        }
        try {
            dataAsBytes = payload.getBytes(MysqlCharacterSet.UTF8_UNICODE_CI.getCharsetName());
            return dataAsBytes;
        } catch (UnsupportedEncodingException e) {
            throw DbException.wrap(e);
        }
    }



}
