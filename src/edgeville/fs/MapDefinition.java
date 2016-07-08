package edgeville.fs;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import edgeville.model.Tile;
import edgeville.model.World;
import edgeville.model.map.MapObj;

/**
 * @author Simon on 8/11/2015.
 */
public class MapDefinition implements Definition {

	public int[][][] masks = new int[4][64][64];
	public byte[][][] floors = new byte[4][64][64];
	public List[][][] objs = new List[4][64][64];

	private int rx;
	private int rz;

	public MapDefinition(int rx, int rz) {
		this.rx = rx;
		this.rz = rz;
	}

	public int rx() {
		return rx;
	}

	public int rz() {
		return rz;
	}

	public void load(DefinitionRepository defrepo, byte[] map, byte[] objects) {
		ByteBuffer buffer = ByteBuffer.wrap(map);

		int baseX = rx * 64;
		int baseZ = rz * 64;

		for (int plane = 0; plane < 4; plane++) {
			for (int x = 0; x < 64; x++) {
				for (int y = 0; y < 64; y++) {
					while (true) {
						int var9 = buffer.get() & 0xFF;
						if (var9 == 0) {
							break;
						}

						if (var9 == 1) {
							buffer.get();
							break;
						}

						if (var9 <= 49) {
							buffer.get();
						} else if (var9 <= 81) {
							floors[plane][x][y] = (byte) (var9 - 49);
						}
					}
				}
			}
		}

		for (int plane = 0; plane < 4; plane++) {
			for (int x = 0; x < 64; x++) {
				for (int y = 0; y < 64; y++) {
					if ((floors[plane][x][y] & 0x1) == 1) {
						int realPlane = plane;
						if ((floors[1][x][y] & 2) == 2)
							realPlane--;
						if (realPlane >= 0)
							addFloor(defrepo, realPlane, x, y, true);
					}
				}
			}
		}

		buffer = ByteBuffer.wrap(objects);
		buffer.position(0);

		int id = -1;
		while (true) {
			int idOffset = readCompact(buffer);
			if (idOffset == 0) {
				return;
			}

			id += idOffset;
			int info = 0;

			while (true) {
				int infoDiff = readCompact(buffer);
				if (infoDiff == 0) {
					break;
				}

				info += infoDiff - 1;
				int localZ = info & 63;
				int localX = info >> 6 & 63;
				int level = info >> 12;
				int settings = buffer.get() & 0xFF;
				int type = settings >> 2;
				int rotation = settings & 3;
				int x = localX + 0;
				int z = localZ + 0;

				if (x >= 0 && z >= 0 && x < 64 && z < 64) {
					int targetLevel = level;
					if ((floors[1][x][z] & 2) == 2) {
						targetLevel = level - 1;
					}

					if (targetLevel >= 0) {
						if (objs[targetLevel][x][z] == null)
							objs[targetLevel][x][z] = new LinkedList<MapObj>();
						((List<MapObj>) objs[targetLevel][x][z]).add(new MapObj(new Tile(baseX + x, baseZ + z, targetLevel), id, type, rotation));
					}

					//register(level, x, z, id, rotation, type, var3, var17);
					//System.out.println("Clip for me the " + id);
					try {
						clip(defrepo, id, x, z, targetLevel, type, rotation, true);
					} catch (Exception ignored) {}
				}
			}
		}
	}

	public MapObj objByType(int level, int x, int z, int type) {
		if (objs[level][x][z] != null) {
			for (Object o : objs[level][x][z]) {
				MapObj obj = ((MapObj) o);
				if (obj.type() == type)
					return obj;
			}
		}
		return null;
	}

	public MapObj objById(int level, int x, int z, int id) {
		if (objs[level][x][z] != null) {
			for (Object o : objs[level][x][z]) {
				MapObj obj = ((MapObj) o);
				if (obj.id() == id)
					return obj;
			}
		}
		return null;
	}

	private void clip(DefinitionRepository defrepo, int object, int x, int y, int plane, int type, int rotation, boolean add) {
		// ObjectDefinition objectDefinition =
		// ObjectDefinition.get(CatanaiRS3.ds, object); // load
		//long l = System.currentTimeMillis();

		ObjectDefinition objectDefinition = defrepo.get(ObjectDefinition.class, object);

		//boolean bank = objectDefinition.name != null && (objectDefinition.name.toLowerCase().startsWith("counter") || objectDefinition.name.toLowerCase().startsWith("bank")) || object == 11763;
		if (type == 22 ? objectDefinition.clipType != 1 : objectDefinition.clipType == 0) {
			return;
		}

		if (type >= 0 && type <= 3) {
			// if (!objectDefinition.ignoreClipOnAlternativeRoute ||
			// objectDefinition.projectileClipped)
			//if (objectDefinition.sizeX > 1 || objectDefinition.sizeY > 1)
			//	System.out.println("wall at " + x + ", " + y + " rotated " + rotation);
			//if (rx == 12084 >> 8 && ry == (12084 & 0xFF)) {
			//}

			// TODO support sized walls!
			addWall(defrepo, plane, x, y, type, rotation, objectDefinition.projectileClipped, !objectDefinition.unclipped, add);
		} else if (type >= 9 && type <= 21/* || type == 4 */) {
			int sizeX;
			int sizeY;
			if (rotation != 1 && rotation != 3) {
				sizeX = objectDefinition.sizeX;
				sizeY = objectDefinition.sizeY;
			} else {
				sizeX = objectDefinition.sizeY;
				sizeY = objectDefinition.sizeX;
			}
			// System.out.println("x="+sizeX + ", y=" + sizeY);
			addObject(defrepo, plane, x, y, sizeX, sizeY, objectDefinition.projectileClipped, !objectDefinition.unclipped, add);
			// if (objectDefinition.isProjectileCliped())// TODO same as aove i
			// am black
			// addObject(plane, x, y, sizeX, sizeY,
			// objectDefinition.isProjectileCliped(),
			// !objectDefinition.ignoreClipOnAlternativeRoute);
		} else if (type == 22) {
			addFloor(defrepo, plane, x, y, add);
		}
	}

	public void clipTile(DefinitionRepository repo, int plane, int x, int y, boolean add) {
		addMask(repo, plane, x, y, 2097152, add);
	}

	public void addFloor(DefinitionRepository repository, int plane, int x, int y, boolean add) {
		addMask(repository, plane, x, y, 0x40000, add);
	}

	public void removeFloor(int plane, int x, int y) {
		removeMask(plane, x, y, 262144);
	}

	public void addObject(DefinitionRepository repository, int plane, int x, int y, int sizeX, int sizeY, boolean solid, boolean notAlternative, boolean add) {
		int mask = 256;
		if (solid) {
			mask |= 0x20000;
		}
		if (notAlternative) {
			mask |= 0x40000000;
		}
		for (int tileX = x; tileX < x + sizeX; tileX++) {
			for (int tileY = y; tileY < y + sizeY; tileY++) {
				addMask(repository, plane, tileX, tileY, mask, add);
			}
		}
	}

	public void removeObject(int plane, int x, int y, int sizeX, int sizeY, boolean solid, boolean notAlternative) {
		int mask = 256;
		if (solid)
			mask |= 131072;
		if (notAlternative)
			mask |= 1073741824;
		for (int tileX = x; tileX < x + sizeX; tileX++)
			for (int tileY = y; tileY < y + sizeY; tileY++)
				removeMask(plane, tileX, tileY, mask);

	}

	public void addWall(DefinitionRepository repository, int plane, int x, int y, int type, int rotation, boolean solid, boolean notAlternative, boolean add) {
		if (type == 0) {
			if (rotation == 0) {
				addMask(repository, plane, x, y, 128, add);
				addMask(repository, plane, x - 1, y, 8, add);
			} else if (rotation == 1) {
				addMask(repository, plane, x, y, 2, add);
				addMask(repository, plane, x, 1 + y, 32, add);
			} else if (rotation == 2) {
				addMask(repository, plane, x, y, 8, add);
				addMask(repository, plane, 1 + x, y, 128, add);
			} else if (rotation == 3) {
				addMask(repository, plane, x, y, 32, add);
				addMask(repository, plane, x, -1 + y, 2, add);
			}
		} else if (type == 1 || type == 3) {
			if (rotation == 0) {
				addMask(repository, plane, x, y, 1, add);
				addMask(repository, plane, -1 + x, 1 + y, 16, add);
			} else if (rotation == 1) {
				addMask(repository, plane, x, y, 4, add);
				addMask(repository, plane, 1 + x, 1 + y, 64, add);
			} else if (rotation == 2) {
				addMask(repository, plane, x, y, 16, add);
				addMask(repository, plane, x + 1, -1 + y, 1, add);
			} else if (rotation == 3) {
				addMask(repository, plane, x, y, 64, add);
				addMask(repository, plane, x - 1, -1 + y, 4, add);
			}
		} else if (type == 2) {
			if (rotation == 0) {
				addMask(repository, plane, x, y, 130, add);
				addMask(repository, plane, -1 + x, y, 8, add);
				addMask(repository, plane, x, y + 1, 32, add);
			} else if (rotation == 1) {
				addMask(repository, plane, x, y, 10, add);
				addMask(repository, plane, x, 1 + y, 32, add);
				addMask(repository, plane, 1 + x, y, 128, add);
			} else if (rotation == 2) {
				addMask(repository, plane, x, y, 40, add);
				addMask(repository, plane, 1 + x, y, 128, add);
				addMask(repository, plane, x, -1 + y, 2, add);
			} else if (rotation == 3) {
				addMask(repository, plane, x, y, 160, add);
				addMask(repository, plane, x, -1 + y, 2, add);
				addMask(repository, plane, -1 + x, y, 8, add);
			}
		}

		if (solid) {
			if (type == 0) {
				if (rotation == 0) {
					addMask(repository, plane, x, y, 0x10000, add);
					addMask(repository, plane, x - 1, y, 4096, add);
				} else if (rotation == 1) {
					addMask(repository, plane, x, y, 1024, add);
					addMask(repository, plane, x, 1 + y, 16384, add);
				} else if (rotation == 2) {
					addMask(repository, plane, x, y, 4096, add);
					addMask(repository, plane, x + 1, y, 0x10000, add);
				} else if (rotation == 3) {
					addMask(repository, plane, x, y, 16384, add);
					addMask(repository, plane, x, -1 + y, 1024, add);
				}
			} else if (type == 1 || type == 3) {
				if (rotation == 0) {
					addMask(repository, plane, x, y, 512, add);
					addMask(repository, plane, x - 1, y + 1, 8192, add);
				} else if (rotation == 1) {
					addMask(repository, plane, x, y, 2048, add);
					addMask(repository, plane, x + 1, 1 + y, 32768, add);
				} else if (rotation == 2) {
					addMask(repository, plane, x, y, 8192, add);
					addMask(repository, plane, x + 1, y - 1, 512, add);
				} else if (rotation == 3) {
					addMask(repository, plane, x, y, 32768, add);
					addMask(repository, plane, x - 1, -1 + y, 2048, add);
				}
			} else if (type == 2) {
				if (rotation == 0) {
					addMask(repository, plane, x, y, 0x10400, add);
					addMask(repository, plane, -1 + x, y, 4096, add);
					addMask(repository, plane, x, y + 1, 16384, add);
				} else if (rotation == 1) {
					addMask(repository, plane, x, y, 5120, add);
					addMask(repository, plane, x, y + 1, 16384, add);
					addMask(repository, plane, 1 + x, y, 0x10000, add);
				} else if (rotation == 2) {
					addMask(repository, plane, x, y, 20480, add);
					addMask(repository, plane, x + 1, y, 0x10000, add);
					addMask(repository, plane, x, y - 1, 1024, add);
				} else if (rotation == 3) {
					addMask(repository, plane, x, y, 0x14000, add);
					addMask(repository, plane, x, -1 + y, 1024, add);
					addMask(repository, plane, x - 1, y, 4096, add);
				}
			}
		}

		if (notAlternative) {
			if (type == 0) {
				if (rotation == 0) {
					addMask(repository, plane, x, y, 0x20000000, add);
					addMask(repository, plane, x - 1, y, 0x2000000, add);
				} else if (rotation == 1) {
					addMask(repository, plane, x, y, 0x800000, add);
					addMask(repository, plane, x, y + 1, 0x8000000, add);
				} else if (rotation == 2) {
					addMask(repository, plane, x, y, 0x2000000, add);
					addMask(repository, plane, x + 1, y, 0x20000000, add);
				} else if (rotation == 3) {
					addMask(repository, plane, x, y, 0x8000000, add);
					addMask(repository, plane, x, y - 1, 0x800000, add);
				}
			} else if (type == 1 || type == 3) {
				if (rotation == 0) {
					addMask(repository, plane, x, y, 0x400000, add);
					addMask(repository, plane, x - 1, y + 1, 0x4000000, add);
				} else if (rotation == 1) {
					addMask(repository, plane, x, y, 0x1000000, add);
					addMask(repository, plane, 1 + x, 1 + y, 0x10000000, add);
				} else if (rotation == 2) {
					addMask(repository, plane, x, y, 0x4000000, add);
					addMask(repository, plane, x + 1, -1 + y, 0x400000, add);
				} else if (rotation == 3) {
					addMask(repository, plane, x, y, 0x10000000, add);
					addMask(repository, plane, -1 + x, y - 1, 0x1000000, add);
				}
			} else if (type == 2) {
				if (rotation == 0) {
					addMask(repository, plane, x, y, 0x20800000, add);
					addMask(repository, plane, -1 + x, y, 0x2000000, add);
					addMask(repository, plane, x, 1 + y, 0x8000000, add);
				} else if (rotation == 1) {
					addMask(repository, plane, x, y, 0x2800000, add);
					addMask(repository, plane, x, 1 + y, 0x8000000, add);
					addMask(repository, plane, x + 1, y, 0x20000000, add);
				} else if (rotation == 2) {
					addMask(repository, plane, x, y, 0xa000000, add);
					addMask(repository, plane, 1 + x, y, 0x20000000, add);
					addMask(repository, plane, x, y - 1, 0x800000, add);
				} else if (rotation == 3) {
					addMask(repository, plane, x, y, 0x28000000, add);
					addMask(repository, plane, x, y - 1, 0x800000, add);
					addMask(repository, plane, -1 + x, y, 0x2000000, add);
				}
			}
		}
	}

	public void removeWall(DefinitionRepository repository, int plane, int x, int y, int type, int rotation, boolean solid, boolean notAlternative) {
		if (type == 0) {
			if (rotation == 0) {
				removeMask(plane, x, y, 128);
				removeMask(plane, x - 1, y, 8);
			}
			if (rotation == 1) {
				removeMask(plane, x, y, 2);
				removeMask(plane, x, 1 + y, 32);
			}
			if (rotation == 2) {
				removeMask(plane, x, y, 8);
				removeMask(plane, 1 + x, y, 128);
			}
			if (rotation == 3) {
				removeMask(plane, x, y, 32);
				removeMask(plane, x, -1 + y, 2);
			}
		}
		if (type == 1 || type == 3) {
			if (rotation == 0) {
				removeMask(plane, x, y, 1);
				removeMask(plane, -1 + x, 1 + y, 16);
			}
			if (rotation == 1) {
				removeMask(plane, x, y, 4);
				removeMask(plane, 1 + x, 1 + y, 64);
			}
			if (rotation == 2) {
				removeMask(plane, x, y, 16);
				removeMask(plane, x + 1, -1 + y, 1);
			}
			if (rotation == 3) {
				removeMask(plane, x, y, 64);
				removeMask(plane, x - 1, -1 + y, 4);
			}
		}
		if (type == 2) {
			if (rotation == 0) {
				addMask(repository, plane, x, y, 130, true);
				removeMask(plane, -1 + x, y, 8);
				removeMask(plane, x, y + 1, 32);
			}
			if (rotation == 1) {
				removeMask(plane, x, y, 10);
				removeMask(plane, x, 1 + y, 32);
				removeMask(plane, 1 + x, y, 128);
			}
			if (rotation == 2) {
				removeMask(plane, x, y, 40);
				removeMask(plane, 1 + x, y, 128);
				removeMask(plane, x, -1 + y, 2);
			}
			if (rotation == 3) {
				removeMask(plane, x, y, 160);
				removeMask(plane, x, -1 + y, 2);
				removeMask(plane, -1 + x, y, 8);
			}
		}
		if (solid) {
			if (type == 0) {
				if (rotation == 0) {
					removeMask(plane, x, y, 0x10000);
					removeMask(plane, x - 1, y, 4096);
				}
				if (rotation == 1) {
					removeMask(plane, x, y, 1024);
					removeMask(plane, x, 1 + y, 16384);
				}
				if (rotation == 2) {
					removeMask(plane, x, y, 4096);
					removeMask(plane, x + 1, y, 0x10000);
				}
				if (rotation == 3) {
					removeMask(plane, x, y, 16384);
					removeMask(plane, x, -1 + y, 1024);
				}
			}
			if (type == 1 || type == 3) {
				if (rotation == 0) {
					removeMask(plane, x, y, 512);
					removeMask(plane, x - 1, y + 1, 8192);
				}
				if (rotation == 1) {
					removeMask(plane, x, y, 2048);
					removeMask(plane, x + 1, 1 + y, 32768);
				}
				if (rotation == 2) {
					removeMask(plane, x, y, 8192);
					removeMask(plane, x + 1, y - 1, 512);
				}
				if (rotation == 3) {
					removeMask(plane, x, y, 32768);
					removeMask(plane, x - 1, -1 + y, 2048);
				}
			}
			if (type == 2) {
				if (rotation == 0) {
					removeMask(plane, x, y, 0x10400);
					removeMask(plane, -1 + x, y, 4096);
					removeMask(plane, x, y + 1, 16384);
				}
				if (rotation == 1) {
					removeMask(plane, x, y, 5120);
					removeMask(plane, x, y + 1, 16384);
					removeMask(plane, 1 + x, y, 0x10000);
				}
				if (rotation == 2) {
					removeMask(plane, x, y, 20480);
					removeMask(plane, x + 1, y, 0x10000);
					removeMask(plane, x, y - 1, 1024);
				}
				if (rotation == 3) {
					removeMask(plane, x, y, 0x14000);
					removeMask(plane, x, -1 + y, 1024);
					removeMask(plane, x - 1, y, 4096);
				}
			}
		}
		if (notAlternative) {
			if (type == 0) {
				if (rotation == 0) {
					removeMask(plane, x, y, 0x20000000);
					removeMask(plane, x - 1, y, 0x2000000);
				}
				if (rotation == 1) {
					removeMask(plane, x, y, 0x800000);
					removeMask(plane, x, y + 1, 0x8000000);
				}
				if (rotation == 2) {
					removeMask(plane, x, y, 0x2000000);
					removeMask(plane, x + 1, y, 0x20000000);
				}
				if (rotation == 3) {
					removeMask(plane, x, y, 0x8000000);
					removeMask(plane, x, y - 1, 0x800000);
				}
			}
			if (type == 1 || type == 3) {
				if (rotation == 0) {
					removeMask(plane, x, y, 0x400000);
					removeMask(plane, x - 1, y + 1, 0x4000000);
				}
				if (rotation == 1) {
					removeMask(plane, x, y, 0x1000000);
					removeMask(plane, 1 + x, 1 + y, 0x10000000);
				}
				if (rotation == 2) {
					removeMask(plane, x, y, 0x4000000);
					removeMask(plane, x + 1, -1 + y, 0x400000);
				}
				if (rotation == 3) {
					removeMask(plane, x, y, 0x10000000);
					removeMask(plane, -1 + x, y - 1, 0x1000000);
				}
			}
			if (type == 2) {
				if (rotation == 0) {
					removeMask(plane, x, y, 0x20800000);
					removeMask(plane, -1 + x, y, 0x2000000);
					removeMask(plane, x, 1 + y, 0x8000000);
				}
				if (rotation == 1) {
					removeMask(plane, x, y, 0x2800000);
					removeMask(plane, x, 1 + y, 0x8000000);
					removeMask(plane, x + 1, y, 0x20000000);
				}
				if (rotation == 2) {
					removeMask(plane, x, y, 0xa000000);
					removeMask(plane, 1 + x, y, 0x20000000);
					removeMask(plane, x, y - 1, 0x800000);
				}
				if (rotation == 3) {
					removeMask(plane, x, y, 0x28000000);
					removeMask(plane, x, y - 1, 0x800000);
					removeMask(plane, -1 + x, y, 0x2000000);
				}
			}
		}
	}

	public void setMask(int plane, int x, int y, int mask) {
		if (x >= 0 && x < 64 && y >= 0 && y < 64)
			masks[plane][x][y] = mask;
	}

	/*public void addMask(int plane, int x, int y, int mask) {
		if (x >= 0 && x < 64 && y >= 0 && y < 64)
			masks[plane][x][y] |= mask;
		else {
			System.out.println("We had " + x + " and " + y + "...");
			Region targ = World.getRegionAbs(rx * 64 + x, ry * 64 + y);

			if (targ != null) {
				int newx = (rx * 64 + x) - targ.absX;
				int newy = (ry * 64 + y) - targ.absY;

				System.out.println("The new locals are " + newx + " and " + newy);

				targ.clipmap[plane][newx][newy] |= mask;
			}
		}
	}*/

	public void addMask(DefinitionRepository repository, int plane, int x, int y, int mask, boolean add) {
		if (x >= 64 || y >= 64 || x < 0 || y < 0) {
			Tile target = new Tile(rx * 64 + x, rz * 64 + y);
			MapDefinition mapDefinition = repository.get(MapDefinition.class, target.region());

			if (mapDefinition != null) {
				int newx = (rx * 64 + x) & 0x3f;
				int newz = (rz * 64 + y) & 0x3f;

				if (add)
					mapDefinition.masks[plane][newx][newz] |= mask;
				else
					mapDefinition.masks[plane][newx][newz] &= (~mask);
			}
			return;
		}

		if (add)
			masks[plane][x][y] |= mask;
		else
			masks[plane][x][y] &= (~mask);
	}

	public void removeMask(int plane, int x, int y, int mask) {
		if (x >= 0 && x < 64 && y >= 0 && y < 64)
			masks[plane][x][y] &= (~mask);
	}

	private static int readCompact(ByteBuffer buffer) {
		int i_36_ = buffer.get(buffer.position()) & 0xFF;
		if (i_36_ < 128) {
			return buffer.get() & 0xFF;
		}

		return (buffer.getShort() & 0xFFFF) - 32768;
	}

}
