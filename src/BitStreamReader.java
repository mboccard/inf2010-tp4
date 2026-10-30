import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class BitStreamReader implements AutoCloseable {
	private byte readBuffer = 0;
    private int bitsRead = 0;
    private FileInputStream readStream;
    
    public BitStreamReader(String filePath) throws FileNotFoundException {
    	readStream = new FileInputStream(filePath);
    }
	
    public byte readBit() throws IOException {
    	if (bitsRead < 1) {
    		readBuffer = (byte)readStream.read();
    		bitsRead = 8;
    	}
    	
    	byte bit = (byte) ((readBuffer & 0x80) >>> 7);
    	
    	readBuffer <<= 1;
    	bitsRead--;
    	
    	return bit;
    }
    
    public byte readByte() throws IOException {
    	byte returnValue = readBuffer;        	
    	int bitsInReturnValue = bitsRead;        	
    	int bitsToStillRead = 8 - bitsRead;
    	
    	readBuffer = (byte)readStream.read();
		bitsRead = 8;
		
		returnValue |= (readBuffer & 0xFF) >> bitsInReturnValue;
    	readBuffer <<= bitsToStillRead;
    	bitsRead = bitsInReturnValue;
    	
    	return returnValue;
    }
    
    public int readSize() throws IOException {
    	return (readByte() & 0xFF) << 24 | 
    		   (readByte() & 0xFF) << 16 | 
    		   (readByte() & 0xFF) << 8  | 
    		   (readByte() & 0xFF);
    }
    
    public boolean canRead() throws IOException {
    	return readStream.available() > 0 || bitsRead > 1;
    }
	
	@Override
	public void close() throws IOException {
		readStream.close();			
	}
	
}