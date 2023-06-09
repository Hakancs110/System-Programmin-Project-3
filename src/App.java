import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

class T {
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

    public int getTime() {
        return time;
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

    public String getData() {
        return data;
    }

}

class Cache {
    public int sets;
    public int lines;
    public int blocksize;
    public int sets_bits;
    public String tagTemp;
    public final int address_size = 32;
    public int hits_counter = 0;
    public int misses_counter = 0;
    public int evictions_counter = 0;
    public T[][] cacheObject = null; // create the structure of the cache
    public int i = 0;
    public int j = 0;
    public int time = 0;
    public boolean isHitBool = false;
    public boolean isMissBool = false;
    public boolean isEvictBool = false;

    Cache(int sets_bits, int lines, int blocksize_bits) {
        this.sets_bits = sets_bits;
        this.sets = (int) Math.pow(2, sets_bits); // S = 2^s
        this.lines = lines;
        this.blocksize = (int) Math.pow(2, blocksize_bits); // B = 2^b
        tagTemp = Integer.toString(address_size - sets_bits - blocksize_bits);

        cacheObject = new T[sets][lines];
    }

    public int writeCacheContents(String address, String data) {
        String addressBinary = Integer.toBinaryString(App.HexToInteger(address));
        addressBinary = String.format("%32s", addressBinary).replaceAll(" ", "0");
        // System.out.printf("address = %s\n", addressBinary);
        i = (sets_bits == 0 ? 0
                : Integer.parseInt(
                        addressBinary.substring(Integer.parseInt(tagTemp), Integer.parseInt(tagTemp) + sets_bits), 2));
        String tagIndex = addressBinary.substring(0, Integer.parseInt(tagTemp));
        // System.out.println("Tag index = " + tagIndex);

        boolean isHit = false;
        int hitLine = -1;

        // Check for hit within the set
        for (int line = 0; line < lines; line++) {
            if (cacheObject[i][line] != null && cacheObject[i][line].getTag().equals(tagIndex)) {
                // Hit found
                isHit = true;
                hitLine = line;
                break;
            }
        }

        if (isHit) {
            isHitBool = true;
            hits_counter++;
            // If hit is encountred, update cache entry and increment time
            cacheObject[i][hitLine].setData(data);
            cacheObject[i][hitLine].setTime(time++);

            // what should we print here for the instruction if hit is encountred?
        } else {
            // if miss is encountred, find empty line, or if cache is full, evict using FIFO
            misses_counter++;
            time++;
            int emptyLine = -1;
            for (int line = 0; line < lines; line++) {
                if (cacheObject[i][line] == null) {
                    emptyLine = line;
                    break;
                }
            }

            if (emptyLine != -1) {
                // Store data in an empty line
                T temp = new T(1, time, data, tagIndex);
                cacheObject[i][emptyLine] = temp;
                isMissBool = true;
            } else {
                // if cache is full, evict using FIFO
                evictions_counter++;
                isEvictBool = true;
                int oldestLine = 0;
                int oldestTime = cacheObject[i][0].getTime();
                for (int line = 1; line < lines; line++) {
                    if (cacheObject[i][line].getTime() < oldestTime) {
                        oldestLine = line;
                        oldestTime = cacheObject[i][line].getTime();
                    }
                }

                // Evict the oldest entry and store the new data
                T temp = new T(1, time, data, tagIndex);
                cacheObject[i][oldestLine] = temp;
                //System.out.println("  Cache full! Data evicted from set " + i + ", line " + oldestLine);
                //System.out.println("  New data stored in set " + i + ", line " + oldestLine);
            }
        }
        return i;
    }

    public void getT() {
        for (int i = 0; i < sets; i++) {
            for (int j = 0; j < lines; j++) {
                if (cacheObject[i][j] != null) {
                    System.out.println(cacheObject[i][j].getTag() + " " + cacheObject[i][j].getTime() + " "
                            + cacheObject[i][j].getValid() + " " + " " + cacheObject[i][j].getData());
                }
            }
        }
    }
}

public class App {
    public static byte[] RAM;
    public static String[] RAMhex;

    public static void main(String[] args) throws Exception {
        // Check for the length of args
        // if(args.length != 15) {
        // System.out.println("Missing arguments!");
        // System.exit(1);
        // }

        readRAM();

        // test for the args
        // String[] test = {"-L1s", "0", "-L1E", "2" , "-L1b","3",
        // "-L2s","1" ,"-L2E", "2", "-L2b", "3",
        // "-t", "t.trace"};

        int l1s = 0;
        int l1E = 0;
        int l1b = 0;

        int l2s = 0;
        int l2E = 0;
        int l2b = 0;
        String tracefilename = "";
        // args = test;

        int args_length = args.length;
        // read the args
        for (int i = 0; i < args_length; i += 2) {
            switch (args[i]) {
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

        // create the caches
        Cache L1I = new Cache(l1s, l1E, l1b);

        Cache L1D = new Cache(l1s, l1E, l1b);

        Cache L2 = new Cache(l2s, l2E, l2b);

        // read the trace file
        FileReader tracefile = new FileReader(new File(tracefilename));
        BufferedReader br = new BufferedReader(tracefile);
        String line = null;
        String traces = br.readLine() + " ";

        while ((line = br.readLine()) != null) {
            traces += line + " ";
        }
        br.close();
        tracefile.close();

        // parse the trace file
        // System.out.println(traces);
        traces = traces.replaceAll(",", "");
        String[] trace = traces.split(" ");

        int trace_length = trace.length;
        for (int i = 0; i < trace_length; i++) {
            // System.out.println(trace[i]);
            if (trace[i].equals("I")) {
                String address = trace[i + 1];
                int addressIndex = Integer.parseInt(address, 16) / 8;
                String data = RAMhex[addressIndex];
                String size = trace[i + 2];
                System.out.printf("%s %s, %s\n", trace[i], address, size);

                
                if(L1I.blocksize == 8)
                	data = RAMhex[addressIndex];
                else if(L1I.blocksize == 4)
                	data = data.substring(8, data.length());
                else if(L1I.blocksize == 2)
                	data = data.substring(12, data.length());
                
                L1I.writeCacheContents(address, data);
                if (L1I.isHitBool) {
                    System.out.print("  L1I hit, ");
                    L1I.isHitBool = false; // reset it to false
                } else if (L1I.isMissBool) {
                    System.out.print("  L1I miss, ");
                    L1I.isMissBool = false; // reset it to false
                } else if (L1I.isEvictBool) {
                    System.out.print("  L1I evict, ");
                    L1I.isEvictBool = false; // reset it to false
                }
                
                if(L2.blocksize == 8)
                	data = RAMhex[addressIndex];
                else if(L2.blocksize == 4)
                	data = data.substring(8, data.length());
                else if(L2.blocksize == 2)
                	data = data.substring(12, data.length());
                
                int s = L2.writeCacheContents(address, data);
                if (L2.isHitBool) {
                    System.out.print("L2 hit\n");
                    L2.isHitBool = false; // reset it to false
                    // what should we print here as an explanation??
                } else if (L2.isMissBool) {
                    System.out.print("L2 miss\n");
                    L2.isMissBool = false; // reset it to false
                    System.out.printf("  Place in L2 set %d, L1I\n", s);
                } else if (L2.isEvictBool) {
                    L2.isEvictBool = false; // reset it to false
                    System.out.print("L2 evict\n");
                }
                i += 2;

            } else if (trace[i].equals("L")) {
                String address = trace[i + 1];
                int addressIndex = Integer.parseInt(address, 16) / 8;
                String data = RAMhex[addressIndex];
                String size = trace[i + 2];

                System.out.println("\nL" + " " + address + "," + size);

                
                if(L1D.blocksize == 8)
                	data = RAMhex[addressIndex];
                else if(L1D.blocksize == 4) 
                	data = data.substring(8, data.length());
                else if(L1D.blocksize == 2)
                	data = data.substring(12, data.length());
                
                L1D.writeCacheContents(address, data);

                if (L1D.isHitBool) {
                    System.out.print("  L1D hit, ");
                    L1D.isHitBool = false; // reset it to false
                } else if (L1D.isMissBool) {
                    System.out.print("  L1D miss, ");
                    L1D.isMissBool = false; // reset it to false
                } else if (L1D.isEvictBool) {
                    System.out.print("  L1D evict, ");
                    L1D.isEvictBool = false; // reset it to false
                }
                
                if(L2.blocksize == 8)
                	data = RAMhex[addressIndex];
                else if(L2.blocksize == 4)
                	data = data.substring(8, data.length());
                else if(L2.blocksize == 2)
                	data = data.substring(12, data.length());
                
                int s = L2.writeCacheContents(address, data);
                // System.out.println("L2:");
                // L2.getT();

                if (L2.isHitBool) {
                    System.out.print("L2 hit\n");
                    L2.isHitBool = false; // reset it to false
                    // what should we print here as an explanation??
                } else if (L2.isMissBool) {
                    System.out.print("L2 miss\n");
                    L2.isMissBool = false; // reset it to false
                    System.out.printf("  Place in L2 set %d, L1D\n", s);
                } else if (L2.isEvictBool) {
                    L2.isEvictBool = false; // reset it to false
                    System.out.println("L2 evict\n");
                }
                i += 2;

            } else if (trace[i].equals("M")) {
                String address = trace[i + 1];
                int addressIndex = Integer.parseInt(address, 16) / 8;

                String data = RAMhex[addressIndex];
                int size = Integer.parseInt(trace[i + 2]);

                String olddata = data.substring(size * 2, data.length());

                String traceData = trace[i + 3];
                // System.out.println(data);

                System.out.println("\nM" + " " + address + "," + size + ", " + traceData);
                L1D.writeCacheContents(address, data);
                // System.out.println("----------L1D:");
                // L2.getT();

                if (L1D.isHitBool) {
                    System.out.print("  L1D hit, ");
                    L1D.isHitBool = false; // reset it to false
                } else if (L1D.isMissBool) {
                    System.out.print("  L1D miss, ");
                    L1D.isMissBool = false; // reset it to false
                } else if (L1D.isEvictBool) {
                    System.out.print("  L1D evict, ");
                    L1D.isEvictBool = false; // reset it to false
                }

                // System.out.println("L2:");
                int s = L2.writeCacheContents(address, data);
                // L2.getT();

                if (L2.isHitBool) {
                    System.out.println("L2 hit\n");
                    L2.isHitBool = false; // reset it to false
                    // what should we print here as an explanation??
                } else if (L2.isMissBool) {
                    System.out.print("L2 miss\n");
                    L2.isMissBool = false; // reset it to false
                    System.out.printf("  Place in L2 set %d, L1D\n", s);
                } else if (L2.isEvictBool) {
                    L2.isEvictBool = false; // reset it to false
                    System.out.println("L2 evict\n");
                }

                data = traceData + olddata;
                RAMhex[addressIndex] = data;
                i += 3;
            }

            else if (trace[i].equals("S")) {
                String address = trace[i + 1];
                int addressIndex = Integer.parseInt(address, 16) / 8;
                String data = RAMhex[addressIndex];

                int size = Integer.parseInt(trace[i + 2]);

                String olddata = data.substring(size * 2, data.length());

                System.out.println();
                String traceData = trace[i + 3];
                data = traceData + olddata;
                RAMhex[addressIndex] = data;

                // System.out.println(data);

                System.out.println("\nS" + " " + address + "," + size + ", " + traceData);
                L1D.writeCacheContents(address, data);
                // System.out.println("----------L1D:");
                // L2.getT();

                if (L1D.isHitBool) {
                    System.out.print("  L1D hit, ");
                    L1D.isHitBool = false; // reset it to false
                } else if (L1D.isMissBool) {
                    System.out.print("  L1D miss, ");
                    L1D.isMissBool = false; // reset it to false
                } else if (L1D.isEvictBool) {
                    System.out.print("  L1D evict, ");
                    L1D.isEvictBool = false; // reset it to false
                }

                // System.out.println("L2:");
                int s = L2.writeCacheContents(address, data);
                // L2.getT();

                if (L2.isHitBool) {
                    System.out.print("L2 hit\n");
                    L2.isHitBool = false; // reset it to false
                    // what should we print here as an explanation??
                } else if (L2.isMissBool) {
                    System.out.print("L2 miss\n");
                    L2.isMissBool = false; // reset it to false
                    System.out.printf("  Place in L2 set %d, L1D\n", s);
                } else if (L2.isEvictBool) {
                    L2.isEvictBool = false; // reset it to false
                    System.out.print("L2 evict\n");
                }

                i += 3;
            }
            writeRAMContents("output.txt");
        }
        System.out.printf("L1I-hits:%d L1I-misses:%d L1I-evicitons:%d\n", L1I.hits_counter, L1I.misses_counter,
                L1I.evictions_counter);
        System.out.printf("L1D-hits:%d L1D-misses:%d L1D-evicitons:%d\n", L1D.hits_counter, L1D.misses_counter,
                L1D.evictions_counter);
        System.out.printf("L2-hits:%d L2-misses:%d L2-evicitons:%d\n", L2.hits_counter, L2.misses_counter,
                L2.evictions_counter);

        System.out.println("Final Cache States:");
        System.out.println("L1I:");
        L1I.getT();
        System.out.println("L1D:");
        L1D.getT();
        System.out.println("L2:");
        L2.getT();
    }

    public static void readRAM() {
        File ramFile = new File("RAM.dat");
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

        // System.out.println("RAM data read successfully.");

        signedToHex();
        writeRAMContents("output.txt");
    }

    public static void writeRAMContents(String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (String data : RAMhex) {
                writer.write(String.valueOf(data));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing RAM contents: " + e.getMessage());
        }
    }

    public static void signedToHex() {
        int a = 0;
        int count = 0;
        RAMhex[a] = "";

        for (int i = 0; i < RAM.length; i++) {
            if (count == 8) {
                count = 0;
                a++;
                RAMhex[a] = "";
            }
            count++;
            RAMhex[a] += String.format("%02X", RAM[i]);
        }
    }

    public static int HexToInteger(String hexString) {
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