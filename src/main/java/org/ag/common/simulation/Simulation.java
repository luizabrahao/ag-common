package org.ag.common.simulation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.jcip.annotations.NotThreadSafe;

import org.ag.common.agent.Agent;
import org.ag.common.renderer.EnvironmentElementsRenderer;
import org.ag.common.renderer.ExploratedEnvironmentRenderer;
import org.ag.common.renderer.Renderer;
import org.ag.common.renderer.RendererManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulation encapsulates all the basic features that are needed for using
 * agents to run an experiment. It holds an environment, which size is specified
 * by the user at construction time, a list of agents that are to take part in
 * the simulation and other infrastructure elements such as executor services.
 * 
 * @author Luiz Abrahao <luiz@luizabrahao.com>
 * 
 *         An example of simulation:
 * 
 *         <pre>
 * {@code
 * 	public static void main(String[] args) {
 * 		final Agent a01 = new TestTaskAgent("a01");
 * 		final Agent a02 = new TestTaskAgent("a02");
 * 	
 * 		final Simulation simulation = new Simulation("./target/", 200, 200, 20);
 * 	
 * 		simulation.addAgentMiddleEnvironment(a01);
 * 		simulation.addAgentMiddleEnvironment(a02);
 * 	
 * 		simulation.scheduleEnvironmentExploredRenderer("explored-5.png",
 * 				Color.DARK_GRAY, Color.white, 5, TimeUnit.SECONDS);
 * 			
 * 		simulation.scheduleEnvironmentExploredRenderer("explored-10.png",
 * 				Color.DARK_GRAY, Color.white, 10, TimeUnit.SECONDS);
 * 	
 * 		simulation.run(10, TimeUnit.SECONDS);
 * }
 * }
 * </pre>
 */
@NotThreadSafe
public class Simulation {
	private static final Logger logger = LoggerFactory
			.getLogger(Simulation.class);

	protected final ScheduledExecutorService executor;
	private final int poolSize;
	private final String basePath;

	private final Environment environment;
	private final List<Agent> agents;
	private final List<Future<Void>> tasks;
	private final RendererManager rendererManager;

	/**
	 * The default constructor for a simulation. Defines the environment's
	 * dimension and another parameters like the base path used for the
	 * simulation and the size of the thread-pool
	 * 
	 * @param basePath
	 *            The path to a folder in the file system that will be used as
	 *            reference when saving files
	 * @param environment
	 *            The environment that the simulation will use
	 * @param poolSize
	 *            The number of agents that will be running at the same time.
	 *            (thread-pool size)
	 */
	public Simulation(final String basePath, Environment environment,
			final int poolSize) {

		this.poolSize = poolSize;
		this.executor = Executors.newScheduledThreadPool(poolSize);

		if (basePath.lastIndexOf("/") != basePath.length() - 1) {
			this.basePath = basePath + "/";

		} else {
			this.basePath = basePath;
		}

		this.environment = environment;
		this.agents = new ArrayList<Agent>();
		this.tasks = new ArrayList<Future<Void>>();
		this.rendererManager = new RendererManager(basePath);

	}

	public int getPoolSize() {
		return poolSize;
	}

	public int getNumberOfAgents() {
		return this.agents.size();
	}
	
	public String getBasePath() {
		return this.basePath;
	}

	/**
	 * Add the agent to the environment at the line and column requested. If the
	 * line or column specified are less then 0 or greater than the height or
	 * width of the environment, the agent will not be added and an error
	 * message will be logged to alert the user.
	 * 
	 * @param agent
	 *            Agent to be added to the environment
	 * @param line
	 *            Vertical position of the agent in the environment
	 * @param column
	 *            Horizontal position of the agent in the environment
	 */
	public void addAgent(final Agent agent, final int line, final int column) {
		this.agents.add(agent);
		this.environment.placeAgentAt(agent, line, column);
	}

	/**
	 * Add the agent at the middle of the environment.
	 * 
	 * @param agent
	 *            Agent to be added to the environment
	 */
	public void addAgentMiddleEnvironment(final Agent agent) {
		this.agents.add(agent);
		this.environment.placeAgentAtTheMiddle(agent);
	}

	/**
	 * Schedules a custom Render to be executed by the renderer executor service
	 * after the delay specified.
	 * 
	 * @param renderer
	 *            Renderer to be scheduled
	 * @param delay
	 *            Amount of time to delay the execution
	 * @param unit
	 *            Delay's time unit
	 */
	public void scheduleRenderer(final Renderer renderer,
			final String filename, final long delay, final TimeUnit unit) {
		
		rendererManager.scheduleRender(renderer, filename, delay, unit);
	}

	/**
	 * Schedules an <em>ExploratedEnvironmentRenderer</em>. This is a handy
	 * method, because this renderer is likely to be used very often.
	 * 
	 * @see ExploratedEnvironmentRenderer
	 * 
	 * @param filename
	 *            The filename for the rendered image. Not the full path.
	 * @param delay
	 *            Amount of time to delay the execution
	 * @param unit
	 *            Delay's time unit
	 */
	public void scheduleEnvironmentExploredRenderer(final String filename,
			final long delay, final TimeUnit unit) {
		
		final Renderer r = new ExploratedEnvironmentRenderer(filename, environment);
		this.scheduleRenderer(r, filename, delay, unit);
	}

	/**
	 * Schedules an <em>ExploratedEnvironmentRenderer</em> that uses custom
	 * colours to represent the environment and the nodes that have been visited
	 * by the agents. This is a handy method, because this renderer is likely to
	 * be used very often.
	 * 
	 * @see ExploratedEnvironmentRenderer
	 * 
	 * @param filename
	 *            The filename for the rendered image. Not the full path.
	 * 
	 * @param colourEnv
	 *            Colour that the environment will be painted with
	 * 
	 * @param colourVisited
	 *            Colour that the nodes that have been visited by agents will be
	 *            painted with
	 * 
	 * @param delay
	 *            Amount of time to delay the execution
	 * @param unit
	 *            Delay's time unit
	 */
	public void scheduleEnvironmentExploredRenderer(final String filename,
			final Color colourEnv, final Color colourVisited, final long delay,
			final TimeUnit unit) {

		Renderer r = new ExploratedEnvironmentRenderer(filename, environment, colourEnv,
				colourVisited);

		this.scheduleRenderer(r, filename, delay, unit);
	}

	public void scheduleEnvironmentElementRenderer(final String filename,
			final long delay, final TimeUnit unit) {
		
		Renderer r = new EnvironmentElementsRenderer(filename, environment);
		this.scheduleRenderer(r, filename, delay, unit);
	}

	public void composeImage(String name, String[] imagesNames) {
		this.rendererManager.writeComposeImage(name, imagesNames);
	}

	/**
	 * <p>
	 * Firstly all the agents are submitted to the agents' executor service,
	 * after than an anonymous task is scheduled to shutdown the service after
	 * the amount of time specified by the parameters <em>time</em> and
	 * <em>unit</em>. This shutdown is executed in an active form by
	 * interrupting the active tasks.
	 * </p>
	 * 
	 * <p>
	 * After that the renderers are scheduled and the renderers' executors
	 * service is shutdown in a passive mode, that is, no current task is
	 * interrupted. If after the time specified by the
	 * <em>renderersTimeoutInSeconds</em> some renderers ares still running, the
	 * simulation will try to stop them with an active shutdown, interrupting
	 * the runnung tasks.
	 * </p>
	 * 
	 * @param time
	 *            Amount of time to run the simulation for, the renderers do not
	 *            enter in this number, their have their own life-cycle.
	 * @param unit
	 *            Time unit for the simulation execution
	 */
	public void run(final long time, final TimeUnit unit) {
		logger.info("Starting simulation...");

		for (Agent agent : agents) {
			tasks.add(executor.submit(agent));
		}

		executor.schedule(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				executor.shutdownNow();

				try {
					if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
						logger.error("Could not stop simulation!");
					}

				} catch (InterruptedException e) {
					executor.shutdownNow();
					Thread.currentThread().interrupt();
				}

				return null;
			}
		}, time, unit);
		
		rendererManager.run();

	}

	protected Environment getEnvironment() {
		return environment;
	}
	
	protected RendererManager getRendererManager() {
		return rendererManager; 
	}
}
