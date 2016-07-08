package edgeville.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.AttributeKey;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
public class ServerHandler extends ChannelInboundHandlerAdapter {

	/**
	 * The logger instance for this class.
	 */
	private static final Logger logger = LogManager.getLogger(ServerHandler.class);

	/**
	 * The attribute key for the Player attachment of the channel.
	 */
	public static final AttributeKey<Player> ATTRIB_PLAYER = AttributeKey.valueOf("player");

	/**
	 * A reference to the server instance.
	 */
	private GameServer server;

	public ServerHandler(GameServer server) {
		this.server = server;
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		super.channelRegistered(ctx);

		logger.trace("A new client has connected: {}", ctx.channel());
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		super.channelUnregistered(ctx);

		logger.trace("A client has disconnected: {}", ctx.channel());

		if (ctx.channel().attr(ATTRIB_PLAYER).get() != null) {
			ctx.channel().attr(ATTRIB_PLAYER).get().putAttribute(edgeville.model.AttributeKey.LOGOUT, true);
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		super.channelRead(ctx, msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause.getStackTrace()[0].getMethodName().equals("read0"))
			return;

		if (cause instanceof ReadTimeoutException) {
			logger.info("Channel disconnected due to read timeout (30s): {}.", ctx.channel());
			ctx.channel().close();
		} else {
			logger.error("An exception has been caused in the pipeline: ", cause);
		}
	}

}
