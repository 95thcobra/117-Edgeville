package edgeville.net;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import edgeville.GameServer;
import edgeville.net.codec.game.ActionDecoder;
import edgeville.net.codec.game.CommandEncoder;
import edgeville.net.codec.pregame.HandshakeResponseEncoder;
import edgeville.net.codec.pregame.Js5DataEncoder;
import edgeville.net.codec.pregame.PreGameDecoder;
import edgeville.net.codec.pregame.PregameLoginEncoder;

/**
 * @author Simon on 8/4/2014.
 */
public class ClientInitializer extends ChannelInitializer<Channel> {

	/**
	 * The handler to assign to the channel.
	 */
	private ServerHandler handler;

	/**
	 * The handler for js5 related message handling.
	 */
	private Js5Handler js5Handler;

	/**
	 * The handler for login related messages.
	 */
	private LoginHandler loginHandler;

	/**
	 * A shared command encoding instance. Can be shared because it's stateless and has no variables.
	 */
	private CommandEncoder commandEncoder;

	private GlobalTrafficShapingHandler trafficHandler;

	/**
	 * Construcs a new initializer from the given game server instance.
	 * @param server The server instance who owns this initializer.
	 */
	public ClientInitializer(GameServer server) {
		handler = new ServerHandler(server);
		js5Handler = new Js5Handler(server);
		loginHandler = new LoginHandler(server);
		commandEncoder = new CommandEncoder();
		trafficHandler = new GlobalTrafficShapingHandler(Executors.newSingleThreadScheduledExecutor(), 0, 0, 1000);
	}

	@Override
	protected void initChannel(Channel channel) throws Exception {
		channel.pipeline().addLast(trafficHandler, new PreGameDecoder(), new Js5DataEncoder(),
				new HandshakeResponseEncoder(), new PregameLoginEncoder(), js5Handler, loginHandler, handler);
	}

	public void initForGame(Channel channel) {
		while (channel.pipeline().last() != null) {
			channel.pipeline().removeLast();
		}

		channel.pipeline().addLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS), trafficHandler, commandEncoder,
				new ActionDecoder(), handler);
	}

	public TrafficCounter trafficStats() {
		return trafficHandler.trafficCounter();
	}

}