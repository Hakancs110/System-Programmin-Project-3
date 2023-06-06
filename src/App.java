import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


class T{
    public int valid;
    public int time;
    public String data;
    public String tag;


    T(int valid, int time, String data, String tag) {
        this.valid = valid;
        this.time = time;
        this.data = data;
        this.tag = tag;
    }

    public void setValid(int valid) {
        this.valid = valid;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getValid() {
        return valid;
    }

    public String getTag() {
        return tag;
    }
    

}

class Cache {
    public int sets;
    public int lines;
    public int blocksize;

    public String tagTemp;
    public final int address_size = 32;
    public int hits_counter = 0;
    public int misses_counter = 0;
    public int evictions_counter = 0;
    public T[][] cacheObject = null;   // create the structure of the cache
    public int i = 0;
    public int j = 0;
    public int time = 0;
    
    Cache(int sets_bits, int lines, int blocksize_bits) {
        this.sets = (int) Math.pow(2, sets_bits);          // S = 2^s
        this.lines = lines;
        this.blocksize = (int) Math.pow(2, blocksize_bits); // B = 2^b
        tagTemp = Integer.toString(address_size - sets - blocksize);
        
        cacheObject = new T[sets][lines];
    }
    
    public void writeCacheContents(String data) {
        if(cacheObject[i][j] == null) {
            misses_counter++;
            T temp = new T(1, time, data, tagTemp);
            cacheObject[i][j] = temp;
        }
    	if(cacheObject[i][j].getTag().equals(tagTemp)) {
            hits_counter++;
            time++;
        }
        else {
            misses_counter++;
            T temp = new T(1, time, data, tagTemp);
            cacheObject[i][j] = temp;
        }

        i++;
            if(i > sets) {
                i = 0;
                j++;
            }
    }
} 


public class App {
	public static byte[] RAM;
    public static String[] RAMhex;
	
    public static void main(String[] args) throws Exception {
    	// Check for the length of args
    	// if(args.length != 15) {
    	// 	System.out.println("Missing arguments!");
    	// 	System.exit(1);
  		// }
    	
    	readRAM();
        //test for the args
        String[] test = {"-L1s", "0", "-L1E", "2" , "-L1b","3",
        "-L2s","1" ,"-L2E", "2", "-L2b", "3",
        "-t", "test_small.trace"};

        int l1s = 0;
        int l1E = 0;
        int l1b = 0;
        
        int l2s = 0;
        int l2E = 0;
        int l2b = 0;
        String tracefilename = "";
        args = test;
        
        int args_length = args.length;
        //read the args
        for(int i = 0; i < args_length; i += 2)
		{
			switch(args[i])
			{
			case "-L1s":
                l1s = Integer.parseInt(args[i + 1]);
                break;
            case "-L1E":
                l1E = Integer.parseInt(args[i + 1]);
                break;
            case "-L1b":
                l1b = Integer.parseInt(args[i + 1]);
                break;
            case "-L2s":
                l2s = Integer.parseInt(args[i + 1]);
                break;
            case "-L2E":
                l2E = Integer.parseInt(args[i + 1]);
                break;
            case "-L2b":
                l2b = Integer.parseInt(args[i + 1]);
                break;
            case "-t":
                tracefilename = args[i + 1];
                break;
            default:
                System.err.println(args[i] + " is an invalid argument!");
                System.exit(1);
			}
		}

        //create the caches
        Cache L1I = new Cache(l1s, l1E, l1b);
        // L1I.writeCacheContents();
        
        Cache L1D = new Cache(l1s, l1E, l1b);
        // L1D.writeCacheContents();
        
        Cache L2 = new Cache(l2s, l2E, l2b);
        // L2.writeCacheContents();
        L2.writeCacheContents("data");

        //read the trace file
        FileReader tracefile = new FileReader(new File("./src/" +tracefilename));
        BufferedReader br = new BufferedReader(tracefile);
        String line = null;
        String traces = br.readLine() + " ";

        while ((line = br.readLine()) != null) {
            traces += line + " ";
        }
        br.close();
        tracefile.close();


        //parse the trace file
        //System.out.println(traces);
        traces = traces.replaceAll(",", "");
        String [] trace = traces.split(" ");
        
        int trace_length = trace.length;
        for(int i = 0; i < trace_length; i++){
            //System.out.println(trace[i]);
            if(trace[i].equals("I")){
                String address = trace[i+1];
                int addressIndex = Integer.parseInt(address, 16) / 8;
                String data = RAMhex[addressIndex];
                String size = trace[i+2];

                // L2.writeCacheContents(data);
                // L1I.writeCacheContents(data);
                i += 2;
                
            }
            else if(trace[i].equals("L")) {
                String address = trace[i+1];
                int addressIndex = Integer.parseInt(address, 16) / 8;
                String data = RAMhex[addressIndex];
                String size = trace[i+2];
                i += 2;
                
            }
            else if(trace[i].equals("M")) {
                String address = trace[i+1];
                int addressIndex = Integer.parseInt(address, 16) / 8;
                String data = RAMhex[addressIndex];
                String size = trace[i+2];
                String traceData = trace[i+3];
                i += 3;
            }
            else if(trace[i].equals("S")) {
                String address = trace[i+1];
                int addressIndex = Integer.parseInt(address, 16) / 8;
                String data = RAMhex[addressIndex];
                String size = trace[i+2];
                String traceData = trace[i+3];
                i += 3;
            }
        }  
    }

    public static void readRAM() {
        File ramFile = new File("./src/RAM.dat");
        long fileLength = ramFile.length();
        RAM = new byte[(int) fileLength];
        RAMhex = new String[(int) fileLength];

        try (FileInputStream fileInputStream = new FileInputStream(ramFile)) {
            int bytesRead = fileInputStream.read(RAM);
            if (bytesRead != fileLength) {
                System.err.println("Error reading RAM file");
                return;
            }
        } catch (IOException e) {
            System.err.println("Error reading RAM file: " + e.getMessage());
            return;
        }

        System.out.println("RAM data read successfully.");

        signedToHex();
        writeRAMContents("output.txt");
    }
    
    public static void writeRAMContents(String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            int count = 0;
            for (String data : RAMhex) {
                writer.write(String.valueOf(data));
                writer.newLine();
                // count++;
                // if(count == 7) {
                //     writer.newLine();
                //     count = 0;
                // }
            }
            System.out.println("RAM contents written to " + fileName);
        } catch (IOException e) {
            System.err.println("Error writing RAM contents: " + e.getMessage());
        }
    }

    public static void signedToHex() {
        int a = 0;
        int count = 0;
        RAMhex[a] = "";
        

        for(int i = 0; i < RAM.length; i++) {
            if(count == 8) {
                count = 0;
                a++;
                RAMhex[a] = "";
            }
            count++;
            RAMhex[a] += String.format("%02X", RAM[i]);
        }
    }

    public static int HexToInteger(String hexString){
        hexString = hexString.toLowerCase();

        int result = 0;
        int multiplier = 1;

        for (int i = hexString.length() - 1; i >= 0; i--) {
            char c = hexString.charAt(i);

            if (Character.isDigit(c)) {
                result += (c - '0') * multiplier;
            } else {
                result += (c - 'a' + 10) * multiplier;
            }

            multiplier *= 16;
        }

        return result;
    }
}
