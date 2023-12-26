package io.github.xumingming.dataops;

import org.junit.Test;

import java.util.List;

import static io.github.xumingming.dataops.RunCommand.parsePlaceholders;
import static org.junit.Assert.assertEquals;

public class RunCommandTest
{
    @Test
    public void testParsePlaceholders()
    {
        List<Placeholder> placeholderList = parsePlaceholders(" a : 1, 2;b:3, 4 ");
        assertEquals(2, placeholderList.size());
        assertEquals(new Placeholder("a", 1, 2), placeholderList.get(0));
        assertEquals(new Placeholder("b", 3, 4), placeholderList.get(1));
    }
}
