package edgeville.util.map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Simon on 8/23/2014.
 *
 */
public class MapDecryptionKeys {

	private static Map<Integer, int[]> keys = new HashMap<>();
	private static final Logger logger = LogManager.getLogger(MapDecryptionKeys.class);

	public static void load(File from) throws IOException {
		/*if (!from.exists()) {
			logger.warn("Map decryption key file does not exist. Decryption keys will be sent as zeroes.");
			return;
		}

		ByteBuffer buffer = ByteBuffer.wrap(Files.readAllBytes(from.toPath()));
		while (buffer.remaining() > 0) {
			int map = buffer.getShort() & 0xFFFF;
			int[] k = new int[4];
			for (int i = 0; i < 4; i++)
				k[i] = buffer.getInt();
			keys.put(map, k);
		}

		logger.info("Loaded {} map decryption key sets.", keys.size());*/
		  File[] files = new File("data/map/xteas").listFiles();
		   for (File file : files) {
			   try(BufferedReader br = new BufferedReader(new FileReader(file))) {
				   	int[] xteas = new int[4];
				    for(int i = 0 ; i < xteas.length;i++) {
				    	xteas[i] = Integer.parseInt(br.readLine());
				    }
				    keys.put(getRegionFromFileName(file.getName()), xteas);
				}
		   }
	}
	
	private static int getRegionFromFileName(String fileName) {
		return Integer.parseInt(fileName.replaceFirst("[.][^.]+$", ""));
	}

	public static int[] get(int region) {
		if (keys.containsKey(region)) {
			return keys.get(region);
		}

		logger.warn("Missing map decryption keys for map {}!", region);
		return new int[4];
	}

	public static void main(String[] args) throws IOException {
		logger.warn("Packing map decryption keys into one file...");

		File dir = new File("temp");
		DataOutputStream dos = new DataOutputStream(new FileOutputStream("data/map/keys.bin"));
		int num = 0;
		for (File f : dir.listFiles()) {
			if (f.getName().endsWith(".txt")) {
				String[] key_s = new String(Files.readAllBytes(f.toPath())).split("\n");
				dos.writeShort(Integer.parseInt(f.getName().substring(0, f.getName().length() - 4)));

				for (int i=0; i<4; i++) {
					dos.writeInt(Integer.parseInt(key_s[i].trim()));
				}

				num++;
			}
		}

		logger.warn("Finished packing a total of {} map decryption keys.", num);
	}

}
