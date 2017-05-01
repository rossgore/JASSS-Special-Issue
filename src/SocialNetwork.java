import java.util.Random;

import org.graphstream.algorithm.generator.*;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;


public class SocialNetwork {

	// this belongs to the class, not instances of the class
	public static String[] NETWORK_TYPE_LIST = {"Small World", "Preferential Attachment", "Random", "Ego"};
	public static int SMALL_WORLD = 0;
	public static int PREF_ATTACH = 1;
	public static int RANDOM = 2;
	public static int EGO = 3;

	public static String[] WEIGHT_TYPE_LIST = {"Constant", "Uniform"};
	public static int CONSTANT = 0;
	public static int UNIFORM = 1;

	/**
	 * For those with a graph/networks background see below for a description of the types
	 * Small World - Watts-Strogatz - 
	 *  http://graphstream-project.org/doc/Generators/Dorogovtsev-Mendes-generator/
	 * Preferential Attachment - Barabasi-Albert - 
	 * http://graphstream-project.org/doc/Generators/Barabasi-Albert-Preferential-Attachment-generator/
	 * Random - http://graphstream-project.org/doc/Generators/Random-graph-generator/
	 */

	public String networkType;
	public double [] networkParams;

	public String weightType;
	public double [] weightParams;

	public Graph graph;
	public BaseGenerator gen;

	double [][] network;

	public SocialNetwork() // default this is what we create
	{
		networkType = NETWORK_TYPE_LIST[SMALL_WORLD];
		graph = new SingleGraph("Default Network - Small World");

		// just a set of typical values for a small world network in the default constructor
		// nothing meaningful
		networkParams = new double[3];
		networkParams[0] = 20;
		networkParams[1] = 2;
		networkParams[2] = 0.5;

		int numberOfNodes = (int) networkParams[0];
		int nearestNeighborConnectivity = (int) networkParams[1];
		double probabilityOfConnect = networkParams[2];

		gen = new WattsStrogatzGenerator(numberOfNodes, nearestNeighborConnectivity, probabilityOfConnect);	

		gen.addSink(graph);
		gen.begin();
		while(gen.nextEvents()) {}
		gen.end();

		for(Edge e:graph.getEachEdge()) {
			e.setAttribute("weight", Math.random());
		}

		network = new double[graph.getNodeCount()][graph.getNodeCount()];

		for(Node n:graph.getEachNode()) {
			for(Edge e: n.getEachEdge()) {
				int x = Integer.parseInt(n.getId());
				int y = Integer.parseInt(e.getOpposite(n).getId());
				network[x][y] = Double.parseDouble(e.getAttribute("weight").toString());
			}
		}

	}

	public SocialNetwork(String graphType, double[] netParams, long randomSeed)
	{
		int typeIndex = 0;
		for (int i=0; i<NETWORK_TYPE_LIST.length; i++)
		{
			if (graphType.equals(NETWORK_TYPE_LIST[i]))
			{
				typeIndex = i;
			}
		}
		if (typeIndex == EGO)
		{
			networkType = NETWORK_TYPE_LIST[EGO];
			int numberOfNodes = (int) netParams[0];
			EgoNetwork enet = new EgoNetwork(numberOfNodes);
			
			graph = new SingleGraph(networkType);
			network = enet.getNetworkAsMatrix();
			for (int i=0; i<network.length; i++)
			{
				graph.addNode(i+"");
			}
			for (int i=0; i<network.length; i++)
			{
				for (int j=0; j<network[0].length; j++)
				{
					if (network[i][j]>0)
					{
						graph.addEdge(i+"-"+j, ""+i, ""+j, true);	
					}
				}
				for(Edge e:graph.getEachEdge()) {
					Node node0 = e.getNode0();
					Node node1 = e.getNode1();
					int row = Integer.parseInt(node0.getId());
					int col = Integer.parseInt(node1.getId());
					e.setAttribute("weight", network[row][col]);
				}
			}
			
		}
		else if (typeIndex == SMALL_WORLD)
		{
			networkType = NETWORK_TYPE_LIST[SMALL_WORLD];
			graph = new SingleGraph(networkType);
			int numberOfNodes = (int) netParams[0];
			int nearestNeighborConnectivity = (int) netParams[1];
			double probabilityOfConnect = netParams[2];

			gen = new WattsStrogatzGenerator(numberOfNodes, nearestNeighborConnectivity, probabilityOfConnect);	
			gen.setRandomSeed(randomSeed);
			gen.addSink(graph);
			gen.begin();
			while(gen.nextEvents()) {}
			gen.end();

			network = new double[graph.getNodeCount()][graph.getNodeCount()];
		}
		else if (typeIndex == PREF_ATTACH)
		{
			networkType = NETWORK_TYPE_LIST[PREF_ATTACH];
			graph = new SingleGraph(networkType);

			int numberOfNodes = (int) netParams[0];
			int maxLinksPerStep = (int) netParams[1];

			gen = new BarabasiAlbertGenerator(maxLinksPerStep);
			gen.setRandomSeed(randomSeed);
			// Generate 100 nodes:
			gen.addSink(graph); 
			gen.begin();

			for(int i=0; i<numberOfNodes; i++) {
				gen.nextEvents();
			}

			gen.end();


		}
		else if (typeIndex == RANDOM)
		{
			networkType = NETWORK_TYPE_LIST[RANDOM];
			graph = new SingleGraph(networkType);

			int numberOfNodes = (int) netParams[0];
			int avgDegree = (int) netParams[1];

			gen = new RandomGenerator(avgDegree);
			gen.setRandomSeed(randomSeed);
			gen.addSink(graph);
			gen.begin();
			for(int i=0; i<numberOfNodes; i++) {
				gen.nextEvents();
			}
			gen.end();
			network = new double[graph.getNodeCount()][graph.getNodeCount()];
		}
		
		int nodeNum=0;
		for(Node n:graph.getEachNode()) {
	        n.setAttribute("ui.label", ""+nodeNum);
	        nodeNum++;
	    }
	}

	public void setWeights(String wType, double [] wParams, long randomSeed)
	{
		if (this.networkType.equals(NETWORK_TYPE_LIST[3]))
		{
			// ego network weights are set during its generation
			return;
		}
		int weightIndex = 0;
		for (int i=0; i<WEIGHT_TYPE_LIST.length; i++)
		{
			if (wType.equals(WEIGHT_TYPE_LIST[i]))
			{
				weightIndex = i;
			}
		}
		Random rand = new Random(randomSeed);
		network = new double[graph.getNodeCount()][graph.getNodeCount()];
		if ( weightIndex== CONSTANT)
		{
			double constantValue = wParams[0];
			for(Edge e:graph.getEachEdge()) {
				e.setAttribute("weight", constantValue);
			}

		}
		else if  (weightIndex == UNIFORM)
		{
			double minValue = wParams[0];
			double maxValue = wParams[1];
			for(Edge e:graph.getEachEdge()) {
				double randValue = (minValue) + rand.nextDouble()*(maxValue-minValue);
				e.setAttribute("weight", randValue);
			}
		}

		// fill matrix with edge weights
		for(Node n:graph.getEachNode()) {
			for(Edge e: n.getEachEdge()) {
				int x = Integer.parseInt(n.getId());
				int y = Integer.parseInt(e.getOpposite(n).getId());
				network[x][y] = Double.parseDouble(e.getAttribute("weight").toString());
			}
		}

	}

	public void display()
	{
		graph.display();
	}

	public void printWeightMatrix()
	{
		for (int i=0; i<network.length; i++)
		{
			for (int j=0; j<network[0].length; j++)
			{
				System.out.print(network[i][j]+", ");
			}
			System.out.println();
		}
	}
}
