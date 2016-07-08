package edgeville.fs;

import nl.bartpelle.dawnguard.DataStore;
import nl.bartpelle.dawnguard.util.Compression;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edgeville.Constants;
import edgeville.GameServer;
import edgeville.util.map.MapDecryptionKeys;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Simon on 7/11/2015.
 */
@SuppressWarnings("unchecked") public class DefinitionRepository {

	private static final Logger logger = LogManager.getLogger(DefinitionRepository.class);

	private Map<Class<? extends Definition>, Definition[]> definitionMaps = new HashMap<>();
	private DataStore store;

	public DefinitionRepository(GameServer server) {

		logger.info("Loading definition repository...");
		store = server.store();
		boolean lazy = Constants.LAZY_DEFINITIONS;/*server.config().hasPath("definitions.lazy") && server.config().getBoolean("definitions.lazy");*/

		// Load items
		int numItems = store.getIndex(2).getDescriptor().getLastFileId(10);
		ItemDefinition[] items = new ItemDefinition[numItems];
		definitionMaps.put(ItemDefinition.class, items);

		if (!lazy) {
			for (int id = 0; id < numItems; id++) {
                items[id] = loadDefinition(ItemDefinition.class, id);
            }
		}

        /*try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File("./data/list/equipment_info.txt"), true))) {
            for (int id = 0; id < numItems; id++) {
                writer.write(items[id].name + ":" + items[id].);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/

		// Load npcs
		int numNpcs = store.getIndex(2).getDescriptor().getLastFileId(9);
		NpcDefinition[] npcs = new NpcDefinition[numNpcs];
		definitionMaps.put(NpcDefinition.class, npcs);

		if (!lazy) {
            for (int id = 0; id < numNpcs; id++) {
                npcs[id] = loadDefinition(NpcDefinition.class, id);
            }
        }

		// Load objects
		int numObjects = store.getIndex(2).getDescriptor().getLastFileId(6);
		ObjectDefinition[] objects = new ObjectDefinition[numObjects];
		definitionMaps.put(ObjectDefinition.class, objects);

		if (!lazy) {
            for (int id = 0; id < numObjects; id++) {
                objects[id] = loadDefinition(ObjectDefinition.class, id);
            }
        }

		// Load maps
		int maxMaps = 0xFFFF;
		MapDefinition[] maps = new MapDefinition[maxMaps];
		definitionMaps.put(MapDefinition.class, maps);

		// This definition is a bit... hacky. It's to avoid recursive dependencies.
		if (!lazy) {
			for (int x = 0; x < 255; x++) {
				for (int z = 0; z < 255; z++) {
					int region = (x << 8) | z;

					try {
						int mapId = store.getIndex(5).getDescriptor().getArchiveID("m" + x + "_" + z);
						int landscapeId = store.getIndex(5).getDescriptor().getArchiveID("l" + x + "_" + z);
						if (mapId >= 0 && landscapeId >= 0) {
							maps[region] = new MapDefinition(x, z);
						}
					} catch (Exception ignored) {
						ignored.printStackTrace();
					}
				}
			}

			for (int x = 0; x < 255; x++) {
				for (int z = 0; z < 255; z++) {
					int region = (x << 8) | z;

					try {
						loadDefinition(MapDefinition.class, region);
					} catch (Exception ignored) {
						if (ignored.getMessage() == null || !ignored.getMessage().contains("Error while parsing archive header"))
							ignored.printStackTrace();
					}
				}
			}
		}

		// Load varbits
		int numVarbits = store.getIndex(2).getDescriptor().getLastFileId(14);
		VarbitDefinition[] varbits = new VarbitDefinition[numVarbits];
		definitionMaps.put(VarbitDefinition.class, varbits);

		if (!lazy) {
			for (int id = 0; id < numVarbits; id++) {
				varbits[id] = loadDefinition(VarbitDefinition.class, id);
			}
		}

		// Load enums
		int numEnums = store.getIndex(2).getDescriptor().getLastFileId(8);
		EnumDefinition[] enums = new EnumDefinition[numEnums];
		definitionMaps.put(EnumDefinition.class, enums);

		if (!lazy) {
			for (int id = 0; id < numEnums; id++) {
				enums[id] = loadDefinition(EnumDefinition.class, id);
			}
		}

		logger.info("Loaded {} item definitions.", numItems);
		logger.info("Loaded {} npc definitions.", numNpcs);
		logger.info("Loaded {} object definitions.", numObjects);
		logger.info("Loaded {} varbit definitions.", numVarbits);
		logger.info("Loaded {} enum definitions.", numEnums);
	}

	private <T extends Definition> T loadDefinition(Class<T> type, int id) {
		if (type == ItemDefinition.class) {
			return (T) new ItemDefinition(id, store.getIndex(2).getContainer(10).getFileData(id, true, true));
		} else if (type == VarbitDefinition.class) {
			return (T) new VarbitDefinition(id, store.getIndex(2).getContainer(14).getFileData(id, true, true));
		} else if (type == EnumDefinition.class) {
			return (T) new EnumDefinition(id, store.getIndex(2).getContainer(8).getFileData(id, true, true));
		} else if (type == NpcDefinition.class) {
			return (T) new NpcDefinition(id, store.getIndex(2).getContainer(9).getFileData(id, true, true));
		} else if (type == ObjectDefinition.class) {
			return (T) new ObjectDefinition(id, store.getIndex(2).getContainer(6).getFileData(id, true, true));
		} else if (type == MapDefinition.class) {
			int x = id >> 8;
			int z = id & 0xFF;
			int mapId = store.getIndex(5).getDescriptor().getArchiveID("m" + x + "_" + z);
			int landscapeId = store.getIndex(5).getDescriptor().getArchiveID("l" + x + "_" + z);

			MapDefinition[] arr = (MapDefinition[]) definitionMaps.get(type);

			if (landscapeId != -1 && mapId != -1 && store.getIndex(5).getDescriptor().archiveExists(landscapeId)) {
				try {
					byte[] map = store.getFileDirect(5, mapId, 0);
					byte[] decrypted = store.getEncryptedFileDirect(5, landscapeId, 0, MapDecryptionKeys.get(id));

					if (arr[id] == null)
						arr[id] = new MapDefinition(x, z);

					arr[id].load(this, map, decrypted);
				} catch (Exception e) {
					//logger.error("Could not load map {} {}!", id, landscapeId, e);
				}

				return (T) arr[id];
			}
		}

		return null;
	}

	public <T extends Definition> T get(Class<T> type, int id) {
		T[] arr = (T[]) definitionMaps.get(type);

		if (id < 0 || id >= arr.length)
			return null;

		if (arr[id] == null) {
			return arr[id] = (T) loadDefinition(type, id);
		}

		return ((T[]) definitionMaps.get(type))[id];
	}

	public int total(Class<? extends Definition> type) {
		return definitionMaps.get(type).length;
	}

}
