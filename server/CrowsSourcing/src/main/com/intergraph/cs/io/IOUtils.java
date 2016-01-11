package com.intergraph.cs.io;



import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.security.MessageDigest;

public class IOUtils {
	public static final String TAG = IOUtils.class.getSimpleName();

	public static final String UTF_16BE = "UTF-16BE";
	public static final String UTF_16LE = "UTF-16LE";
	public static final String UTF_8 = "UTF-8";

	/**
	 * Standard buffer size for stream operations.
	 */
	public static final int BUFFER_SIZE = 1024;

	/**
	 * Wraps InputStream by a Reader ensuring correct handling of extended character sets,
	 * such as UTF-16LE, UTF-16BE and UTF-8.
	 * 
	 * @param input An input stream.
	 * @return A wrapper reader.
	 * 
	 * @throws java.io.IOException If something goes wrong.
	 */
	public static Reader streamToReader(InputStream input) throws IOException {
		String encoding = null;
		BufferedInputStream in = new BufferedInputStream(input);
		in.mark(3);
		int byte1 = in.read();
		int byte2 = in.read();
		if (byte1 == 0xFF && byte2 == 0xFE) {
			encoding = UTF_16LE;
		} else if (byte1 == 0xFE && byte2 == 0xFF) {
			encoding = UTF_16BE;
		} else {
			int byte3 = in.read();
			if (byte1 == 0xEF && byte2 == 0xBB && byte3 == 0xBF) {
				encoding = UTF_8;
			} else {
				in.reset();
			}
		}
		

		if (encoding != null) {
			return new InputStreamReader(in, encoding);
		}

		return new InputStreamReader(in);
	}
	
    public static String readerToString(Reader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
 
        char[] buffer = new char[BUFFER_SIZE];
        int read;
        while ((read = reader.read(buffer)) != -1) {
        	builder.append(buffer, 0, read);
        }
        
        return builder.toString();
    }
    
    public static String streamToString(InputStream stream) throws IOException {
    	Reader reader = streamToReader(stream);
    	String result = readerToString(reader);
		closeSilently(reader);

		return result;
    }
    
    public static void copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		int read;
		while ((read = input.read(buffer, 0, buffer.length)) != -1) {
			output.write(buffer, 0, read);
		}
    }

    public static void closeSilently(AutoCloseable closeable) {
    	try {
    		if (closeable != null)
	    		closeable.close();
    	}
    	catch (Exception e) {
    	}
    }
}
