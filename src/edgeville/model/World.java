package edgeville.model;

import com.google.gson.Gson;

import edgeville.Constants;
import edgeville.GameServer;
import edgeville.event.EventHandler;
import edgeville.fs.DefinitionRepository;
import edgeville.fs.MapDefinition;
import edgeville.model.clanchat.ClanChatHandler;
import edgeville.model.entity.Npc;
import edgeville.model.entity.Player;
import edgeville.model.map.MapObj;
import edgeville.net.message.game.encoders.*;
import edgeville.plugin.PluginHandler;
import edgeville.util.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.security.SecureRandom;
import java.util.*;

/**
 * @author Simon on 8/22/2014.
 * Modified by Simon Jacobs on 26/2/2016.
 */
public class World {

    private EntityList<Player> players = new EntityList<>(2048);
    private Map<Object, Player> playerLookupMap = new HashMap<>();
    private Map<String, Player> playerNameLookupMap = new HashMap<>();
    private EntityList<Npc> npcs = new EntityList<>(0xFFFF);
    private List<GroundItem> groundItems = new LinkedList<>();
    private List<MapObj> spawnedObjs = new LinkedList<>();

    public List<MapObj> getSpawnedObjs() {
        return spawnedObjs;
    }

    private DefinitionRepository definitionRepository;
    private ExamineRepository examineRepository;
    private EquipmentInfo equipmentInfo;
    private GameServer server;
    private int id;
    private String name;
    private Random random = new SecureRandom();
    private final boolean emulation;
    
    private ClanChatHandler clanChatHandler;

    private static PluginHandler pluginHandler = new PluginHandler();

    public static PluginHandler getPluginHandler() {
        return pluginHandler;
    }

    private int combatXpMult;
    private int skillingXpMult;

    public World(GameServer server) {
        this.server = server;
        definitionRepository = new DefinitionRepository(server);
        examineRepository = new ExamineRepository(definitionRepository);
        equipmentInfo = new EquipmentInfo(definitionRepository, new File("data/list/equipment_info.txt"), new File("data/list/renderpairs.txt"), new File("data/list/bonuses.txt"), new File("data/list/weapon_types.txt"), new File("data/list/weapon_speeds.txt"), new File("data/list/equipment_requirements.json"));

        // Acquire some info from config
        /*name = server.config().getString("world.name");
        id = server.config().getInt("world.id");
        emulation = server.config().hasPath("world.emulation") && server.config().getBoolean("world.emulation");
        combatXpMult = server.config().getInt("world.xprate.combat");
        skillingXpMult = server.config().getInt("world.xprate.skilling");*/
        name = Constants.SERVER_NAME;
        id = Constants.WORLD_ID;
        emulation = Constants.WORLD_EMULATION;
        combatXpMult = Constants.COMBAT_XP_RATE_MULTIPLIER;
        skillingXpMult = Constants.SKILLING_XP_RATE_MULTIPLIER;

        // Load npc spawns
        loadNpcSpawns();

        // Load object spawns
        loadObjectSpawns();
        
        this.clanChatHandler = new ClanChatHandler();
    }
    
    

    public ClanChatHandler getClanChatHandler() {
		return clanChatHandler;
	}



	private void loadObjectSpawns() {
        Gson gson = new Gson();
        File spawns = new File("data/map/objects");
        for (File spawn : spawns.listFiles()) {
            if (spawn.getName().endsWith(".json")) {
                try {
                    ObjectSpawn[] s = gson.fromJson(new FileReader(spawn), ObjectSpawn[].class);
                    for (ObjectSpawn sp : s) {
                        Tile tile = new Tile(sp.x, sp.z, sp.level);
                        MapObj mapObj = new MapObj(tile, sp.id, 10, sp.rot);
                        spawnObj(mapObj);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void loadNpcSpawns() {
        Gson gson = new Gson();
        File spawns = new File("data/map/npcs");
        for (File spawn : spawns.listFiles()) {
            if (spawn.getName().endsWith(".json")) {
                try {
                    NpcSpawn[] s = gson.fromJson(new FileReader(spawn), NpcSpawn[].class);
                    for (NpcSpawn sp : s) {
                        Tile spawnTile = new Tile(sp.x, sp.z, sp.level);
                        Npc npc = new Npc(sp.id, this, spawnTile, true);
                        npc.spawnDirection(sp.dir());
                        npc.walkRadius(sp.radius);
                        registerNpc(npc);

                        //System.out.println("NPC LOADED: " + npc.def().name);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public int getPlayersOnline() {
        return players().size();
    }

    public int id() {
        return id;
    }

    public boolean emulation() {
        return emulation;
    }

    public int combatMultiplier() {
        return combatXpMult;
    }

    public int skillingMultiplier() {
        return skillingXpMult;
    }

    public boolean registerPlayer(Player player) {
        int slot = players.add(player);

        if (slot == -1)
            return false;

        player.index(slot);
        playerLookupMap.put(player.id(), player);
        playerNameLookupMap.put(player.name().toLowerCase(), player);
        return true;
    }

    public void unregisterPlayer(Player player) {
        players.remove(player);
        playerLookupMap.remove(player.id());
        playerNameLookupMap.remove(player.name().toLowerCase());
    }

    public boolean registerNpc(Npc npc) {
        int slot = npcs.add(npc);

        if (slot == -1)
            return false;

        npc.index(slot);
        //server.scriptRepository().triggerNpcSpawn(npc);
        return true;
    }

    public void unregisterNpc(Npc npc) {
        npcs.remove(npc);
        npc.index(-1);
    }

    private EventHandler eventHandler = new EventHandler();
    public EventHandler getEventHandler() {
        return eventHandler;
    }

    public void cycle() {
        // Ground items which need synching...
        groundItems.stream().filter(g -> !g.broadcasted() && g.shouldBroadcast()).forEach(item -> {
            item.broadcasted(true);

            // See who's getting broadcasted!
            players().forEach(p -> {
                if (!p.equals(item.getOwner()) && p.seesChunk(item.tile().x, item.tile().z)) {
                    p.write(new SetMapBase(p, item.tile()));
                    p.write(new AddGroundItem(item));
                }
            });
        });

        // Ground items which need removal..
        groundItems.stream().filter(GroundItem::shouldBeRemoved).forEach(this::despawnItem);
        groundItems.removeIf(GroundItem::shouldBeRemoved);

        eventHandler.process();
    }

    private void despawnItem(GroundItem item) {
        players().forEach(p -> {
            if (p.seesChunk(item.tile().x, item.tile().z)) {
                p.write(new SetMapBase(p, item.tile()));
                p.write(new RemoveGroundItem(item));
            }
        });
    }

    public Optional<Player> playerForId(Object id) {
        return Optional.ofNullable(playerLookupMap.get(id));
    }

    public Optional<Player> getPlayerByName(String glue) {
        return Optional.ofNullable(playerNameLookupMap.get(glue.toLowerCase()));
    }

    public EntityList<Player> players() {
        return players;
    }

    public EntityList<Npc> npcs() {
        return npcs;
    }

    public GameServer server() {
        return server;
    }

    public DefinitionRepository definitions() {
        return definitionRepository;
    }

    public ExamineRepository examineRepository() {
        return examineRepository;
    }

    public EquipmentInfo equipmentInfo() {
        return equipmentInfo;
    }

    public String name() {
        return name;
    }

    public Random random() {
        return random;
    }

    public int random(int i) {
        return random.nextInt(i + 1);
    }

    public GroundItem getGroundItem(int x, int z, int level, int id) {
        return groundItems.stream().filter(g -> g.item().getId() == id && g.tile().x == x && g.tile().z == z && g.tile().level == level).findAny().orElse(null);
    }

    public boolean groundItemValid(GroundItem item) {
        return groundItems.contains(item);
    }

    public boolean removeGroundItem(GroundItem item) {
        boolean b = groundItems.remove(item);
        despawnItem(item);
        return b;
    }

    public void spawnGroundItem(GroundItem item) {
        groundItems.add(item);

        players.forEach(p -> {
            if (p.activeArea().contains(item.tile())) {
                // Is this an item for us?
                if (item.getOwner() == null || p.equals(item.getOwner()) || item.broadcasted()) {
                    p.write(new SetMapBase(p, item.tile()));
                    p.write(new AddGroundItem(item));
                }
            }
        });
    }

    public void spawnObj(MapObj obj) {
        spawnedObjs.add(obj);

        players.forEach(p -> {
            if (p.activeArea().contains(obj.tile())) {
                p.write(new SetMapBase(p, obj.tile()));
                p.write(new SpawnObject(obj));
            }
        });
    }

    public void removeObjSpawn(MapObj obj) {
        spawnedObjs.remove(obj);

        MapObj original = objByType(obj.type(), obj.tile().x, obj.tile().z, obj.tile().level);
        if (original != null) {
            players.forEach(p -> {
                if (p.activeArea().contains(original.tile())) {
                    p.write(new SetMapBase(p, original.tile()));
                    p.write(new SpawnObject(original));
                }
            });
        } else {
            throw new RuntimeException("not implemented: cannot remove obj that had no predecessor");
        }
    }

    public void tileGraphic(int id, Tile tile, int height, int delay) {
        players.forEach(p -> {
            if (p.activeArea().contains(tile)) {
                p.write(new SetMapBase(p, tile));
                p.write(new SendTileGraphic(id, tile, height, delay));
            }
        });
    }

    public void syncMap(Player player, Area previousMap) {
        Area active = player.activeArea();
        for (int x = active.x1(); x < active.x2(); x += 8) {
            for (int z = active.z1(); z < active.z2(); z += 8) {
                if (previousMap == null || !previousMap.contains(new Tile(x, z))) {
                    syncChunk(player, x, z);
                }
            }
        }
    }

    private void syncChunk(Player p, int x, int z) {
        Area area = new Area(x, z, x + 7, z + 7);
        for (GroundItem item : groundItems) { // Todo check for ownership
            if (item != null && area.contains(item.tile())) {
                // Is this an item for us?
                if (!p.equals(item.getOwner()) && !item.broadcasted()) // Not ours, and not public yet. Bye.
                    continue;
                p.write(new SetMapBase(p, item.tile()));
                p.write(new AddGroundItem(item));
            }
        }
        for (MapObj obj : spawnedObjs) {
            if (obj != null && area.contains(obj.tile())) {
                p.write(new SetMapBase(p, obj.tile()));
                p.write(new SpawnObject(obj));
            }
        }
    }

    public void spawnProjectile(Tile from, Entity to, int gfx, int startHeight, int endHeight, int delay, int lifetime, int angle, int steepness) {
        players.forEachWithinDistance(from, 15, p -> { // TODO how far? viewport = 14 max..
            Tile base = p.activeMap();
            int relx = from.x - base.x;
            int relz = from.z - base.z;

            Tile origin = new Tile(relx % 8, relz % 8);
            p.write(new SetMapBase(relx / 8 * 8, relz / 8 * 8));
            p.write(new FireProjectile(origin, to, gfx, startHeight, endHeight, delay, lifetime, angle, steepness));
        });
    }

    public MapObj objById(int id, int x, int z, int level) {
        Optional<MapObj> spawned = spawnedObjs.stream().filter(m -> m.id() == id && m.tile().equals(x, z, level)).findAny();
        return spawned.orElseGet(() -> definitionRepository.get(MapDefinition.class, Tile.coordsToRegion(x, z)).objById(level, x & 63, z & 63, id));
    }

    public MapObj objByType(int type, int x, int z, int level) {
        Optional<MapObj> spawned = spawnedObjs.stream().filter(m -> m.type() == type && m.tile().equals(x, z, level)).findAny();
        return spawned.orElseGet(() -> definitionRepository.get(MapDefinition.class, Tile.coordsToRegion(x, z)).objByType(level, x & 63, z & 63, type));
    }

    public int[][] clipAround(Tile base, int radius) {
        Tile src = base.transform(-radius, -radius, 0);
        return clipSquare(src, radius * 2 + 1);
    }

    public int[][] clipSquare(Tile base, int size) {
        int[][] clipping = new int[size][size];

        MapDefinition active = definitionRepository.get(MapDefinition.class, base.region());
        int activeId = base.region();

        for (int x = base.x; x < base.x + size; x++) {
            for (int z = base.z; z < base.z + size; z++) {
                int reg = Tile.coordsToRegion(x, z);
                if (reg != activeId) {
                    activeId = reg;
                    active = definitionRepository.get(MapDefinition.class, activeId);
                }

                if (active != null)
                    clipping[x - base.x][z - base.z] = active.masks[base.level][x & 63][z & 63]; // TODO this has -1 AOOBE
            }
        }

        return clipping;
    }

    public Tile randomTileAround(Tile base, int radius) {
        int[][] clip = clipSquare(base.transform(-radius, -radius, 0), radius * 2 + 1);

        for (int i = 0; i < 100; i++) {
            int x = random.nextInt(radius * 2 + 1), z = random.nextInt(radius * 2 + 1);
            if (clip[x][z] == 0) {
                return base.transform(x - radius, z - radius, 0);
            }
        }

        return base;
    }

}
