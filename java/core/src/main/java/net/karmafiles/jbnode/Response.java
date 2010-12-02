package net.karmafiles.jbnode;

/**
 * Created by Ilya Brodotsky
 * Date: 22.09.2010
 * Time: 14:59:06
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */

public class Response {
    private boolean error;
    private byte[] data;

    public Response() {
    }

    public Response(boolean error, byte[] data) {
        this.error = error;
        this.data = data;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
