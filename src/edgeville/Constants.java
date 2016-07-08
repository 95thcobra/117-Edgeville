package edgeville;

import edgeville.model.Locations;
import edgeville.model.Tile;
import edgeville.model.uid.UIDProvider;
import edgeville.model.uid.providers.SimpleUIDProvider;
import edgeville.services.serializers.JSONFileSerializer;
import edgeville.services.serializers.PlayerSerializer;

/**
 * Created by Sky on 28-6-2016.
 */
public class Constants {
	
	// Settings
	public static final boolean ALL_PVP = true;
	
	public static final String SERVER_NAME = "Edgeville";
	public static final int REVISION = 117;
	public static final boolean FORCE_REVISION = false;
	public static final String CACHE_DIR = "./data/filestore";
	public static final String MAP_KEYS_DIR = "./data/map/keys.bin";

	// Netty config
	public static final int PORT = 43594;
	public static final String IP_ADDRESS = "0.0.0.0";
	public static final int ACCEPT_THREADS = 1;
	public static final int IO_THREADS = 2;

	// UID Provider
	public static final Class UID_PROVIDER = SimpleUIDProvider.class;

	// Lazy load definitions
	public static final boolean LAZY_DEFINITIONS = true;
	public static final Tile SPAWN_TILE = Locations.EDGEVILLE.getTile();

	public static final int WORLD_ID = 1;
	public static final boolean WORLD_EMULATION = true;
	public static final int COMBAT_XP_RATE_MULTIPLIER = 1;
	public static final int SKILLING_XP_RATE_MULTIPLIER = 1;

	public static final boolean DROP_ITEMS_ON_DEATH = false;
}
