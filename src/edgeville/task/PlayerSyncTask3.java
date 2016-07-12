package edgeville.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edgeville.io.RSBuffer;
import edgeville.io.RSBuffer.SizeType;
import edgeville.model.Tile;
import edgeville.model.World;
import edgeville.model.entity.Player;
import edgeville.model.entity.SyncInfo;
import edgeville.model.entity.player.EquipSlot;
import edgeville.model.entity.player.PlayerSyncInfo;
import edgeville.net.message.game.encoders.UpdatePlayers;
import edgeville.util.EquipmentInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Simon
 */
public class PlayerSyncTask3 implements Task {

	private static final Logger logger = LogManager.getLogger(PlayerSyncTask3.class);

	static class Job extends SubTask {

		/**
		 * Preallocated integer array to avoid continuous reallocation.
		 */
		private int[] rebuilt = new int[2048];
		private Player[] players;

		public Job(World world, Player... players) {
			super(world);
			this.players = players;
		}

		@Override
		public void execute() {
			for (Player player : players)
				sync(player);
		}

		public static int testlol = 0; // TEST THIS FOR ONCE

		private void syncTest(Player player) {
			if (testlol == 0) {
				RSBuffer buffer = new RSBuffer(player.channel().alloc().buffer(512));
				//RSBuffer buffer = new RSBuffer(Unpooled.buffer());
				buffer.packet(64).writeSize(RSBuffer.SizeType.SHORT);
				buffer.startBitMode();

				buffer.writeBits(1, 1);
				buffer.writeBits(1, 1);
				buffer.writeBits(2, 0);

				// outside loop 2
				for (int i = 0; i < 2047; i++) {
					buffer.writeBits(1, 0);
					buffer.writeBits(2, 0); // Skip amount 0, is this right?
				}

				buffer.endBitMode();

				int maskData = 32;
				buffer.writeByte(maskData);
				
				int appearanceSize = 41; // Appearance size TODO:replace by size of buffer
				buffer.writeByte(appearanceSize); // Send appearance size
				
				//updateAppearance(player, buffer); // Works 

				// Does not work
				RSBuffer appearanceBuffer = new RSBuffer(Unpooled.buffer());
				updateAppearance(player, appearanceBuffer);	
				buffer.writeBytes(appearanceBuffer.getBuffer());

				System.out.println("sending 64 playerupdating");
				player.write(new UpdatePlayers(buffer));
			}
			testlol++;
		}

		private void sync(Player player) {
			if (12 == 12) {
				syncTest(player);
				return;
			}

			// System.out.println("GOT HEREEE");
			RSBuffer buffer = new RSBuffer(player.channel().alloc().buffer(512));
			buffer.packet(64).writeSize(RSBuffer.SizeType.SHORT);

			RSBuffer appearanceUpdateBuffer = new RSBuffer(Unpooled.buffer());

			buffer.startBitMode();

			processLocalPlayers(player, buffer, appearanceUpdateBuffer, true);
			processLocalPlayers(player, buffer, appearanceUpdateBuffer, false);

			processOutsidePlayers(player, buffer, appearanceUpdateBuffer, true);
			processOutsidePlayers(player, buffer, appearanceUpdateBuffer, true);

			buffer.endBitMode();

			buffer.writeBytes(appearanceUpdateBuffer.getBuffer());

			this.localPlayerIndexCount = 0;
			this.outsidePlayerIndexCount = 0;
			for (int playerIndex = 1; playerIndex < 2048; playerIndex++) {
				slotFlags[playerIndex] >>= 1;
				Player p = localPlayers[playerIndex];
				if (p == null)
					this.outsidePlayerIndices[this.outsidePlayerIndexCount++] = playerIndex;
				else
					this.localPlayerIndices[this.localPlayerIndexCount++] = playerIndex;
			}

			player.write(new UpdatePlayers(buffer));
		}

		private boolean needsRemove(Player player, Player currentPlayer) {
			return currentPlayer == null || player.getTile().distance(currentPlayer.getTile()) > 14
					|| player.getTile().level != currentPlayer.getTile().level;
		}

		private static final int MAX_PLAYER_ADD = 15;

		private boolean needsAdd(Player player, Player currentPlayer) {
			return currentPlayer != null && player.getTile().distance(currentPlayer.getTile()) <= 14
					&& player.getTile().level == currentPlayer.getTile().level && localAddedPlayers < MAX_PLAYER_ADD;
		}

		public Player[] localPlayers = new Player[2048];
		public Player[] outsidePlayers = new Player[2048];

		public int[] localPlayerIndices = new int[2048];
		public int localPlayerIndexCount = 0;

		public int[] outsidePlayerIndices = new int[2048];
		public int outsidePlayerIndexCount = 0;

		public int[] regionHashes = new int[2048];
		public byte[] slotFlags = new byte[2048];

		public int localAddedPlayers = 0;

		private void processOutsidePlayers(Player player, RSBuffer buffer, RSBuffer appearanceUpdateBuffer, boolean b) {
			int skip = 0;
			for (int i = 0; i < outsidePlayerIndexCount; i++) {
				int playerIndex = outsidePlayerIndices[i];
				if (b ? slotFlags[playerIndex] == 0 : slotFlags[playerIndex] != 0)
					continue;

				Player currentPlayer = player.world().players().get(playerIndex);
				if (needsAdd(player, currentPlayer)) {
					buffer.writeBits(1, 1);
					buffer.writeBits(2, 0); // request add
					int hash = currentPlayer.getTile().toRegionPacked();
					if (hash == regionHashes[playerIndex])
						buffer.writeBits(1, 0);
					else {
						buffer.writeBits(1, 1);
						updateRegionHash(buffer, regionHashes[playerIndex], hash);
						regionHashes[playerIndex] = hash;
					}
					buffer.writeBits(6, currentPlayer.getTile().getXInRegion());
					buffer.writeBits(6, currentPlayer.getTile().getYInRegion());
					boolean NEEDAPPEARANCEUPDATE_TODO = false;// TODO
					appendUpdateBlock(currentPlayer, appearanceUpdateBuffer, NEEDAPPEARANCEUPDATE_TODO, true);
					buffer.writeBits(1, 1);
					localAddedPlayers++;
					localPlayers[currentPlayer.index()] = currentPlayer;
					slotFlags[playerIndex] = (byte) (slotFlags[playerIndex] | 2);
				} else {
					int hash = currentPlayer == null ? regionHashes[playerIndex]
							: currentPlayer.getTile().toRegionPacked();
					if (currentPlayer != null && hash != regionHashes[playerIndex]) {
						buffer.writeBits(1, 1);
						updateRegionHash(buffer, regionHashes[playerIndex], hash);
						regionHashes[playerIndex] = hash;
					} else {
						buffer.writeBits(1, 0);
						for (int j = i + 1; j < outsidePlayerIndexCount; j++) {
							int p2Index = outsidePlayerIndices[j];
							if (b ? slotFlags[p2Index] == 0 : slotFlags[p2Index] != 0)
								continue;

							Player p2 = player.world().players().get(p2Index);
							if (needsAdd(player, p2)
									|| (p2 != null && p2.getTile().toRegionPacked() != regionHashes[p2Index]))
								break;
							skip++;

						}
						skipPlayers(buffer, skip);
					}
				}
			}
		}

		private void processLocalPlayers(Player player, RSBuffer buffer, RSBuffer appearanceUpdateBuffer, boolean b) {
			int skip = 0;
			for (int i = 0; i < this.localPlayerIndexCount; i++) {
				int playerIndex = this.localPlayerIndices[i];
				Player currentPlayer = player.world().players().get(playerIndex);

				if (b ? slotFlags[playerIndex] != 0 : slotFlags[playerIndex] == 0)
					continue;

				if (needsRemove(player, currentPlayer)) {
					buffer.writeBits(1, 1); // needs update
					buffer.writeBits(1, 0); // no masks update
					buffer.writeBits(2, 0); // request remove

					regionHashes[playerIndex] = currentPlayer.activeMap() == null
							? currentPlayer.getTile().toRegionPacked() : currentPlayer.activeMap().toRegionPacked();
					int hash = currentPlayer.getTile().toRegionPacked();
					if (hash == regionHashes[playerIndex])
						buffer.writeBits(1, 0);
					else {
						buffer.writeBits(1, 1);
						updateRegionHash(buffer, regionHashes[playerIndex], hash);
						regionHashes[playerIndex] = hash;
					}

					localPlayers[playerIndex] = null;
				} else {
					boolean needsUpdate = currentPlayer.sync().dirty();

					if (needsUpdate) {
						boolean NEEDAPPEARANCEUPDATE_TODO = false;// TODO
						appendUpdateBlock(currentPlayer, appearanceUpdateBuffer, NEEDAPPEARANCEUPDATE_TODO, false);
					}

					if (currentPlayer.sync().teleported()) {
						buffer.writeBits(1, 1); // needs update
						buffer.writeBits(1, needsUpdate ? 1 : 0); // flag update
						buffer.writeBits(2, 3); // teleport type

						int xOffset = currentPlayer.getTile().x - currentPlayer.activeMap().x;
						int yOffset = currentPlayer.getTile().z - currentPlayer.activeMap().z;
						int heightOffset = currentPlayer.getTile().level - currentPlayer.activeMap().level;

						if (Math.abs(xOffset) <= 14 && Math.abs(yOffset) <= 14) {
							buffer.writeBits(1, 0);

							if (xOffset < 0)
								xOffset += 32;
							if (yOffset < 0)
								yOffset += 32;

							buffer.writeBits(12, yOffset + (xOffset << 5) + (heightOffset << 10));

						} else {
							buffer.writeBits(1, 1);
							buffer.writeBits(30,
									(yOffset & 0x3fff) + ((xOffset & 0x3fff) << 14) + ((heightOffset & 0x3) << 28));
						}
					} else if (currentPlayer.sync().primaryStep() >= 0) { // walking?
						boolean running = currentPlayer.sync().primaryStep() >= 0;
						buffer.writeBits(1, 1);

						int xOffset = currentPlayer.getTile().x - currentPlayer.activeMap().x;
						int yOffset = currentPlayer.getTile().z - currentPlayer.activeMap().z;
						int heightOffset = currentPlayer.getTile().level - currentPlayer.activeMap().level;

						if (xOffset == 0 && yOffset == 0) {
							buffer.writeBits(1, 1);
							buffer.writeBits(2, 0);
							if (!needsUpdate) {
								boolean NEEDAPPEARANCEUPDATE_TODO = false;// TODO
								appendUpdateBlock(currentPlayer, appearanceUpdateBuffer, NEEDAPPEARANCEUPDATE_TODO,
										false);
							}
						} else {
							buffer.writeBits(1, needsUpdate ? 1 : 0);
							buffer.writeBits(2, running ? 2 : 1);
							int WALKING_DIRECTION_TODO = 0;
							buffer.writeBits(running ? 4 : 3, WALKING_DIRECTION_TODO); // TODO
						}
					} else if (needsUpdate) {
						buffer.writeBits(1, 1);
						buffer.writeBits(1, 1);
						buffer.writeBits(2, 0);
					} else { // skip
						buffer.writeBits(1, 0);

						for (int j = i + 1; j < currentPlayer.sync().localPlayerPtr(); j++) {
							int player2Index = currentPlayer.sync().localPlayerIndices()[j];
							if (b ? currentPlayer.sync().calculatedFlag() != 0
									: currentPlayer.sync().calculatedFlag() == 0)
								continue;
							Player p2 = localPlayers[player2Index];
							if (needsRemove(player, p2)) {
								break;
							}
							skip++;
						}
						this.skipPlayers(buffer, skip);
						slotFlags[playerIndex] = (byte) (slotFlags[playerIndex] | 2);
					}
				}
			}
		}

		private void updateRegionHash(RSBuffer buffer, int lastRegionHash, int currentRegionHash) {
			int lastRegionX = lastRegionHash >> 8;
			int lastRegionY = 0xff & lastRegionHash;
			int lastPlane = lastRegionHash >> 16;
			int currentRegionX = currentRegionHash >> 8;
			int currentRegionY = 0xff & currentRegionHash;
			int currentPlane = currentRegionHash >> 16;
			int planeOffset = currentPlane - lastPlane;
			if (lastRegionX == currentRegionX && lastRegionY == currentRegionY) {
				buffer.writeBits(2, 1);
				buffer.writeBits(2, planeOffset);
			} else if (Math.abs(currentRegionX - lastRegionX) <= 1 && Math.abs(currentRegionY - lastRegionY) <= 1) {
				int opcode;
				int dx = currentRegionX - lastRegionX;
				int dy = currentRegionY - lastRegionY;
				if (dx == -1 && dy == -1)
					opcode = 0;
				else if (dx == 1 && dy == -1)
					opcode = 2;
				else if (dx == -1 && dy == 1)
					opcode = 5;
				else if (dx == 1 && dy == 1)
					opcode = 7;
				else if (dy == -1)
					opcode = 1;
				else if (dx == -1)
					opcode = 3;
				else if (dx == 1)
					opcode = 4;
				else
					opcode = 6;
				buffer.writeBits(2, 2);
				buffer.writeBits(5, (planeOffset << 3) + (opcode & 0x7));
			} else {
				int xOffset = currentRegionX - lastRegionX;
				int yOffset = currentRegionY - lastRegionY;
				buffer.writeBits(2, 3);
				buffer.writeBits(18, (yOffset & 0xff) + ((xOffset & 0xff) << 8) + (planeOffset << 16));
			}
		}

		private void updateAppearance(Player player, RSBuffer buffer) {
			
			buffer.writeByte(0); // Gender
			buffer.writeByte(player.getSkullHeadIcon()); // Skull
			buffer.writeByte(player.getPrayerHeadIcon()); // Prayer

			// if (transmog >= 0) {
			// buffer.writeShort(0xFFFF).writeShort(transmog);
			/* } else */ {
				int[] looks = { 0, 0, 0, 0, 18, 0, 26/* arms */, 36, 7, 33, 42, 10/* beard */ };
				// 6 = arms
				// 8 = hair
				// 9 = hands
				// 10 = boots
				// 11 = beard
				EquipmentInfo equipInfo = player.world().equipmentInfo();
				for (int i = 0; i < 12; i++) {
					/*
					 * if (i == 6 && player.getEquipment().hasAt(4) &&
					 * equipInfo.typeFor(player.getEquipment().get(4).getId())
					 * == 6) { buffer.writeByte(0); continue; }
					 * 
					 * if (i == 8 && player.getEquipment().hasAt(0) &&
					 * equipInfo.typeFor(player.getEquipment().get(0).getId())
					 * == 8) { buffer.writeByte(0); continue; }
					 * 
					 * if (i == 11 && player.getEquipment().hasAt(0) &&
					 * equipInfo.typeFor(player.getEquipment().get(0).getId())
					 * == 8) {
					 * 
					 * }
					 * 
					 * if (player.getEquipment().hasAt(i)) {
					 * buffer.writeShort(0x200 +
					 * player.getEquipment().get(i).getId()); } else if
					 * (looks[i] != 0) { buffer.writeShort(0x100 + looks[i]); }
					 * else { buffer.writeByte(0); }
					 */
					//buffer.writeByte(looks[i]);
					buffer.writeByte(0);
				}
			}

			// Dem colors
			buffer.writeByte(3);
			buffer.writeByte(16);
			buffer.writeByte(16);
			buffer.writeByte(0);
			buffer.writeByte(0);

			//int weapon = player.getEquipment().hasAt(EquipSlot.WEAPON) ? player.getEquipment().get(EquipSlot.WEAPON).getId() : -1;
			//int[] renderpair = player.world().equipmentInfo().renderPair(weapon);
			
			//for (int renderAnim : renderpair) {
			//	buffer.writeShort(renderAnim); // Renderanim
			//}
			buffer.writeShort(808); // Renderanim
			buffer.writeShort(808); // Renderanim
			buffer.writeShort(808); // Renderanim
			buffer.writeShort(808); // Renderanim
			buffer.writeShort(808); // Renderanim
			buffer.writeShort(808); // Renderanim
			buffer.writeShort(822); // Renderanim

			/* Str idgaf */

			buffer.writeString(player.name());
			
			buffer.writeByte(0);
			buffer.writeShort(0);
			buffer.writeByte(0);
			buffer.writeByte(0);
		}

		private void appendUpdateBlock(Player player, RSBuffer buffer, boolean needAppearanceUpdate, boolean added) {
			PlayerSyncInfo pSync = (PlayerSyncInfo) player.sync();
			int maskData = 0;
			if (needAppearanceUpdate) {
				updateAppearance(player, buffer);
			}

			/*
			 * PlayerSyncInfo pSync = (PlayerSyncInfo) player.sync(); int mask =
			 * pSync.calculatedFlag() |
			 * (player.sync().isNewlyAdded(player.index()) ?
			 * PlayerSyncInfo.Flag.LOOKS.value : 0); if (mask >> 8 != 0) { mask
			 * |= 0x80; }
			 * 
			 * buffer.writeByte(mask); if (mask >> 8 != 0) buffer.writeByte(mask
			 * >> 8);
			 * 
			 * if (pSync.hasFlag(PlayerSyncInfo.Flag.HIT.value))
			 * buffer.get().writeBytes(pSync.hitSet()); if
			 * (pSync.hasFlag(PlayerSyncInfo.Flag.FACE_ENTITY.value))
			 * buffer.get().writeBytes(pSync.faceEntitySet()); if
			 * (pSync.hasFlag(PlayerSyncInfo.Flag.GRAPHIC.value))
			 * buffer.get().writeBytes(pSync.graphicSet()); if
			 * (pSync.hasFlag(PlayerSyncInfo.Flag.SHOUT.value))
			 * buffer.get().writeBytes(pSync.shoutSet()); if
			 * (pSync.hasFlag(PlayerSyncInfo.Flag.LOOKS.value) ||
			 * player.sync().isNewlyAdded(player.index()))// this
			 * buffer.get().writeBytes(pSync.looksBlock()); if
			 * (pSync.hasFlag(PlayerSyncInfo.Flag.FORCE_MOVE.value))
			 * buffer.get().writeBytes(pSync.forceMoveSet()); if
			 * (pSync.hasFlag(PlayerSyncInfo.Flag.FACE_TILE.value))
			 * buffer.get().writeBytes(pSync.faceTileSet()); if
			 * (pSync.hasFlag(PlayerSyncInfo.Flag.CHAT.value))
			 * buffer.get().writeBytes(pSync.chatMessageBlock()); if
			 * (pSync.hasFlag(PlayerSyncInfo.Flag.ANIMATION.value))
			 * buffer.get().writeBytes(pSync.animationSet()); if
			 * (pSync.hasFlag(PlayerSyncInfo.Flag.HIT2.value))
			 * buffer.get().writeBytes(pSync.hitSet2());
			 */
		}

		private void skipPlayers(RSBuffer buffer, int amount) {
			buffer.writeBits(2, amount == 0 ? 0 : amount > 255 ? 3 : (amount > 31 ? 2 : 1));
			if (amount > 0)
				buffer.writeBits(amount > 255 ? 11 : (amount > 31 ? 8 : 5), amount);
		}
	}

	@Override
	public void execute(World world) {

	}

	@Override
	public Collection<SubTask> createJobs(World world) {
		int numjobs = world.players().size() / 25 + 1;
		ArrayList<SubTask> tasks = new ArrayList<>(numjobs);
		List<Player> work = new ArrayList<>(5);

		// Create jobs which will cover 5 players per job
		world.players().forEach(p -> {
			work.add(p);

			if (work.size() == 100) {
				tasks.add(new Job(world, work.toArray(new Player[work.size()])));
				work.clear();
			}
		});

		// Remainders?
		if (!work.isEmpty()) {
			tasks.add(new Job(world, work.toArray(new Player[work.size()])));
		}

		return tasks;
	}

	@Override
	public boolean isAsyncSafe() {
		return true;
	}
}
