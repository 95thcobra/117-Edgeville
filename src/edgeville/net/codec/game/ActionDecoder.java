package edgeville.net.codec.game;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;
import edgeville.net.ServerHandler;
import edgeville.net.message.game.decoders.*;
import edgeville.net.message.game.encoders.Action;
import edgeville.net.message.game.encoders.PacketInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Simon on 8/23/2014.
 *
 */
public class ActionDecoder extends ByteToMessageDecoder {

	private static final Logger logger = LogManager.getLogger(ActionDecoder.class);

	@SuppressWarnings("unchecked")
	private Class<? extends Action>[] actionRepository = new Class[256];
	private Map<Integer, Integer> ignored = new HashMap<>();
	private int[] actionSizes = new int[256];

	private State state = State.OPCODE;
	private int opcode;
	private int size;

	public ActionDecoder() {
		/* Fill repo, maybe through xml/json? */
		actionRepository[40] = SpellOnNpc.class;
		actionRepository[50] = ItemDragBank.class;
		actionRepository[13] = JoinClanChatDialog.class;
		
		actionRepository[202] = WalkMap.class;
		actionRepository[70] = WalkMap.class;
		actionRepository[210] = PublicChat.class;
		actionRepository[213] = ExamineObject.class;
		actionRepository[231] = ConsoleAction.class;
		actionRepository[156] = ExamineItem.class;
		actionRepository[165] = ItemAction1.class;
		actionRepository[122] = ItemAction2.class;
		actionRepository[212] = ItemAction3.class;
		actionRepository[153] = WindowStateChanged.class;
		actionRepository[190] = ItemDragAction.class;
		actionRepository[199] = PlayerAction1.class;
		actionRepository[237] = PlayerAction2.class;
		actionRepository[141] = ChangeDisplayMode.class;
		actionRepository[110] = SpellOnPlayer.class;
		actionRepository[107] = ItemOnItem.class;
		actionRepository[246] = ItemAction5.class;
		actionRepository[88] = ItemAction4.class;
		actionRepository[12] = GroundItemAction3.class;
		actionRepository[124] = DialogueContinue.class;
		actionRepository[185] = ObjectAction1.class;
		actionRepository[6] = ObjectAction2.class;
		actionRepository[84] = CloseMainInterface.class;

		actionRepository[189] = NpcAction1.class; // firstoption
		actionRepository[254] = NpcAttack.class; // attack

		actionRepository[228] = IntegerInput.class;
		actionRepository[227] = StringInput.class;
		//actionRepository[115] = PingServer.class;
		//actionRepository[117] = MoveMouse.class;
		//actionRepository[129] = ChangeDisplayMode.class;

		Arrays.stream(ButtonAction.OPCODES).forEach(i -> actionRepository[i] = ButtonAction.class);

		// Ignore a few classes
		ignored.put(25, 0);
		ignored.put(69, 0);
		ignored.put(46, -1);
		ignored.put(112, 6); // Mouse click
		ignored.put(148, -2); // Key history
		ignored.put(101, 4); // Key
		ignored.put(58, 0);
		ignored.put(59, -1);

		/* Load sizes */
		for (int i=0; i<256; i++) {
			if (actionRepository[i] != null) {
				PacketInfo annotation = actionRepository[i].getAnnotation(PacketInfo.class);

				if (annotation == null) {
					logger.warn("Missing PacketInfo annotation for packet {}!", i);
				} else {
					actionSizes[i] = annotation.size();
				}
			}
		}
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in.readableBytes() < 1) {
			/* Disconnection */
			return;
		}

		Player player = ctx.channel().attr(ServerHandler.ATTRIB_PLAYER).get();
		RSBuffer buffer = new RSBuffer(in);

		switch (state) {
			case OPCODE:
				if (in.readableBytes() < 1)
					return;
				opcode = buffer.readByte() /*- player.inrand().nextInt()*/ & 0xFF;
				state = State.SIZE;
				in.markReaderIndex();

			case SIZE:
				if (actionRepository[opcode] == null && !ignored.containsKey(opcode)) {
					logger.warn("Unknown action {}, probable size: {}.", opcode, buffer.get().readableBytes());

					size = in.readableBytes();
				} else {
					int required = ignored.containsKey(opcode) ? ignored.get(opcode) : actionSizes[opcode];

					if (required == -1) {
						if (in.readableBytes() < 1) {
							in.resetReaderIndex();
							return;
						}

						size = buffer.readUByte();
					} else if (required == -2) {
						if (in.readableBytes() < 2) {
							in.resetReaderIndex();
							return;
						}

						size = buffer.readUShort();
					} else {
						size = required;
					}
				}

				state = State.DATA;
				in.markReaderIndex();

			case DATA:
				if (in.readableBytes() < size) {
					logger.warn("Not enough data ({}) to decode packet {} ({} needed)", in.readableBytes(), opcode, size);

					// Too much data? Skip it.
					if (size > 1000) {
						in.skipBytes(in.readableBytes());
						state = State.OPCODE;
					}

					break;
				}

				if (!ignored.containsKey(opcode) && actionRepository[opcode] != null) {
					Action a = actionRepository[opcode].newInstance();
					int bufferStart = buffer.get().readerIndex();
					a.decode(new RSBuffer(buffer.get().slice(bufferStart, size)), ctx, opcode, size);
					player.pendingActions().add(a);
					buffer.get().readerIndex(bufferStart + size);
				} else {
					buffer.skip(size);
				}

				state = State.OPCODE;
				in.markReaderIndex();
				break;
		}
	}

	enum State {
		OPCODE,
		SIZE,
		DATA
	}

}
