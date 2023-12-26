package io.github.xumingming.dataops;

import java.util.Objects;

public class Placeholder
{
    private String key;
    private int start;
    private int end;

    public Placeholder(String key, int start, int end)
    {
        this.key = key;
        this.start = start;
        this.end = end;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public int getStart()
    {
        return start;
    }

    public void setStart(int start)
    {
        this.start = start;
    }

    public int getEnd()
    {
        return end;
    }

    public void setEnd(int end)
    {
        this.end = end;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Placeholder that = (Placeholder) o;
        return start == that.start && end == that.end && key.equals(that.key);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(key, start, end);
    }

    @Override
    public String toString()
    {
        return "PlaceholderInfo{" +
                "key='" + key + '\'' +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}
