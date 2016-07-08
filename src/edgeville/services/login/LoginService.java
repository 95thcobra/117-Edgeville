package edgeville.services.login;

import edgeville.Constants;
import edgeville.GameServer;
import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.Interfaces;
import edgeville.model.uid.UIDProvider;
import edgeville.net.ServerHandler;
import edgeville.net.message.LoginRequestMessage;
import edgeville.net.message.game.encoders.Action;
import edgeville.net.message.game.encoders.DisplayMap;
import edgeville.net.message.game.encoders.SetRootPane;
import edgeville.services.Service;
import edgeville.services.serializers.JSONFileSerializer;
import edgeville.services.serializers.PlayerSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Simon on 8/1/2015.
 *
 * Handles logging in, logging out... and being logged in?
 */
public class LoginService implements Service {

	private static final Logger logger = LogManager.getLogger(LoginService.class);

	/**
	 * The queue of pending login requests, which is concurrent because there's (at least) two threads
	 * accessing this at the same time. One (or more) being the decoder thread from Netty, one (or more) being
	 * the login service worker.
	 */
	private LinkedBlockingQueue<LoginRequestMessage> messages = new LinkedBlockingQueue<>();

	/**
	 * The executor which houses the login service workers.
	 */
	private Executor executor;

	/**
	 * A reference to the game server which we process requests for.
	 */
	private GameServer gameServer;

	/**
	 * The serializer we use to (de)serialize our lovely player's data. Defaults to JSON.
	 */
	private PlayerSerializer serializer;

	@Override
	public void setup(GameServer server/*, Config serviceConfig*/) {
		gameServer = server;

		UIDProvider uidProvider = server.uidProvider();
		serializer = server.service(PlayerSerializer.class, true).orElse(new JSONFileSerializer(uidProvider));

		logger.info("Using {} to serialize and deserialize player data.", serializer.getClass().getSimpleName());
	}

	public void enqueue(LoginRequestMessage message) {
		messages.add(message);
	}

	public LinkedBlockingQueue<LoginRequestMessage> messages() {
		return messages;
	}

	@Override
	public boolean isAlive() {
		return true; // How could this service possibly be dead??
	}

	@Override
	public boolean start() {
		executor = Executors.newFixedThreadPool(2);

		for (int i = 0; i < 1; i++)
			executor.execute(new LoginWorker(this));

		return true;
	}

	@Override
	public boolean stop() {
		return false; // TODO disable the login service, denying requests. Might be a nice feature.
	}

	public GameServer server() {
		return gameServer;
	}

	public PlayerSerializer serializer() {
		return serializer;
	}

	public static void complete(Player player, GameServer server, LoginRequestMessage message) {
		//player.interfaces().resizable(message.resizableInterfaces());
		player.move(player.getTile());

		// Attach player to session
		player.channel().attr(ServerHandler.ATTRIB_PLAYER).set(player);

		
		System.out.println("Sending displaymap!");
		player.write(new DisplayMap(player)); // This has to be the first packet!		
		
		// incoming packet crash
		player.write(new SetRootPane(548));
		
		//player.world().syncMap(player, null);
		//player.interfaces().send(); // Must come after to set root pane; else crash =(

		// This isn't really a packet but it's required to be done on the logic thread
		/*player.pendingActions().add(new Action() {
			public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {}
			public void process(Player player) {
				player.initiate();
			}
		});*/
	}

}
