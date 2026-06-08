package jrm.server.shared;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import jrm.misc.IOUtils;

/**
 * class to create a temporary file and return an input stream to read from it. The file will be deleted when the input stream is closed. This is useful for creating a temporary file to store data that will be sent to the client, and then deleting the file after it has been sent.
 */
public class TempFileInputStream extends FileInputStream {
    /** The temporary file that the input stream reads from. This file will be deleted when the input stream is closed. */
    private final File file;
    /** The length of the temporary file. This is useful for setting the Content-Length header when sending the file to the client. */
    private final long length;

    /** Creates a new TempFileInputStream that reads from the given file. The file will be deleted when the input stream is closed.
     * @param file the temporary file to read from
     * @throws FileNotFoundException if the file does not exist
     */
    public TempFileInputStream(File file) throws FileNotFoundException {
        super(file);
        this.file = file;
        this.length = file.length();
    }

    @Override
    public void close() throws IOException {
        super.close();
        Files.deleteIfExists(file.toPath());
    }

    /**
     * create an instance of TempFileInputStream that reads from a new temporary file. The file will be deleted when the input stream is closed.
     * @return InputStream that reads from a temporary file. The file will be deleted when the input stream is closed.
     * @throws IOException if an I/O error occurs while creating the temporary file
     */
    public static InputStream newInstance() throws IOException {
        return new TempFileInputStream(IOUtils.createTempFile("JRMSRV", null).toFile());
    }

    /** create an instance of TempFileInputStream that reads from a new temporary file, and writes the contents of the given input stream to the temporary file. The file will be deleted when the input stream is closed.
     * @param in the input stream to read from and write to the temporary file
     * @return InputStream that reads from a temporary file containing the contents of the given input stream. The file will be deleted when the input stream is closed.
     * @throws IOException if an I/O error occurs while creating the temporary file or writing to it
     */
    public static InputStream newInstance(InputStream in) throws IOException {
        return newInstance(in, -1L, false);
    }

    /** create an instance of TempFileInputStream that reads from a new temporary file, and writes the contents of the given input stream to the temporary file. The file will be deleted when the input stream is closed.
     * @param in the input stream to read from and write to the temporary file
     * @param len the length of the input stream to read and write to the temporary file. If len is negative, the entire input stream will be read and written to the temporary file.
     * @return InputStream that reads from a temporary file containing the contents of the given input stream. The file will be deleted when the input stream is closed.
     * @throws IOException if an I/O error occurs while creating the temporary file or writing to it
     */
    public static InputStream newInstance(InputStream in, long len) throws IOException {
        return newInstance(in, len, false);
    }

    /** create an instance of TempFileInputStream that reads from a new temporary file, and writes the contents of the given input stream to the temporary file. The file will be deleted when the input stream is closed.
     * @param in the input stream to read from and write to the temporary file
     * @param len the length of the input stream to read and write to the temporary file. If len is negative, the entire input stream will be read and written to the temporary file.
     * @param close whether to close the input stream after writing to the temporary file
     * @return InputStream that reads from a temporary file containing the contents of the given input stream. The file will be deleted when the input stream is closed.
     * @throws IOException if an I/O error occurs while creating the temporary file or writing to it
     */
    public static InputStream newInstance(InputStream in, long len, boolean close) throws IOException {
        final var tmpfile = IOUtils.createTempFile("JRMSRV", null);
        try (final var out = new BufferedOutputStream(Files.newOutputStream(tmpfile))) {
            if (len < 0)
                for (int b = in.read(); b != -1; b = in.read())
                    out.write(b);
            else
                for (long i = 0; i < len; i++)
                    out.write(in.read());
            if (close)
                in.close();
        }
        return new TempFileInputStream(tmpfile.toFile());
    }

    /**
     * return the length of the temporary file. This is useful for setting the Content-Length header when sending the file to the client.
     * @return the length of the temporary file
     */
    public long getLength() {
        return length;
    }

}
