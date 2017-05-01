import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;

import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;


public class EgoNetwork {

	public static int MALE = 1;
	public static int FEMALE = 0;

	public static int SUP = 0;
	public static int SYM = 1;
	public static int ACT = 2;

	public static int KIN = 0;
	public static int NOT_KIN = 1;

	public static double C_K = 95.3275;
	public static double C_NK = 93.8275;

	public static double W_MEAN = 0.3217;
	public static double W_STD = 0.1608;

	public static double S_ALPHA = 4.1;
	public static double S_BETA = 3.49;
	public static double S_LAMBDA = 0;

	public static double B_ALPHA = 205.48;
	public static double B_BETA = 8.5264;
	public static double B_LAMBDA = 0;

	public static double E_K_ALPHA = 1.85;
	public static double E_K_BETA = 2.296;
	public static double E_K_LAMBDA = 0;

	public static double E_NK_MEAN = 0.485;
	public static double E_NK_STD = 0.17;

	public static double E_K_SUP_L = 0.8582;
	public static double E_K_SYM_L = 0.6852;

	public static double E_NK_SUP_L = 0.8185;
	public static double E_NK_SYM_L = 0.7247;

	public static double E_UPPER_BOUND = 1;
	public static double E_LOWER_BOUND = 0;

	public static double[] AD_M_SUP_SYM_DIST = {15.98, 33.24, 84.85};
	public static double[] AD_F_SUP_SYM_DIST = {16.64, 41.36, 55.50};

	public static double[] AD_M_ACT_DIST = {9.70, 24.93, 69.34};
	public static double[] AD_F_ACT_DIST = {14.68, 29.21, 62.12};

	private int mSize = 400;
	private double[][] ego;

	public EgoNetwork()
	{
		ego = new double[mSize][mSize];
		for (int n=0; n<ego.length; n++)
		{
			int gender = genderBernulli();
			double s_sym = getS();
			double w = getW();
			double s_sup = s_sym * w;
			double bdg = getB();

			boolean done = false;
			int i=0;
			double tot = 0;
			while (!done)
			{
				int layer = selectLayer(i, s_sym, s_sup);
				double sampleFromADDist = Math.random()*100;
				int a = getA(layer, gender, sampleFromADDist);
				int d = getD(layer, gender, sampleFromADDist);
				double e = getE(d, layer);
				double t = getH(d,e);
				if ((t/2)<(bdg-tot))
				{
					createRelationship(e, a, gender, n);
					tot += t;
					i++;
				}
				else
				{
					done = true;
				}
			}
		}
	}

	public EgoNetwork(int pSize)
	{
		mSize = pSize;
		ego = new double[mSize][mSize];
		for (int n=0; n<ego.length; n++)
		{
			int gender = genderBernulli();
			double s_sym = getS();
			double w = getW();
			double s_sup = s_sym * w;
			double bdg = getB();

			boolean done = false;
			int i=0;
			double tot = 0;
			while (!done)
			{
				int layer = selectLayer(i, s_sym, s_sup);
				double sampleFromADDist = Math.random()*100;
				int a = getA(layer, gender, sampleFromADDist);
				int d = getD(layer, gender, sampleFromADDist);
				double e = getE(d, layer);
				double t = getH(d,e);
				if ((t/2)<(bdg-tot))
				{
					createRelationship(e, a, gender, n);
					tot += t;
					i++;
				}
				else
				{
					done = true;
				}
			}
			//System.out.println(i);
		}
	}

	public void createRelationship(double emotionalCloseness, int alterGender, int myGender, int myId)
	{
		double weight = emotionalCloseness;
		if (alterGender == myGender)
		{
			weight = emotionalCloseness * emotionalCloseness;
		}
		int partnerId = (int) (Math.random()*this.ego.length);
		while (partnerId == myId || ego[myId][partnerId] != 0)
		{
			partnerId = (int) (Math.random()*this.ego.length);
		}
		ego[myId][partnerId] = weight;
	}

	public static int selectLayer(int curLayer, double maxSymLayer, double maxSupLayer)
	{
		if (curLayer < Math.floor(maxSupLayer))
		{
			return SUP;
		}
		else if (curLayer < Math.floor(maxSymLayer))
		{
			return SYM;
		}
		else
		{
			return ACT;
		}
	}

	public static int genderBernulli()
	{
		double coin = Math.random();
		if (coin > 0.5)
		{
			return MALE;
		}
		else
		{
			return FEMALE;
		}
	}

	public static double getS()
	{
		return nextGamma(S_ALPHA, S_BETA, S_LAMBDA);
	}

	public static double getW()
	{
		Random rand = new Random();
		return Math.max(0, W_MEAN + W_STD * rand.nextGaussian());

	}

	public static double getB()
	{
		return nextGamma(B_ALPHA, B_BETA, B_LAMBDA);
	}

	public static int getA(int gender, int layer, double sample)
	{
		if (layer == SYM || layer == SUP)
		{
			if (gender == MALE)
			{
				if (sample <= AD_M_SUP_SYM_DIST[0])
				{
					return MALE;
				}
				else if (sample <= AD_M_SUP_SYM_DIST[1])
				{
					return FEMALE;
				}
				else if (sample <= AD_M_SUP_SYM_DIST[2])
				{
					return MALE;
				}
				else
				{
					return FEMALE;
				}
			}
			else // gender == female
			{
				if (sample <= AD_F_SUP_SYM_DIST[0])
				{
					return MALE;
				}
				else if (sample <= AD_F_SUP_SYM_DIST[1])
				{
					return FEMALE;
				}
				else if (sample <= AD_F_SUP_SYM_DIST[2])
				{
					return MALE;
				}
				else
				{
					return FEMALE;
				}
			}
		}
		else // layer == ACT
		{
			if (gender == MALE)
			{
				if (sample <= AD_M_ACT_DIST[0])
				{
					return MALE;
				}
				else if (sample <= AD_M_ACT_DIST[1])
				{
					return FEMALE;
				}
				else if (sample <= AD_M_ACT_DIST[2])
				{
					return MALE;
				}
				else
				{
					return FEMALE;
				}
			}
			else // gender == female
			{
				if (sample <= AD_F_ACT_DIST[0])
				{
					return MALE;
				}
				else if (sample <= AD_F_ACT_DIST[1])
				{
					return FEMALE;
				}
				else if (sample <= AD_F_ACT_DIST[2])
				{
					return MALE;
				}
				else
				{
					return FEMALE;
				}
			}
		}
	}

	public static int getD(int gender, int layer, double sample)
	{
		if (layer == SYM || layer == SUP)
		{
			if (gender == MALE)
			{
				if (sample <= AD_M_SUP_SYM_DIST[0])
				{
					return KIN;
				}
				else if (sample <= AD_M_SUP_SYM_DIST[1])
				{
					return KIN;
				}
				else if (sample <= AD_M_SUP_SYM_DIST[2])
				{
					return NOT_KIN;
				}
				else
				{
					return NOT_KIN;
				}
			}
			else // gender == female
			{
				if (sample <= AD_F_SUP_SYM_DIST[0])
				{
					return KIN;
				}
				else if (sample <= AD_F_SUP_SYM_DIST[1])
				{
					return KIN;
				}
				else if (sample <= AD_F_SUP_SYM_DIST[2])
				{
					return NOT_KIN;
				}
				else
				{
					return NOT_KIN;
				}
			}
		}
		else // layer == ACT
		{
			if (gender == MALE)
			{
				if (sample <= AD_M_ACT_DIST[0])
				{
					return KIN;
				}
				else if (sample <= AD_M_ACT_DIST[1])
				{
					return KIN;
				}
				else if (sample <= AD_M_ACT_DIST[2])
				{
					return NOT_KIN;
				}
				else
				{
					return NOT_KIN;
				}
			}
			else // gender == female
			{
				if (sample <= AD_F_ACT_DIST[0])
				{
					return KIN;
				}
				else if (sample <= AD_F_ACT_DIST[1])
				{
					return KIN;
				}
				else if (sample <= AD_F_ACT_DIST[2])
				{
					return NOT_KIN;
				}
				else
				{
					return NOT_KIN;
				}
			}
		}
	}


	public static double getE(int kinship, int layer)
	{
		double result = -1;
		Random rand = new Random();

		if (kinship == KIN )
		{
			if (layer == SUP)
			{
				while (result < E_K_SUP_L || result > E_UPPER_BOUND)
				{
					result = nextGamma(E_K_ALPHA, E_K_BETA, E_K_LAMBDA);

				}
			}
			else if (layer == SYM)
			{
				while (result < E_K_SYM_L || result > E_K_SUP_L)
				{
					result = nextGamma(E_K_ALPHA, E_K_BETA, E_K_LAMBDA);


				}
			}
			else
			{
				while (result < E_LOWER_BOUND || result > E_K_SYM_L)
				{
					result = nextGamma(E_K_ALPHA, E_K_BETA, E_K_LAMBDA);
				}
			}
		}
		else
		{
			if (layer == SUP)
			{
				while (result < E_NK_SUP_L || result > E_UPPER_BOUND)
				{
					result = E_NK_MEAN + E_NK_STD*rand.nextGaussian();
				}
			}
			else if (layer == SYM)
			{
				while (result < E_NK_SYM_L || result > E_NK_SUP_L)
				{
					result = E_NK_MEAN + E_NK_STD*rand.nextGaussian();
				}
			}
			else
			{
				while (result < E_LOWER_BOUND || result > E_NK_SYM_L)
				{
					result = E_NK_MEAN + E_NK_STD*rand.nextGaussian();
				}
			}
		}
		return result;
	}
	public static double getH(int kinship, double e)
	{

		if (kinship == KIN)
		{
			return Math.pow(C_K, e) - 0.5;
		}
		else
		{
			return Math.pow(C_NK, e) + 1;
		}
	}
	
	public double[][] getNetworkAsMatrix()
	{
		return ego;
	}

	public static double nextGamma(double alpha, double beta,double lambda) {
		double gamma=0;
		if (alpha <= 0 || beta <= 0) {
			throw new IllegalArgumentException ("alpha and beta must be strictly positive.");
		}
		if (alpha < 1) {
			double b,p;
			boolean flag=false;
			b=1+alpha*Math.exp(-1);
			while(!flag) {
				p=b*Math.random();
				if (p>1) {
					gamma=-Math.log((b-p)/alpha);
					if (Math.random()<=Math.pow(gamma,alpha-1)) flag=true;
				}
				else {
					gamma=Math.pow(p,1/alpha);
					if (Math.random()<=Math.exp(-gamma)) flag=true;
				}
			}
		}
		else if (alpha == 1) {
			gamma = -Math.log (Math.random());
		} else {
			double y = -Math.log (Math.random());
			while ( Math.random() > Math.pow (y * Math.exp (1 - y), alpha - 1))
				y = -Math.log (Math.random());
			gamma = alpha * y;
		}
		return beta*gamma+lambda;
	}
	
	public static double getMean(double[] list)
	{
		double sum = 0;
		for (int i=0; i<list.length; i++)
		{
			sum+=list[i];
		}
		return sum/list.length;
	}
	
	public static double getOutDegree(double [][] matrix)
	{
		int degree = 0;
		for (int i=0; i<matrix.length; i++)
		{
			for (int j=0; j<matrix[0].length; j++)
			{
				if (matrix[i][j]!=0)
				{
					degree++;
				}
			}
		}
		return degree;
	}
	
	public static Graph createGraphFromMatrix(double [][] network)
	{
		Graph graph = new SingleGraph("Graph");
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
				e.setAttribute("weight", 1);
			}
		}
		return graph;
	}
	
	public static double getAvgSP(double[][] matrix)
	{
		Graph thisGraph = createGraphFromMatrix(matrix);
		
		double[][] spForThisGraph = new double[matrix.length][matrix[0].length];
		ArrayList<Node> myGraphNodes = new ArrayList<Node>();
		for(Edge e:thisGraph.getEachEdge()) {
			Node node0 = e.getNode0();
			if (myGraphNodes.contains(node0)==false)
			{
				myGraphNodes.add(node0);
			}
			Node node1 = e.getNode1();
			if (myGraphNodes.contains(node1)==false)
			{
				myGraphNodes.add(node1);
			}
		}
		
		for (int i=0; i<myGraphNodes.size(); i++)
		{
			Node myNode = myGraphNodes.get(i);
			Dijkstra dijkstra = new Dijkstra();
	 		dijkstra.init(thisGraph);
	 		dijkstra.setSource(myNode);
	 		dijkstra.compute();
			for (int j=0; j<myGraphNodes.size(); j++)
			{
				spForThisGraph[i][j] = dijkstra.getPathLength(thisGraph.getNode(myGraphNodes.get(j).getId()));
			}
		}
		return getSPMean(spForThisGraph);

	}
	
	public static double getSPMean(double[][] matrix)
	{
		double sum = 0;
		double totalPaths = 0;
		for (int i=0; i<matrix.length; i++)
		{
			for (int j=0; j<matrix.length; j++)
			{
				if (matrix[i][j] != Double.POSITIVE_INFINITY && matrix[i][j] !=0)
				{
					sum+=matrix[i][j];
					totalPaths++;
				}
			}
		}
		return sum/totalPaths;
	}
	
	public static double getAvgOutDegree(double[][] matrix)
	{
		return getOutDegree(matrix)/matrix.length;
	}
	
	public static double[][] getConnectedMatrix(int size)
	{
		double[][] matrix = new double[size][size];
		for (int i=0; i<size; i++)
		{
			for (int j=0; j<size; j++)
			{
				matrix[i][j] = 0;
			}
		}
		return matrix;
	}

	public static void main(String [] args)
	{
		DecimalFormat df = new DecimalFormat("#.###");
		
		
		// set min search size
		int minNetSize = 3150;
		// set max search size
		int maxNetSize = 7500;
		
		int numberOfSamples = 10;
		
		ArrayList<Integer> sizeList = new ArrayList<Integer>();
		System.out.println("Size,AvgAvgDegree,AvgAvgSP");
		// generate all network sizes this will take a while
		for (int i=minNetSize; i<=maxNetSize; i=i+50)
		{
			sizeList.add(i);
			
			double [] avgDegreeList = new double[numberOfSamples];
			double [] avgSPList = new double[numberOfSamples];
			
			for (int j=0; j<numberOfSamples; j++)
			{
				EgoNetwork en = new EgoNetwork(i);
				//System.out.println("i= "+i+", j="+j+" Generated an ego network of size: "+i);
				double[][] enAsMatrix = en.getNetworkAsMatrix();
				
				//double[][] enAsMatrix = getConnectedMatrix(i);
				avgDegreeList[j] = getAvgOutDegree(enAsMatrix);
				avgSPList[j] = getAvgSP(enAsMatrix);
			}
			System.out.println(i+","+df.format(getMean(avgDegreeList))+","+df.format(getMean(avgSPList)));
		}
	}
}
