package net.karmafiles.jbnode;

/**
 * Created by Ilya Brodotsky
 * Date: 01.09.2010
 * Time: 18:32:06
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */

public class JBNodeException extends Exception {
    public JBNodeException() {
    }

    public JBNodeException(String message) {
        super(message);
    }

    public JBNodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public JBNodeException(Throwable cause) {
        super(cause);
    }
}
