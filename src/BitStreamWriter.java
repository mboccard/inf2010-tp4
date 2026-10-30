import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitStreamWriter implements AutoCloseable {
	private byte writeBuffer = 0;
    private int bitsWritten = 0;
    
    private int totalByteWritten = 0;
    private FileOutputStream writeStream;
	
    public BitStreamWriter(String filePath) throws FileNotFoundException {
    	writeStream = new FileOutputStream(filePath);
    }
	
	public void writeBit(byte v) throws IOException {
		writeBuffer <<= 1;
		writeBuffer |= v & 1;
    	bitsWritten++;
    	
		if (bitsWritten == 8)
		{
			writeStream.write(writeBuffer);
			totalByteWritten++;
			writeBuffer = 0;
			bitsWritten = 0;
		}
	}
	
	public void writeByte(byte v) throws IOException {
		int bitDelta = 8 - bitsWritten;
		
		byte highV = (byte) ((v & 0xFF) >>> bitsWritten);
		byte lowV = (byte) ((v & 0xFF) & (0xFF >>> bitDelta) );
		
		writeBuffer = (byte)((writeBuffer << bitDelta) | highV);
		
		writeStream.write(writeBuffer);
		totalByteWritten++;
		writeBuffer = lowV;
	}

	public void writeSize(int s) throws IOException {
		writeByte((byte)((s >>> 24) & 0xFF));
		writeByte((byte)((s >>> 16) & 0xFF));
		writeByte((byte)((s >>> 8) & 0xFF));
		writeByte((byte)((s >>> 0) & 0xFF));
	}
	
	public int getTotalWrittenSize() { return totalByteWritten + (bitsWritten > 0 ? 1 : 0); }		
	
	public void flush() throws IOException {
		if (bitsWritten > 0) {
			writeBuffer <<= (8 - bitsWritten);
			writeStream.write(writeBuffer);
			totalByteWritten++;
			writeBuffer = 0;
			bitsWritten = 0;
        }
	}

	@Override
	public void close() throws IOException {
		flush();
		writeStream.close();
	}
}