package edgeville.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edgeville.io.RSBuffer;
import edgeville.model.Tile;
import edgeville.model.World;
import edgeville.model.entity.Npc;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.NpcSyncInfo;
import edgeville.model.entity.player.PlayerSyncInfo;
import edgeville.net.message.game.encoders.UpdatePlayers;

/**
 * @author Simon on 8/23/2014.
 */
public class NpcSyncTask implements Task {

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

		private void sync(Player player) {
			RSBuffer buffer = new RSBuffer(player.channel().alloc().buffer(256));
			buffer.packet(234).writeSize(RSBuffer.SizeType.SHORT);

			buffer.startBitMode();
			encodeSurroundings(player, buffer);
			encodeMissing(player, buffer);
			buffer.endBitMode();

			// Update other masks
			PlayerSyncInfo sync = (PlayerSyncInfo) player.sync();
			for (int i=0; i < sync.npcUpdateReqPtr(); i++) {
				Npc npc = player.world().npcs().get(sync.npcUpdateRequests()[i]);

				if (npc == null) {
					buffer.writeByte(0);
					continue;
				}

				NpcSyncInfo npcSync = npc.sync();
				int mask = npcSync.calculatedFlag();
				buffer.writeByte(mask);

				
				if (npcSync.hasFlag(NpcSyncInfo.Flag.SHOUT.value))
					buffer.get().writeBytes(npcSync.shoutSet());
				
				
				if (npcSync.hasFlag(NpcSyncInfo.Flag.HIT.value)) {
					buffer.get().writeBytes(npcSync.getHitSetNPC());
				}
				if (npcSync.hasFlag(NpcSyncInfo.Flag.HIT2.value)) {
					buffer.get().writeBytes(npcSync.getHitSet2NPC());
				}
				if (npcSync.hasFlag(NpcSyncInfo.Flag.ANIMATION.value))
					buffer.get().writeBytes(npcSync.animationSet());
				if (npcSync.hasFlag(NpcSyncInfo.Flag.GRAPHIC.value))
					buffer.get().writeBytes(npcSync.graphicSet());

				
			}

			player.write(new UpdatePlayers(buffer));
		}

		private void encodeSurroundings(Player player, RSBuffer buffer) {
			buffer.writeBits(8, player.sync().localNpcPtr()); // Local npc count

			int rebuiltptr = 0;
			for (int i=0; i<player.sync().localNpcPtr(); i++) {
				int index = player.sync().localNpcIndices()[i];
				Npc npc = player.world().npcs().get(index);

				// See if the player either logged out, or is out of our viewport
				if (npc == null || player.getTile().distance(npc.getTile()) > 14 || player.getTile().level != npc.getTile().level) {
					buffer.writeBits(1, 1); // Yes, we need an update
					buffer.writeBits(2, 3); // Type 3: remove
					continue;
				}

				boolean needsUpdate = npc.sync().dirty();
				npc.inViewport(true); // Mark as in viewport

				if (needsUpdate) {
					buffer.writeBits(1, 1);

					int primaryStep = npc.sync().primaryStep();
					int secondaryStep = npc.sync().secondaryStep();
					boolean teleport = npc.sync().teleported();

					if (teleport) {
						buffer.writeBits(2, 3); // Teleport (don't add to rebuilt, respawn after adding)
					} else if (primaryStep >= 0) {
						boolean run = secondaryStep >= 0;
						buffer.writeBits(2, run ? 2 : 1); // Step up your game

						buffer.writeBits(3, primaryStep);
						if (run)
							buffer.writeBits(3, secondaryStep);

						buffer.writeBits(1, npc.sync().calculatedFlag() != 0 ? 1 : 0);

						rebuilt[rebuiltptr++] = index;
						if (npc.sync().calculatedFlag() != 0) {
							player.sync().npcUpdateRequests()[player.sync().npcUpdateReqPtr()] = npc.index();
							player.sync().npcUpdateReqPtr(player.sync().npcUpdateReqPtr() + 1);
						}
					} else {
						buffer.writeBits(2, 0); // No movement
						rebuilt[rebuiltptr++] = index;

						player.sync().npcUpdateRequests()[player.sync().npcUpdateReqPtr()] = npc.index();
						player.sync().npcUpdateReqPtr(player.sync().npcUpdateReqPtr() + 1);
					}
				} else {
					buffer.writeBits(1, 0); // No updates at all
					rebuilt[rebuiltptr++] = index;
				}
			}

			System.arraycopy(rebuilt, 0, player.sync().localNpcIndices(), 0, rebuiltptr);
			player.sync().localNpcPtr(rebuiltptr);
		}

		private void encodeMissing(Player player, RSBuffer buffer) {
			int[] ln = player.sync().localNpcIndices();
			final int[] lnp = {player.sync().localNpcPtr()};

			for (int idx = 0; idx < 2048; idx++) {
				Npc npc = player.world().npcs().get(idx);
				if (npc == null || player.sync().hasNpcInView(npc.index()) || player.getTile().distance(npc.getTile()) > 14 || player.getTile().level != npc.getTile().level)
					continue;

				// Limit addition to 25 per cycle, and 255 local.
				//if (player.sync().newlyAddedPtr() >= 25 || lpp[0] >= 254) {
				//	break;
				//}

				buffer.writeBits(15, npc.index());
				buffer.writeBits(3, npc.spawnDirection()); // Direction to face
				buffer.writeBits(1, 0); // Update
				buffer.writeBits(14, npc.id()); // npc id
				buffer.writeBits(1, 1); // Clear tile queue
				buffer.writeBits(5, npc.getTile().x - player.getTile().x);
				buffer.writeBits(5, npc.getTile().z - player.getTile().z);

				ln[lnp[0]++] = npc.index();
			}

			// Only write the end if there's more bytes coming; otherwise the client knows it's empty.
			if (player.sync().npcUpdateReqPtr() > 0)
				buffer.writeBits(15, -1); // No more adding

			player.sync().localNpcPtr(lnp[0]);
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
