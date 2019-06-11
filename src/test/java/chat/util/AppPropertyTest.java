package chat.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Alexander Diachenko
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testApplicationContext.xml"})
public class AppPropertyTest {

    @Autowired
    private AppProperty simpleProperties;

    @Test
    public void shouldReturnNameWhenPropertyCorrect() {
        Properties properties = simpleProperties.loadProperty();
        String name = properties.getProperty("name");
        assertEquals("alex", name);
    }

    @Test
    public void shouldReturnNullWhenPropertyIncorrect() {
        Properties properties = simpleProperties.loadProperty();
        String name = properties.getProperty("qwe");
        assertNull(name);
    }
}