package edgeville.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edgeville.fs.DefinitionRepository;
import edgeville.fs.ItemDefinition;
import edgeville.fs.ObjectDefinition;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * @author Simon on 8/9/2015.
 *
 * Holds all the examines we gathered.
 */
public class ExamineRepository {

	private static final Logger logger = LogManager.getLogger(ExamineRepository.class);

	private String[] items;
	private String[] objects;

	public ExamineRepository(DefinitionRepository defrepo) {
		items = new String[defrepo.total(ItemDefinition.class)];
		objects = new String[defrepo.total(ObjectDefinition.class)];

		loadItems("data/examine/item_examines.txt", items);
		loadItems("data/examine/object_examines.txt", objects);

		logger.info("Loaded {} item examines.", items.length);
		logger.info("Loaded {} object examines.", objects.length);
	}

	private void loadItems(String path, String[] out) {
		try {
			Scanner scanner = new Scanner(new File(path));
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				int id = Integer.parseInt(line.substring(0, line.indexOf(':')));
				String examine = line.substring(line.indexOf(':') + 1);
				out[id] = examine;
			}
		} catch (Exception e) {
			logger.error("Could not item examines.", e);
		}
	}

	public String item(int id) {
		if (id < 0 || id >= items.length)
			return "Something.";

		String examine = items[id];
		return examine == null ? "Something." : examine;
	}

	public String object(int id) {
		if (id < 0 || id >= objects.length)
			return "Something.";

		String examine = objects[id];
		return examine == null ? "Something." : examine;
	}

}
