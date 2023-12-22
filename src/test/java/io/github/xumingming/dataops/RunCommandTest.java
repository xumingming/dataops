package io.github.xumingming.dataops;

import org.junit.Test;

import java.util.List;

import static io.github.xumingming.dataops.RunCommand.parsePlaceholders;
import static io.github.xumingming.dataops.RunCommand.rewrite;
import static org.junit.Assert.assertEquals;

public class RunCommandTest
{
    @Test
    public void testParsePlaceholders()
    {
        List<RunCommand.PlaceholderInfo> placeholderInfoList = parsePlaceholders(" a : 1, 2;b:3, 4 ");
        assertEquals(2, placeholderInfoList.size());
        assertEquals(new RunCommand.PlaceholderInfo("a", 1, 2), placeholderInfoList.get(0));
        assertEquals(new RunCommand.PlaceholderInfo("b", 3, 4), placeholderInfoList.get(1));
    }

    @Test
    public void testRewrite()
    {
        List<RunCommand.PlaceholderInfo> placeholderInfoList = parsePlaceholders(" a:1,2;b:3,4 ");
        assertEquals("hello 1, 3, 1", rewrite("hello __a__, __b__, __a__", placeholderInfoList));

        placeholderInfoList = parsePlaceholders(" a :1,100;b:200,300 ");
        System.out.println(rewrite("hello __a__, __b__, __a__", placeholderInfoList));
        System.out.println(rewrite("hello __a__, __b__, __a__", placeholderInfoList));
        System.out.println(rewrite("hello __a__, __b__, __a__", placeholderInfoList));
        System.out.println(rewrite("hello __a__, __b__, __a__", placeholderInfoList));
        System.out.println(rewrite("hello __a__, __b__, __a__", placeholderInfoList));
    }
}
