package edgeville.net.codec.pregame;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edgeville.net.message.HandshakeMessage;
import edgeville.net.message.Js5DataRequest;
import edgeville.net.message.LoginRequestMessage;
import edgeville.util.BufferUtilities;
import edgeville.util.UsernameUtilities;

import java.util.List;

/**
 * @author Simon on 8/4/2014.
 *
 */
public class PreGameDecoder extends ByteToMessageDecoder {

	/**
	 * Logging instance for this class.
	 */
	private static final Logger logger = LogManager.getLogger(PreGameDecoder.class);

	private static final int HANDSHAKE_OPCODE = 15;

	private static final int INITIALIZATION_OPCODE_1 = 2;
	private static final int INITIALIZATION_OPCODE_2 = 3;
	private static final int INITIALIZATION_OPCODE_3 = 6;

	private static final int PRIORITY_FETCH = 0;
	private static final int DELAYABLE_FETCH = 1;

	private static final int PRE_LOGIN = 14;
	private static final int LOGIN = 16;
	private static final int RECONNECT = 18;

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (!in.isReadable())
			return;

		in.markReaderIndex();
		int opcode = in.readByte();

		if (opcode == HANDSHAKE_OPCODE) {
			int revision = in.readInt();
			out.add(new HandshakeMessage(revision));
		} else if (opcode == INITIALIZATION_OPCODE_1 || opcode == INITIALIZATION_OPCODE_2 || opcode == INITIALIZATION_OPCODE_3) {
			in.skipBytes(3);
		} else if (opcode == PRIORITY_FETCH || opcode == DELAYABLE_FETCH) {
			if (in.readableBytes() < 3) {
				in.resetReaderIndex();
				return;
			}

			int index = in.readUnsignedByte();
			int container = in.readUnsignedShort();

			logger.trace("Container fetch request for index {}, container {}.", index, container);
			out.add(new Js5DataRequest(index, container, opcode == PRIORITY_FETCH));
		} else if (opcode == PRE_LOGIN) {
			ctx.writeAndFlush(ctx.alloc().buffer(1).writeByte(0));
		} else if (opcode == LOGIN || opcode == RECONNECT) {
			/* Make sure all data is here */
			int size = in.readUnsignedShort();
			int end = in.readerIndex() + size;
			if (in.readableBytes() < size) {
				in.resetReaderIndex();
				return;
			}

			/* Decode login now that we're sure this is a full packet */
			LoginRequestMessage lrm = decodeLogin(ctx, in);
			if (lrm != null)
				out.add(lrm);

			// Skip to end of message
			in.readerIndex(end);
		} else if (opcode == 23) {
			in.readInt();
			ByteBuf o = Unpooled.buffer();
			o.writeByte(0);
			o.writeShort(28); // sz
			o.writeByte(1); // all okay
			o.writeByte(1); // whole packet

			o.writeByte(1); {// Num worlds
				o.writeByte(0); // flag
				o.writeByte(0).writeBytes("Hi".getBytes()).writeByte(0); // name
			}

			o.writeByte(0);
			o.writeByte(1);
			o.writeByte(1);

			o.writeByte(0);
			o.writeByte(0);
			o.writeInt(0);
			o.writeByte(0).writeByte(0); // activity
			o.writeByte(0).writeByte(0); // address
			o.writeInt(0);

			o.writeByte(0);
			o.writeShort(0);

			ctx.writeAndFlush(o);
		} else {
			logger.info("Unknown incoming pregame opcode: {}", opcode);
			in.resetReaderIndex();
		}
	}

	private LoginRequestMessage decodeLogin(ChannelHandlerContext ctx, ByteBuf in) {
		int revision = in.readInt();
		System.out.printf("Revision: %d\n", revision);
		
		int unknown_1 = in.readByte(); // should be 1
		System.out.printf("unknown_1: %d\n", unknown_1);
		
		int unknown_2 = in.readByte(); // not sure
		System.out.printf("unknown_2: %d\n", unknown_2);

		/* Isaac key */
		int[] isaacSeed = new int[4];
		for (int i=0; i<4; i++) {
			isaacSeed[i] = in.readInt();
			System.out.println("isaac: "+i + " -> " +isaacSeed[i]);
		}
		
		in.readLong();
		String password = BufferUtilities.readString(in);
		System.out.printf("password: %s\n", password);
		
		String username = BufferUtilities.readString(in);
		System.out.printf("username: %s\n", username);
		
		//in.readLong(); // TODO figure out what this is.
		//String password = BufferUtilities.readString(in);
		//String username = BufferUtilities.readString(in);

		//int unknownshort1 = in.readShort();
		//int unknownshort2 = in.readShort();

		/* Contents from the random.dat file (probably used to ban a computer) */
		//byte[] random_dat = new byte[24];
		//in.readBytes(random_dat);

		//String unknownstring = BufferUtilities.readString(in);
		//int unknownint1 = in.readInt();
		//int unknownint2 = in.readInt();

		//int[] crcs = new int[27];
		//for (int i=0; i<crcs.length; i++)
		//	crcs[i] = in.readInt();

        // int ten = in.readByte();
		logger.info("User login from {}:{}.", username, password);

		return new LoginRequestMessage(ctx.channel(), username, password, isaacSeed, new int[4], revision, new byte[24], false);
	}
}