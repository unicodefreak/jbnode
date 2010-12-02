package net.karmafiles.jbnode.examples.json;

/**
 * Created by Ilya Brodotsky
 * Date: 22.09.2010
 * Time: 16:52:52
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */

/*
    Test class used for serialization
      
 */
public class JsonObject {
    private String name;
    private Integer id;

    private JsonObject[] nodes;

    public JsonObject() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public JsonObject[] getNodes() {
        return nodes;
    }

    public void setNodes(JsonObject[] nodes) {
        this.nodes = nodes;
    }
}
