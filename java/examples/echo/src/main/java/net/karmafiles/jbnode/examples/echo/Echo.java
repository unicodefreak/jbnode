package net.karmafiles.jbnode.examples.echo;

/**
 * Created by Ilya Brodotsky
 * Date: 02.09.2010
 * Time: 20:32:30
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */

public class Echo {

    public final static String jbNodeVersion = "1.0";

    public byte[] echo(byte[] param) {
        return param;
    }

    public byte[] echoError(byte[] param) {
        throw new RuntimeException(new String(param));
    }

}
