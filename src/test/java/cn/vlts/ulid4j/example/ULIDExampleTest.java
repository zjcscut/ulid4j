package cn.vlts.ulid4j.example;

import cn.vlts.ulid4j.ULID;
import cn.vlts.ulid4j.ULIDFactory;
import org.junit.Assert;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author throwable
 * @version v1
 * @description Examples of ULID
 * @since 2022/10/19 20:33
 */
public class ULIDExampleTest {

    @Test
    public void tesCreateULID() {
        ULIDFactory ulidFactory = ULIDFactory.factory();
        System.out.println(ulidFactory.ulid());
        ulidFactory = ULIDFactory.factory(System::currentTimeMillis);
        System.out.println(ulidFactory.ulid());
        final SecureRandom secureRandom = new SecureRandom();
        ulidFactory = ULIDFactory.factory(System::currentTimeMillis, len -> {
            byte[] bytes = new byte[len];
            secureRandom.nextBytes(bytes);
            return bytes;
        });
        System.out.println(ulidFactory.ulid());
        System.out.println(ulidFactory.ulid(15000));
    }

    @Test
    public void tesCreateMonotonicULID() {
        ULIDFactory ulidFactory = ULIDFactory.monotonicFactory();
        System.out.println(ulidFactory.ulid());
        System.out.println(ulidFactory.ulid());
        ulidFactory = ULIDFactory.monotonicFactory(System::currentTimeMillis);
        System.out.println(ulidFactory.ulid());
        System.out.println(ulidFactory.ulid());
        final SecureRandom secureRandom = new SecureRandom();
        ulidFactory = ULIDFactory.monotonicFactory(System::currentTimeMillis, len -> {
            byte[] bytes = new byte[len];
            secureRandom.nextBytes(bytes);
            return bytes;
        });
        System.out.println(ulidFactory.ulid());
        System.out.println(ulidFactory.ulid());
        System.out.println(ulidFactory.ulid(System.currentTimeMillis() + 10000));
        System.out.println(ulidFactory.ulid());
    }

    @Test
    public void tesGetComponentsOfULID() {
        ULIDFactory ulidFactory = ULIDFactory.factory();
        ULID ulid = ulidFactory.ulid();
        System.out.printf("timestamp component => %s,randomness component => %s\n", ulid.getTimestamp(), Arrays.toString(ulid.getRandomness()));
    }

    @Test
    public void tesConvertToUUIDAndParseFromUUID() {
        ULIDFactory ulidFactory = ULIDFactory.factory();
        ULID ulid = ulidFactory.ulid();
        String o = ulid.toString();
        System.out.println(o);
        UUID uuid = ulid.toUUID();
        System.out.println(uuid);
        ulid = ULID.fromUUID(uuid);
        String t = ulid.toString();
        System.out.println(t);
        Assert.assertEquals(o, t);
    }

    @Test
    public void parseFromULIDString() {
        ULIDFactory ulidFactory = ULIDFactory.factory();
        ULID ulid = ulidFactory.ulid();
        String o = ulid.toString();
        System.out.println(o);
        ULID newUlid = ULID.fromString(o);
        System.out.println(newUlid);
        Assert.assertEquals(ulid, newUlid);
    }
}
