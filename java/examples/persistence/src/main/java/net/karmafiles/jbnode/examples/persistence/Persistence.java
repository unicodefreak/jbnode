package net.karmafiles.jbnode.examples.persistence;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Ilya Brodotsky
 * Date: 22.09.2010
 * Time: 17:45:13
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */

@Transactional
public class Persistence {
    public final static String jbNodeVersion = "1.0";

    private AtomicLong idGenerator = new AtomicLong(0L);

    private EntityManager entityManager;

	@PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private ObjectMapper getJsonObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);

        return objectMapper;
    }

    public byte[] saveUser(byte[] req) throws IOException {
        ObjectMapper objectMapper = getJsonObjectMapper();
        User user = objectMapper.readValue(req, 0, req.length, User.class);

        if(user.getId() == null) {
            user.setId(String.valueOf(idGenerator.incrementAndGet()));
        }

        entityManager.persist(user);

        return objectMapper.writeValueAsBytes(user);
    }

    public byte[] listUsers(byte[] req) throws IOException {
        ObjectMapper objectMapper = getJsonObjectMapper();

        List users = entityManager.createQuery("from User").getResultList();

        return objectMapper.writeValueAsBytes(users);
    }

}
