package edgeville;

import edgeville.model.World;
import edgeville.model.uid.UIDProvider;
import edgeville.model.uid.providers.SimpleUIDProvider;
import edgeville.net.ClientInitializer;
import edgeville.plugin.PluginHandler;
import edgeville.services.Service;
import edgeville.services.login.LoginService;
import edgeville.services.serializers.JSONFileSerializer;
import edgeville.util.HuffmanCodec;
import edgeville.util.map.MapDecryptionKeys;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import nl.bartpelle.dawnguard.DataStore;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * @author Simon on 8/4/2014.
 */
public class GameServer {

	/**
	 * Logger instance for this class.
	 */
	private static final Logger logger = LogManager.getLogger(GameServer.class);

	/**
	 * Filestore instance
	 */
	private DataStore fileStore;

	/**
	 * Netty's server bootstrap instance.
	 */
	private ServerBootstrap bootstrap;

	/**
	 * The {@link edgeville.net.ClientInitializer} which sets the initial pipeline of the new connections.
	 */
	private ClientInitializer connectionInitializer;

	/**
	 * Our game world containing all the magic.
	 */
	private final World world;

	/**
	 * The 'heart' of our server, better known as the thread that lives once per 600 milliseconds.
	 * Every 600ms, this thread 'pulses' and does all the logic.
	 */
	private ServerProcessor processor;

	/**
	 * Our list of services currently loaded.
	 */
	private List<Service> services = new LinkedList<>();

	/**
	 * The UID provider we use to generate unique IDs for a player.
	 */
	private UIDProvider uidProvider;

	/**
	 * The Huffman codec instance we use to encode and decode chat, to save bytes.
	 */
	private HuffmanCodec huffman;

	/**
	 * Creates a new server instance from the passed configuration.
	 *
	 * @param config The configuration to load settings from.
	 */
	public GameServer(/*Config config, */File store) throws Exception {
		if (!store.exists()) {
			throw new FileNotFoundException("Cannot load data store from " + store.getAbsolutePath() + ", aborting.");
		}

		//this.config = config;
		//MapDecryptionKeys.load(new File(config.getString("server.mapkeys")));
		MapDecryptionKeys.load(new File(Constants.MAP_KEYS_DIR));
		fileStore = new DataStore(store);
		//loadScripts();
		//loadGroovyPlugins();
		world = new World(this);
		huffman = new HuffmanCodec(fileStore);
	}

	/**
	 * Starts listening on the port passed with the constructor.
	 */
	public void start() throws Exception {
		// Load the services
		setupServices();

		// Load the UID provider
		setupUIDProvider();

		// Start the engine
		processor = new ServerProcessor(this);
	
		// Construct bootstrap
		//EventLoopGroup acceptGroup = new NioEventLoopGroup(config.getInt("net.acceptthreads"));
		//EventLoopGroup ioGroup = new NioEventLoopGroup(config.getInt("net.iothreads"));
		EventLoopGroup acceptGroup = new NioEventLoopGroup(Constants.ACCEPT_THREADS);
		EventLoopGroup ioGroup = new NioEventLoopGroup(Constants.IO_THREADS);
		connectionInitializer = new ClientInitializer(this);

		bootstrap = new ServerBootstrap();
		bootstrap.group(acceptGroup, ioGroup);
		bootstrap.channel(NioServerSocketChannel.class);
		bootstrap.childHandler(connectionInitializer);
		bootstrap.childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000);
		bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
		bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

		System.gc();

		// Bind to the address/port from the configuration
		logger.info("Binding to {}:{}", /*config.getString("net.address")*/Constants.IP_ADDRESS, /*config.getInt("net.port")*/Constants.PORT);
		
		//System.out.println("HELLO1");
		
		//bootstrap.bind("127.0.0.1", 43594).sync().awaitUninterruptibly();
		bootstrap.bind(Constants.IP_ADDRESS, Constants.PORT).sync().awaitUninterruptibly();
		
		//System.out.println("HELLO");
	}

	/**
	 * Shut down the server in a graceful method. This will first terminate the Netty server (if any is active),
	 * and then terminate the 600ms logic thread in a graceful manner.
	 */
	public void shutdown() {
		if (bootstrap != null) {
			bootstrap.group().shutdownGracefully();
			bootstrap.childGroup().shutdownGracefully();
		}

		if (processor != null)
			processor.terminate();
	}

	/**
	 * Attack the JS scripting context to the calling thread
	 */
	/*public void loadScripts() throws IOException {
		logger.log(Level.INFO, "Loading scripts...");
		scriptExecutor = new ScriptExecutor();
		scriptRepository = new ScriptRepository(scriptExecutor);
		scriptRepository.load();
	}*/

	/*public void loadGroovyPlugins() {
		logger.log(Level.INFO, "Loading groovy plugins...");
		World.getPluginHandler().init();
	}*/

	/**
	 * Instantiate and setup any services that are provided through the configuration file.
	 */
	private void setupServices() {
		/*ConfigList serviceDefinitions = config.getList("services");

		for (ConfigValue serv : serviceDefinitions) {
			Class<? extends Service> serviceClass = null;
			Config object = ((ConfigObject) serv).toConfig();

			logger.info("Loading service '{}'...", object.getString("class"));

			// Try to resolve it
			try {
				serviceClass = Class.forName(object.getString("class")).asSubclass(Service.class);
			} catch (ClassNotFoundException e) {
				logger.error("Cannot find service class '{}'.", object.getString("class"));
			} catch (ClassCastException e) {
				logger.error("Unable to cast '{}' to subtype of Service.", object.getString("class"));
			}

			// Have we found the class? Try to add it to our list.
			if (serviceClass != null) {
				try {
					Service service = serviceClass.newInstance();
					service.setup(this, object);
					services.add(service);

					logger.info("Loaded service '{}'.", service.getClass().getSimpleName());
				} catch (Exception e) {
					logger.error("Unable to instantiate '{}'.", object.getString("class"), e);
				}
			}
		}

		// Start all the services we loaded
		services.forEach(Service::start);*/
		
		Service loginService = new LoginService();
		loginService.setup(this/*, object*/);
		services.add(loginService);
		
		logger.info("Loaded service: {}", loginService.getClass().getName());
		
		Service jsonService = new JSONFileSerializer(uidProvider);
		jsonService.setup(this);
		services.add(jsonService);
		
		logger.info("Loaded service: {}", jsonService.getClass().getName());

		// Start all the services we loaded
		services.forEach(Service::start);
	}

	/**
	 * Attempts to set up a UID provider as configured in the json configuration file.
	 */
	private void setupUIDProvider() {
		uidProvider = new SimpleUIDProvider(this);

		try {
			Class<?> untyped = Constants.UID_PROVIDER; //Class.forName(config.getString("server.uidprovider"));
			Class<? extends UIDProvider> providerClass = untyped.asSubclass(UIDProvider.class);
			uidProvider = providerClass.getConstructor(GameServer.class).newInstance(this);

			logger.info("Using {} as UID provider.", uidProvider.getClass().getSimpleName());
		} catch (Exception e) {
			logger.warn("Could not properly initialize UID provider of choice, falling back to SimpleUIDProvider.");
			logger.warn("Reason: {}", e.getCause().toString());
		}
	}

	/**
	 * Finds a service based on its type.
	 *
	 * @param serviceType The class of the service to look for. The class must be a subclass of Service.
	 * @param allowSubclass
	 * @return An optional holding either nothing if there was no such service active, or with the service.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Service> Optional<T> service(Class<? extends T> serviceType, boolean allowSubclass) {
		if (allowSubclass) {
			return (Optional<T>) services.stream().filter(s -> serviceType.isAssignableFrom(s.getClass())).findAny();
		}

		return (Optional<T>) services.stream().filter(s -> s.getClass() == serviceType).findAny();
	}

	/**
	 * Tries to resolve a service by its type, and checks if the service is alive and running properly.
	 *
	 * @param serviceType The class of the service to check.
	 * @return <code>true</code> if the service both exists and is alive, <code>false</code> if not.
	 */
	public boolean serviceAlive(Class<? extends Service> serviceType) {
		Optional<Service> s = service(serviceType, false);
		return s.isPresent() && s.get().isAlive();
	}

	public DataStore store() {
		return fileStore;
	}

	public World world() {
		return world;
	}

	public ClientInitializer initializer() {
		return connectionInitializer;
	}

	public ServerProcessor processor() {
		return processor;
	}

	public HuffmanCodec huffman() {
		return huffman;
	}

	public UIDProvider uidProvider() {
		return uidProvider;
	}

	public static void main(String[] args) {
		logger.info("Starting server...");
		
		try {
			//Config c = ConfigFactory.parseFile(new File("server.conf"));
			//new GameServer(c, new File(c.getString("server.filestore"))).start();
			new GameServer(/*c, */new File("./data/filestore")).start();
		} catch (Exception e) {
			logger.fatal("Server has died unexpectedly", e);
		}
	}

}
