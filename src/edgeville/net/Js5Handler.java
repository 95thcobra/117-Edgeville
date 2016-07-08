package edgeville.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edgeville.Constants;
import edgeville.GameServer;
import edgeville.crypto.IsaacRand;
import edgeville.io.RSBuffer;
import edgeville.model.Tile;
import edgeville.model.entity.Player;
import edgeville.net.future.ClosingChannelFuture;
import edgeville.net.message.*;
import edgeville.net.message.game.encoders.Action;
import edgeville.net.message.game.encoders.DisplayMap;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author Simon on 8/4/2014.
 */
@ChannelHandler.Sharable
public class Js5Handler extends ChannelInboundHandlerAdapter {

	/**
	 * The logger instance for this class.
	 */
	private static final Logger logger = LogManager.getLogger(Js5Handler.class);

	/**
	 * A reference to the server instance.
	 */
	private GameServer server;

	/**
	 * Cached contents from the generated 255,255 request
	 */
	private byte[] cachedIndexInfo;

	public Js5Handler(GameServer server) {
		this.server = server;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		super.channelRead(ctx, msg);

		if (msg instanceof HandshakeMessage) {
			HandshakeMessage mes = (HandshakeMessage) msg;

			if (mes.revision() != /*server.config().getInt("server.revision")*/Constants.REVISION) {
				if (/*server.config().getBoolean("server.forcerevision")*/Constants.FORCE_REVISION) {
					logger.trace("Rejected incoming js5 channel because their revision ({}) was not {}", mes.revision(), /*server.config().getInt("server.revision")*/Constants.REVISION);

					ctx.writeAndFlush(HandshakeResponse.OUT_OF_DATE).addListener(new ClosingChannelFuture());
					return;
				} else {
					logger.trace("Accepted js5 connection with invalid revision ({}, wanted {})", mes.revision(), /*server.config().getInt("server.revision")*/Constants.REVISION);
				}
			}  else {
				logger.trace("Accepted js5 handshake from {}", ctx.channel());
			}

			ctx.writeAndFlush(HandshakeResponse.ALL_OK);
		} else if (msg instanceof Js5DataRequest) {
			Js5DataRequest req = ((Js5DataRequest) msg);

			if (req.index() == 255 && req.container() == 255) {
				ctx.writeAndFlush(new Js5DataMessage(255, 255, getIndexInfo(), req.priority()));
			} else if (req.index() == 255) {
				ctx.writeAndFlush(new Js5DataMessage(255, req.container(), getDescriptorData(req.container()), req.priority()));
			} else {
				ctx.writeAndFlush(new Js5DataMessage(req.index(), req.container(), getFileData(req.index(), req.container()), req.priority()));
			}
		}
	}

	private byte[] getDescriptorData(int index) {
		return trim(server.store().getDescriptorIndex().getArchive(index));
	}

	private byte[] getFileData(int index, int file) {
		return trim(server.store().getIndex(index).getArchive(file));
	}

	private byte[] trim(byte[] b) {
		if (b == null || b.length <= 5) {
			return new byte[5];
		}

		ByteBuffer buffer = ByteBuffer.wrap(b);
		int compression = buffer.get();
		int size = buffer.getInt();

		byte[] n = new byte[size + (compression == 0 ? 5 : 9)];
		System.arraycopy(b, 0, n, 0, size + (compression == 0 ? 5 : 9));
		return n;
	}

	private byte[] getIndexInfo() {
		if (cachedIndexInfo != null)
			return cachedIndexInfo;

		cachedIndexInfo = new byte[5 + server.store().getIndexCount() * 8];
		ByteBuffer buffer = ByteBuffer.wrap(cachedIndexInfo);
		buffer.put((byte) 0);
		buffer.putInt(server.store().getIndexCount() * 8);

		for (int index = 0; index < server.store().getIndexCount(); index++) {
			buffer.putInt(server.store().getIndex(index).getCRC());
			buffer.putInt(server.store().getIndex(index).getDescriptor().getRevision());
		}

		return cachedIndexInfo;
	}

}
