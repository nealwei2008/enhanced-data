package com.yoloho.enhanced.data.dao.generator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UUID32GeneratorTest {
    @Test
    public void test() {
        GeneratedContext context = new GeneratedContext(new Object(), new GeneratedField(null, null));
        String result = (String)new UUID32Generator().generate(context);
        assertNotNull(result);
        assertEquals(32, result.length());
    }
}
