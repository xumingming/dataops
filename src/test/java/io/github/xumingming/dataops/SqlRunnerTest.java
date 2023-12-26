package io.github.xumingming.dataops;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.List;

import static io.github.xumingming.dataops.RunCommand.parsePlaceholders;
import static io.github.xumingming.dataops.SqlRunner.rewrite;

public class SqlRunnerTest
        extends TestCase
{
    @Test
    public void testRewrite()
    {
        List<Placeholder> placeholderList = parsePlaceholders(" a:1,2;b:3,4 ");
        assertEquals("hello 1, 3, 1", rewrite("hello __a__, __b__, __a__", placeholderList));

        placeholderList = parsePlaceholders(" a :1,100;b:200,300 ");
        System.out.println(rewrite("hello __a__, __b__, __a__", placeholderList));
        System.out.println(rewrite("hello __a__, __b__, __a__", placeholderList));
        System.out.println(rewrite("hello __a__, __b__, __a__", placeholderList));
        System.out.println(rewrite("hello __a__, __b__, __a__", placeholderList));
        System.out.println(rewrite("hello __a__, __b__, __a__", placeholderList));
    }
}
