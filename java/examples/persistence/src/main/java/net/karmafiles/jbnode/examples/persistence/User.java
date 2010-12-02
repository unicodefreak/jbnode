package net.karmafiles.jbnode.examples.persistence;

/**
 * Created by Ilya Brodotsky
 * Date: 22.09.2010
 * Time: 17:53:33
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */

@javax.persistence.Entity
@javax.persistence.Table(name = "USER")
public class User implements java.io.Serializable
{
    private String id;
    private String login;

    public User() {}

    @javax.persistence.Id
    @javax.persistence.Column(name = "ID", nullable = false, insertable = true, updatable = true, length = 64)
    public String getId()
    {
        return id;
    }

    public void setId(String value)
    {
        this.id = value;
    }


    @javax.persistence.Column(name = "LOGIN", nullable = false, insertable = true, updatable = true, length = 36)
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

}