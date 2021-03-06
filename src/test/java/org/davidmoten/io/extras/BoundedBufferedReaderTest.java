package org.davidmoten.io.extras;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.junit.Test;

public class BoundedBufferedReaderTest {

    @Test
    public void testTestReader() throws IOException {
        Reader r = createReader(2, 3);
        String s = toString(r);
        assertEquals("ab\nc\n", s);
    }

    @Test
    public void testBoundNotMetUnlimitedMaxBufferSize() throws IOException {
        String s = toString(createReader(5, 10000));
        BoundedBufferedReader b = new BoundedBufferedReader(createReader(5, 10000));
        String s2 = toString(b);
        assertEquals(s, s2);
    }

    @Test
    public void testBoundNotMetLargeMaxBufferSize() throws IOException {
        String s = toString(createReader(5, 10000));
        BoundedBufferedReader b = new BoundedBufferedReader(createReader(5, 10000), 512, 1000000);
        String s2 = toString(b);
        assertEquals(s, s2);
    }

    @Test
    public void testBoundMet() throws IOException {
        System.out.println(toString(createReader(26, 100)));
        BoundedBufferedReader b = new BoundedBufferedReader(createReader(26, 100), 8, 8);
        String line = "abcdefgh\n";
        assertEquals(line + line + line + line, toString(b));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeMaxLineLengthThrows() throws IOException {
        try (BoundedBufferedReader b = new BoundedBufferedReader(createReader(26, 100), 8, -1)) {
        }
    }

    @Test
    public void testOutOfMemoryErrorDoesNotOccur() throws IOException {
        long length = Long.parseLong(System.getProperty("n", Integer.toString(Integer.MAX_VALUE/10)));
        try (BoundedBufferedReader b = new BoundedBufferedReader(createReader(length, length), 8192, 4)) {
            assertEquals("abcd", b.readLine());
        }
    }

    private static String toString(Reader r) throws IOException {
        StringBuilder b = new StringBuilder();
        try (BufferedReader br = new BufferedReader(r)) {
            String line = null;
            while ((line = br.readLine()) != null) {
                b.append(line);
                b.append("\n");
            }
        }
        return b.toString();
    }

    private static String toString(BoundedBufferedReader br) throws IOException {
        StringBuilder b = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null) {
            b.append(line);
            b.append("\n");
        }
        return b.toString();
    }

    private static Reader createReader(long lineLength, long length) {
        return new Reader() {
            final char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
            long index = 0;
            long lineCharsCount = 0;

            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                if (index == length) {
                    return -1;
                }
                for (int i = off; i < off + len; i++) {
                    if (index == length) {
                        return i - off;
                    } else if (lineCharsCount == lineLength) {
                        cbuf[i] = '\n';
                        lineCharsCount = 0;
                    } else {
                        cbuf[i] = chars[(int) (index % chars.length)];
                        lineCharsCount++;
                        index++;
                    }
                }
                return len;
            }

            @Override
            public void close() throws IOException {
                // do nothing
            }

        };
    }

}
