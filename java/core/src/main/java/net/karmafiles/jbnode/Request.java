package net.karmafiles.jbnode;

/**
 * Created by Ilya Brodotsky
 * Date: 21.09.2010
 * Time: 14:36:18
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */

public class Request {
    private String service;
    private String method;
    private byte[] data;

    public Request() {
    }

    public Request(String service, String method, byte[] data) {
        this.service = service;
        this.method = method;
        this.data = data;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
