package net.karmafiles.jbnode.examples.json;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.IOException;

/**
 * Created by Ilya Brodotsky
 * Date: 22.09.2010
 * Time: 16:45:50
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */                                            

public class JSON {

    public final static String jbNodeVersion = "1.0";

    private JsonObject jsonObject;

    /*
        Serializing from JSON string and back.

        For more information please refer to Jackson website
        http://jackson.codehaus.org/
        
     */
    public byte[] test(byte[] data) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        // The following line is optional. 
        // if omitted, will write
        //  {"nodes":[{"nodes":null,"name":"test2","id":2},{"nodes":null,"name":"test3","id":3}],"name":"test1","id":1}
        // instead of  
        //  {"name":"test1","id":1,"nodes":[{"name":"test2","id":2},{"name":"test3","id":3}]}
        objectMapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);

        jsonObject = objectMapper.readValue(data, 0, data.length, JsonObject.class);

        return objectMapper.writeValueAsBytes(jsonObject);
    }

}
