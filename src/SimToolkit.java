import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


public class SimToolkit {

	private static String isspInputFileDirectory = "/Users/ross/src/JASSS-Special-Issue/inputFiles/";
	private static String hdiInputFileDirectory = "/Users/ross/src/JASSS-Special-Issue/inputFiles/hdiData/";
	private static String countryListFile = "/Users/ross/src/JASSS-Special-Issue/calibrationInformation/countries.csv";
	private static String evalCountryListFile = "/Users/ross/src/JASSS-Special-Issue/calibrationInformation/evalcountries.csv";
	private static String robustCountryListFile = "/Users/ross/src/JASSS-Special-Issue/calibrationInformation/robustcountries.csv";
	private static String stateSpaceOutFile = "/Users/ross/src/JASSS-Special-Issue/stateOutput/";

	public static int COUNTRY_INDEX=0;
	public static int EDUCATION_INDEX=1;
	public static int SB_INDEX=2;
	public static int RF_INDEX=3;
	public static int BIG_INDEX=4;
	public static int RP_INDEX=5;

	public static double BeliefInGodIntercept = 0.1357;
	public static double SupernaturalBeliefsIntercept = 1.000559;


	public static double RF_SB_Coefficient =  -0.433416;
	public static double RP_SB_Coefficient =  -0.493820;
	public static double BIG_SB_Coefficient = -0.083256;
	public static double ED_SB_Coefficient = 0.002820;
	public static double RP_BIG_Coefficient= 0.3416;

	public static String readFile(String path, Charset encoding) 
	{
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(path));
			return new String(encoded, encoding);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "THIS SHOULD NEVER BE EXECUTED";

	}

	public static double getAgentsStats(ArrayList<Agent> pAgentList, int pAttributeSelector, int pStatSelector, double stretchFactor)
	{
		double [] data = new double[pAgentList.size()];
		double answer = 0;
		for (int i=0; i<pAgentList.size(); i++)
		{
			if (pAttributeSelector == SB_INDEX)
			{
				data[i] = pAgentList.get(i).getSupernaturalBeliefs();
			}
			else if (pAttributeSelector == RF_INDEX)
			{
				data[i] = pAgentList.get(i).getReligiousFormation();
			}
			else if (pAttributeSelector == BIG_INDEX)
			{
				data[i] = pAgentList.get(i).getBeliefInGod();
			}
			else if (pAttributeSelector == RP_INDEX)
			{
				data[i] = pAgentList.get(i).getReligiousPractice();
			}
		}
		Statistics agentsStats = new Statistics(data);

		if (pStatSelector == Statistics.MEAN)
		{
			answer = agentsStats.getMean();
		}
		else if (pStatSelector == Statistics.SD)
		{
			if (stretchFactor > 0)
			{
				data = normalize(data, agentsStats.getMean(), stretchFactor);
				agentsStats = new Statistics(data);
				answer = agentsStats.getStdDev();
			}
			else
			{
				answer = agentsStats.getStdDev();
			}

		}
		return answer;
	}

	public static double min(double []ra)
	{
		double min = Double.MAX_VALUE;
		for (int i=0; i<ra.length; i++)
		{
			if (ra[i] < min)
			{
				min = ra[i];
			}
		}
		return min;
	}

	public static void writeToFile(double[][][] array, String filename)
	{
		for (int i=0; i<array.length; i++)
		{
			PrintWriter out;
			try {
				out = new PrintWriter(stateSpaceOutFile+"agent"+i+"-"+filename);

				for (int j=0; j<array[i].length; j++)
				{
					String text = array[i][j][0]+","+array[i][j][1]+","+array[i][j][2]+","+array[i][j][3];
					out.println(text);
				}
				out.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}
	
	public static void writeToFile2D(double[][] array, String filename)
	{
		PrintWriter out;
		try {
			out = new PrintWriter(stateSpaceOutFile+"country-"+filename);
			for (int i=0; i<array.length; i++)
			{
					String text = array[i][0]+","+array[i][1]+","+array[i][2]+","+array[i][3];
					out.println(text);
			}
				out.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}

	public static double max(double []ra)
	{
		double max = Double.MIN_VALUE;
		for (int i=0; i<ra.length; i++)
		{
			if (ra[i] > max)
			{
				max = ra[i];
			}
		}
		return max;
	}

	public static double[] normalize(double[] ra, double mean, double strectchFactor) {
		for (int i=0; i < ra.length; i++)
		{
			if (ra[i] > mean)
			{
				ra[i] = ra[i] + ra[i]*strectchFactor;
			}
			else
			{
				if (ra[i] < mean)
					ra[i] = ra[i] - ra[i]*strectchFactor ;
			}
		}
		return ra;
	}

	public static SocialNetwork createSocialNetwork(int pNumOfAgents){
		String sw_type = "Ego";
		double[] sw_params = {pNumOfAgents};
		long seed = 12;
		SocialNetwork socialNetwork = new SocialNetwork(sw_type, sw_params, seed);
		return socialNetwork;
	}


	public static double createSecularInstituion(String pCountry, String pYear)
	{

		return Double.parseDouble(readFile(hdiInputFileDirectory+pCountry+pYear+".dat", Charset.defaultCharset()));
	}

	public static boolean containsNan(double[] numbers)
	{
		boolean answer = false;
		for (int i=0; i<numbers.length; i++)
		{
			if (Double.isNaN(numbers[i]))
			{
				return true;
			}
		}
		return answer;
	}

	public static List<String> getCountries()
	{
		String fileContents = readFile(countryListFile, Charset.defaultCharset());
		return Arrays.asList(fileContents.split("\\s*,\\s*"));
	}

	public static List<String> getEvalCountries(boolean robust)
	{
		if (robust)
		{
			String fileContents = readFile(robustCountryListFile, Charset.defaultCharset());
			return Arrays.asList(fileContents.split("\\s*,\\s*"));
		}
		else
		{
			String fileContents = readFile(evalCountryListFile, Charset.defaultCharset());
			return Arrays.asList(fileContents.split("\\s*,\\s*"));
		}

	}

	public static double createReligiousInstitution(ArrayList<Agent> pAgentList, double pReligiousPracticeThreshold)
	{
		double numberOverThreshold=0;
		double populationSize=0;
		for (int i=0; i<pAgentList.size(); i++)
		{
			double religiousPractice = pAgentList.get(i).getReligiousPractice();
			if (religiousPractice > pReligiousPracticeThreshold)
			{
				numberOverThreshold++;
			}
			populationSize++;
		}
		return (numberOverThreshold/populationSize);
	}

	public static double cobbDouglasFunction(double h, double t, double beta)
	{
		return (Math.pow(h, beta))*Math.pow(t, (1-beta));
	}

	public static double[] getMeanStatsForPopulation(String pCountry, String pYear)
	{
		File inputFile = new File(isspInputFileDirectory+pCountry+pYear+".csv");
		Scanner scanner = null;
		ArrayList<Double> rpValueList = new ArrayList<Double>();
		ArrayList<Double> sbValueList = new ArrayList<Double>();
		ArrayList<Double> bigValueList = new ArrayList<Double>();
		double[] answers = new double[3];
		try {
			scanner = new Scanner(inputFile);

			// burn the header
			String line = scanner.nextLine();

			while (scanner.hasNextLine())
			{
				line = scanner.nextLine();
				List<String> params = Arrays.asList(line.split("\\s*,\\s*"));
				rpValueList.add(Double.parseDouble(params.get(RP_INDEX)));
				sbValueList.add(Double.parseDouble(params.get(SB_INDEX)));
				bigValueList.add(Double.parseDouble(params.get(BIG_INDEX)));

			}
			double [] rpArrayVersion = new double[rpValueList.size()];
			double [] sbArrayVersion = new double[sbValueList.size()];
			double [] bigArrayVersion= new double[bigValueList.size()];
			for (int i=0; i<rpArrayVersion.length; i++)
			{
				rpArrayVersion[i] = rpValueList.get(i);
				sbArrayVersion[i] = sbValueList.get(i);
				bigArrayVersion[i] = bigValueList.get(i);
			}
			answers[0] = new Statistics(rpArrayVersion).getMean();
			answers[1] = new Statistics(sbArrayVersion).getMean();
			answers[2] = new Statistics(bigArrayVersion).getMean();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		scanner.close();
		return answers;		
	}

	public static double[] getSDStatsForPopulation(String pCountry, String pYear)
	{
		File inputFile = new File(isspInputFileDirectory+pCountry+pYear+".csv");
		Scanner scanner = null;
		ArrayList<Double> rpValueList = new ArrayList<Double>();
		ArrayList<Double> sbValueList = new ArrayList<Double>();
		ArrayList<Double> bigValueList = new ArrayList<Double>();
		double[] answers = new double[3];
		try {
			scanner = new Scanner(inputFile);

			// burn the header
			String line = scanner.nextLine();

			while (scanner.hasNextLine())
			{
				line = scanner.nextLine();
				List<String> params = Arrays.asList(line.split("\\s*,\\s*"));
				rpValueList.add(Double.parseDouble(params.get(RP_INDEX)));
				sbValueList.add(Double.parseDouble(params.get(SB_INDEX)));
				bigValueList.add(Double.parseDouble(params.get(BIG_INDEX)));

			}
			double [] rpArrayVersion = new double[rpValueList.size()];
			double [] sbArrayVersion = new double[sbValueList.size()];
			double [] bigArrayVersion= new double[bigValueList.size()];
			for (int i=0; i<rpArrayVersion.length; i++)
			{
				rpArrayVersion[i] = rpValueList.get(i);
				sbArrayVersion[i] = sbValueList.get(i);
				bigArrayVersion[i] = bigValueList.get(i);
			}
			answers[0] = new Statistics(rpArrayVersion).getStdDev();
			answers[1] = new Statistics(sbArrayVersion).getStdDev();
			answers[2] = new Statistics(bigArrayVersion).getStdDev();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		scanner.close();
		return answers;		
	}

	public static double getBeliefInGod(double pRP, double pRF)
	{
		return BeliefInGodIntercept + (RP_BIG_Coefficient*pRP);
	}

	public static double getSupernaturalBeliefs(double pRP, double pRF, double pBIG, double pED)
	{
		return SupernaturalBeliefsIntercept+(RP_SB_Coefficient*pRP)+
				(RF_SB_Coefficient*pRF)+
				(BIG_SB_Coefficient*pBIG)+
				(ED_SB_Coefficient*pED);
	}

	public static double geometricMean(double [] numbers)
	{
		double product = 1.0;
		for (int i=0; i<numbers.length; i++)
		{
			product = product * numbers[i];
		}
		return (Math.pow(product, 1.0 / numbers.length));
	}

	public static ArrayList<Agent> createAgents(String pCountry, String pYear, int numberOfAgents)
	{
		File inputFile = new File(isspInputFileDirectory+pCountry+pYear+".csv");
		ArrayList<Agent> agentPopulation = new ArrayList<Agent>();
		Scanner scanner = null;
		try {
			scanner = new Scanner(inputFile);

			// burn the header
			String line = scanner.nextLine();

			while (scanner.hasNextLine())
			{
				line = scanner.nextLine();
				List<String> params = Arrays.asList(line.split("\\s*,\\s*"));
				String country = params.get(COUNTRY_INDEX);
				int education = Integer.parseInt(params.get(EDUCATION_INDEX));
				double supernaturalBeliefs = Double.parseDouble(params.get(SB_INDEX));
				double religiousFormation =  Double.parseDouble(params.get(RF_INDEX));
				double beliefInGod =  Double.parseDouble(params.get(BIG_INDEX));
				double religiousPractice = Double.parseDouble(params.get(RP_INDEX));
				agentPopulation.add(new Agent(supernaturalBeliefs, religiousFormation, beliefInGod,
						religiousPractice, education, country));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		scanner.close();
		int numberToRemove = agentPopulation.size()-numberOfAgents;

		for (int i=0; i<numberToRemove; i++)
		{
			int indexToRemove = (int) (Math.random()*agentPopulation.size());
			agentPopulation.remove(indexToRemove);
		}
		return agentPopulation;
	}

}
