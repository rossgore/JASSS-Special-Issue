import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Simulation {
	private static ArrayList<Agent> agentList;
	private static double secularInsitution;
	private static double religiousInstitution;
	private static SocialNetwork socialNetwork; 

	public static double RELIGIOUS_PRACTICE_THRESHOLD;
	public static double DOUGLAS_COBB_BETA;
	public static double EDUCATION_HOMOPHILY;
	
	
	public static int MEAN_RP_INDEX = 0;
	public static int MEAN_SB_INDEX = 1;
	public static int MEAN_BIG_INDEX = 2;
	public static int HDI_VALUE_INDEX = 3;
	
	public static void main(String args[])
	{
		String [] simArgs = new String[7];
		String startYear = "1998";
		String endYear   = "2008";
		String numberOfAgents = "350";
		List<String> countries = SimToolkit.getEvalCountries(false);
		for (int l=0; l<countries.size(); l++)
		{
			String country = countries.get(l);
			
			simArgs[0] = country;
			simArgs[1] = startYear;
			simArgs[2] = numberOfAgents;
			simArgs[3] = 0.58+"";
			simArgs[4] = endYear;
			simArgs[5] = 0.000942+"";
			simArgs[6] = 0.40+"";
			double [][] state = runSimThruTimeSteps(simArgs);
			SimToolkit.writeToFile2D(state, country+"-"+startYear+"-"+endYear+".csv");
		}
	}

	public static void fitModel(String args[])
	{

		double[][] RPresults = new double[100][11];
		double[][] SBresults = new double[100][11];
		double[][] BIGresults = new double[100][11];
		double[][] ESresults = new double[100][11];
		String [] bestArgs = new String[]{"philippines", "1998", "350", "0.58", "2008", "0.000942", "0.4"};
		String startYear = "1998";
		String endYear   = "2008";
		String numberOfAgents = "350";
		double min = 0.011573009538299686;
		List<String> countries = SimToolkit.getEvalCountries(false);
		double minDCB = 0.000942;
		double maxDCB = 0.000942;
		for (double i=minDCB; i<=maxDCB; i=i+0.000001)
		{
			double minRPT = 0.58;
			double maxRPT = 0.58;
			for (double j=maxRPT; j>=minRPT; j-=0.01)
			{
				double minEH = 0.40;
				double maxEH = 0.40;
				for (double k=minEH; k<=maxEH; k+=0.025)
				{

					
					for (int reps = 0; reps<100; reps++)
					{
						double totalVar = 0;
						double totalSamples=0;
						String [] simArgs = new String[7];
						for (int l=0; l<countries.size(); l++)
						{
							String country = countries.get(l);
							
							simArgs[0] = country;
							simArgs[1] = startYear;
							simArgs[2] = numberOfAgents;
							simArgs[3] = j+"";
							simArgs[4] = endYear;
							simArgs[5] = i+"";
							simArgs[6] = k+"";
							double [] variance = runSimForCountry(simArgs);
							RPresults[reps][l] = variance[MEAN_RP_INDEX];
							SBresults[reps][l] = variance[MEAN_SB_INDEX];
							BIGresults[reps][l] = variance[MEAN_BIG_INDEX];
							ESresults[reps][l] = variance[HDI_VALUE_INDEX];
							
							if (SimToolkit.containsNan(variance)==false)
							{
								System.out.print(Arrays.toString(simArgs));
								System.out.println(", "+Arrays.toString(variance));
								double stat = SimToolkit.geometricMean(variance);
								totalVar+=stat;
								totalSamples++;
							}

						}
						double normalizedVar = totalVar/totalSamples;
						//System.out.println("Comapring: "+normalizedVar +" vs. "+ min);
						if (normalizedVar <= min)
						{
							min = normalizedVar;
							bestArgs = simArgs;
						}
						//System.out.println();
						//System.out.println("Min Found So Far is: "+min);
						//System.out.println("With Stats: "+Arrays.toString(bestArgs));
						//System.out.println();
					}
					System.out.println();
					System.out.println("Min Found So Far is: "+min);
					System.out.println("With Stats: "+Arrays.toString(bestArgs));
					System.out.println();
					for (int reps=0; reps<100; reps++)
					{
						for (int c=0; c<11; c++)
						{
							System.out.print(RPresults[reps][c]+",");
						}
						System.out.println();
					}
					
					System.out.println();
					for (int reps=0; reps<100; reps++)
					{
						for (int c=0; c<11; c++)
						{
							System.out.print(SBresults[reps][c]+",");
						}
						System.out.println();
					}
					System.out.println();
					for (int reps=0; reps<100; reps++)
					{
						for (int c=0; c<11; c++)
						{
							System.out.print(BIGresults[reps][c]+",");
						}
						System.out.println();
					}
					System.out.println();
					/**
					for (int reps=0; reps<100; reps++)
					{
						for (int c=0; c<11; c++)
						{
							System.out.print(ESresults[reps][c]+",");
						}
						System.out.println();
					}
					**/
				}
			}
		}
	}
	
	public static double[][] runSimThruTimeSteps(String args[])
	{
		String countryParam = args[0];
		String startYearParam = args[1];
		int numberOfAgents = Integer.parseInt(args[2]);
		setRELIGIOUS_PRACTICE_THRESHOLD(Double.parseDouble(args[3]));
		setAgentList(SimToolkit.createAgents(countryParam, startYearParam, numberOfAgents));
		setSecularInsitution(SimToolkit.createSecularInstituion(countryParam, startYearParam));
		setReligiousInstitution(SimToolkit.createReligiousInstitution(getAgentList(),
				getRELIGIOUS_PRACTICE_THRESHOLD()));
		setSocialNetwork(SimToolkit.createSocialNetwork(numberOfAgents));
		String endYearParam = args[4];
		int timeLimit = (Integer.parseInt(endYearParam) - Integer.parseInt(startYearParam))*52;
		setDOUGLAS_COBB_BETA(Double.parseDouble(args[5]));
		Simulation.setEDUCATION_HOMOPHILY(Double.parseDouble(args[6]));

		double[][] stateSpace = new double[timeLimit][4];
		
		// adjust weights based on education.
		for (int i=0; i<getAgentList().size(); i++)
		{
			getAgentList().get(i).adjustNetwork(socialNetwork.network[i], 
					agentList, Simulation.getEDUCATION_HOMOPHILY());
		}

		for(int time=0; time<timeLimit; time++)
		{
			for (int i=0; i<getAgentList().size(); i++)
			{
				//1. ask for help
				getAgentList().get(i).askForHelp(getSecularInsitution(), getRELIGIOUS_PRACTICE_THRESHOLD()); 
				// 2. be social - update practice, beliefs and security.
				getAgentList().get(i).beSocial(socialNetwork.network[i], agentList);
				
			}
			// 3. update institutions.
			double levelOfReligiousHelp = 0;
			double levelOfSecularHelp = 0;
			for (int i=0; i<getAgentList().size(); i++)
			{
				if (getAgentList().get(i).isHelpedByReligiousInst())
				{
					levelOfReligiousHelp++;
				}
				if (getAgentList().get(i).isHelpedBySecularInst())
				{
					levelOfSecularHelp++;
				}
			}
			levelOfReligiousHelp = levelOfReligiousHelp/getAgentList().size();
			levelOfSecularHelp = levelOfSecularHelp/getAgentList().size();
			double newSecularInstValue = SimToolkit.cobbDouglasFunction(levelOfSecularHelp, 
					getSecularInsitution(), 
					getDOUGLAS_COBB_BETA());
			setSecularInsitution(newSecularInstValue);

			double newReligiousInstValue = SimToolkit.cobbDouglasFunction(levelOfReligiousHelp, 
					getReligiousInstitution(), 
					getDOUGLAS_COBB_BETA());
			setReligiousInstitution(newReligiousInstValue);
			stateSpace[time][0] = SimToolkit.getAgentsStats(Simulation.getAgentList(), SimToolkit.RP_INDEX, Statistics.MEAN, 0);
			stateSpace[time][1] = SimToolkit.getAgentsStats(Simulation.getAgentList(), SimToolkit.SB_INDEX, Statistics.MEAN, 0);
			stateSpace[time][2] = SimToolkit.getAgentsStats(Simulation.getAgentList(), SimToolkit.BIG_INDEX, Statistics.MEAN, 0);
			stateSpace[time][3] = getSecularInsitution();
		}
		return stateSpace;
	}
	
	public static double[][][] runSimThruAgentSteps(String args[])
	{
		
		String countryParam = args[0];
		String startYearParam = args[1];
		int numberOfAgents = Integer.parseInt(args[2]);
		setRELIGIOUS_PRACTICE_THRESHOLD(Double.parseDouble(args[3]));
		setAgentList(SimToolkit.createAgents(countryParam, startYearParam, numberOfAgents));
		setSecularInsitution(SimToolkit.createSecularInstituion(countryParam, startYearParam));
		setReligiousInstitution(SimToolkit.createReligiousInstitution(getAgentList(),
				getRELIGIOUS_PRACTICE_THRESHOLD()));
		setSocialNetwork(SimToolkit.createSocialNetwork(numberOfAgents));
		String endYearParam = args[4];
		int timeLimit = (Integer.parseInt(endYearParam) - Integer.parseInt(startYearParam))*52;
		setDOUGLAS_COBB_BETA(Double.parseDouble(args[5]));
		Simulation.setEDUCATION_HOMOPHILY(Double.parseDouble(args[6]));

		double[][][] stateSpace = new double[numberOfAgents][timeLimit][4];
		
		// adjust weights based on education.
		for (int i=0; i<getAgentList().size(); i++)
		{
			getAgentList().get(i).adjustNetwork(socialNetwork.network[i], 
					agentList, Simulation.getEDUCATION_HOMOPHILY());
		}

		for(int time=0; time<timeLimit; time++)
		{
			for (int i=0; i<getAgentList().size(); i++)
			{
				//1. ask for help
				getAgentList().get(i).askForHelp(getSecularInsitution(), getRELIGIOUS_PRACTICE_THRESHOLD()); 
				// 2. be social - update practice, beliefs and security.
				getAgentList().get(i).beSocial(socialNetwork.network[i], agentList);
				
				stateSpace[i][time][0] = getAgentList().get(i).weightedAvgOfSN(socialNetwork.network[i], agentList);
				stateSpace[i][time][1] = getAgentList().get(i).getReligiousPractice();
				stateSpace[i][time][2] = getAgentList().get(i).getSupernaturalBeliefs();
				stateSpace[i][time][3] = getAgentList().get(i).getBeliefInGod();
			}
			// 3. update institutions.
			double levelOfReligiousHelp = 0;
			double levelOfSecularHelp = 0;
			for (int i=0; i<getAgentList().size(); i++)
			{
				if (getAgentList().get(i).isHelpedByReligiousInst())
				{
					levelOfReligiousHelp++;
				}
				if (getAgentList().get(i).isHelpedBySecularInst())
				{
					levelOfSecularHelp++;
				}
			}
			levelOfReligiousHelp = levelOfReligiousHelp/getAgentList().size();
			levelOfSecularHelp = levelOfSecularHelp/getAgentList().size();
			double newSecularInstValue = SimToolkit.cobbDouglasFunction(levelOfSecularHelp, 
					getSecularInsitution(), 
					getDOUGLAS_COBB_BETA());
			setSecularInsitution(newSecularInstValue);

			double newReligiousInstValue = SimToolkit.cobbDouglasFunction(levelOfReligiousHelp, 
					getReligiousInstitution(), 
					getDOUGLAS_COBB_BETA());
			setReligiousInstitution(newReligiousInstValue);
		}
		return stateSpace;
	}

	public static double[] runSimForCountry(String args[])
	{
		String countryParam = args[0];
		String startYearParam = args[1];
		int numberOfAgents = Integer.parseInt(args[2]);
		setRELIGIOUS_PRACTICE_THRESHOLD(Double.parseDouble(args[3]));
		setAgentList(SimToolkit.createAgents(countryParam, startYearParam, numberOfAgents));
		setSecularInsitution(SimToolkit.createSecularInstituion(countryParam, startYearParam));
		setReligiousInstitution(SimToolkit.createReligiousInstitution(getAgentList(),
				getRELIGIOUS_PRACTICE_THRESHOLD()));
		setSocialNetwork(SimToolkit.createSocialNetwork(numberOfAgents));
		String endYearParam = args[4];
		int timeLimit = (Integer.parseInt(endYearParam) - Integer.parseInt(startYearParam))*52;
		setDOUGLAS_COBB_BETA(Double.parseDouble(args[5]));
		Simulation.setEDUCATION_HOMOPHILY(Double.parseDouble(args[6]));

		// adjust weights based on education.
		for (int i=0; i<getAgentList().size(); i++)
		{
			getAgentList().get(i).adjustNetwork(socialNetwork.network[i], 
					agentList, Simulation.getEDUCATION_HOMOPHILY());
		}

		for(int time=0; time<timeLimit; time++)
		{
			for (int i=0; i<getAgentList().size(); i++)
			{
				//1. ask for help
				getAgentList().get(i).askForHelp(getSecularInsitution(), getRELIGIOUS_PRACTICE_THRESHOLD()); 
				// 2. be social - update practice, beliefs and security.
				getAgentList().get(i).beSocial(socialNetwork.network[i], agentList);
			}
			// 3. update institutions.
			double levelOfReligiousHelp = 0;
			double levelOfSecularHelp = 0;
			for (int i=0; i<getAgentList().size(); i++)
			{
				if (getAgentList().get(i).isHelpedByReligiousInst())
				{
					levelOfReligiousHelp++;
				}
				if (getAgentList().get(i).isHelpedBySecularInst())
				{
					levelOfSecularHelp++;
				}
			}
			levelOfReligiousHelp = levelOfReligiousHelp/getAgentList().size();
			levelOfSecularHelp = levelOfSecularHelp/getAgentList().size();
			double newSecularInstValue = SimToolkit.cobbDouglasFunction(levelOfSecularHelp, 
					getSecularInsitution(), 
					getDOUGLAS_COBB_BETA());
			setSecularInsitution(newSecularInstValue);

			double newReligiousInstValue = SimToolkit.cobbDouglasFunction(levelOfReligiousHelp, 
					getReligiousInstitution(), 
					getDOUGLAS_COBB_BETA());
			setReligiousInstitution(newReligiousInstValue);
		}

		
		double[] distanceBetweenActualAndPredicted = new double[4];
		/** use this to get mean stats
		double actualES = SimToolkit.createSecularInstituion(countryParam, endYearParam);
		double[] actualRel = SimToolkit.getMeanStatsForPopulation(countryParam, endYearParam);
		
		distanceBetweenActualAndPredicted[HDI_VALUE_INDEX] = Math.abs(actualES-getSecularInsitution());
		distanceBetweenActualAndPredicted[MEAN_RP_INDEX] = Math.abs(actualRel[MEAN_RP_INDEX] - 
				SimToolkit.getAgentsStats(Simulation.getAgentList(), SimToolkit.RP_INDEX, Statistics.MEAN, 0));
		distanceBetweenActualAndPredicted[MEAN_SB_INDEX] = Math.abs(actualRel[MEAN_SB_INDEX] - 
				SimToolkit.getAgentsStats(Simulation.getAgentList(), SimToolkit.SB_INDEX, Statistics.MEAN, 0));
		distanceBetweenActualAndPredicted[MEAN_BIG_INDEX] = Math.abs(actualRel[MEAN_BIG_INDEX] - 
				SimToolkit.getAgentsStats(Simulation.getAgentList(), SimToolkit.BIG_INDEX, Statistics.MEAN, 0));
		**/
		double actualES = SimToolkit.createSecularInstituion(countryParam, endYearParam);
		double[] actualRel = SimToolkit.getSDStatsForPopulation(countryParam, endYearParam);
		distanceBetweenActualAndPredicted[HDI_VALUE_INDEX] = Math.abs(actualES-getSecularInsitution());
		distanceBetweenActualAndPredicted[MEAN_RP_INDEX] = Math.abs(actualRel[MEAN_RP_INDEX] - 
				SimToolkit.getAgentsStats(Simulation.getAgentList(), SimToolkit.RP_INDEX, Statistics.SD, 0.1856936));
		distanceBetweenActualAndPredicted[MEAN_SB_INDEX] = Math.abs(actualRel[MEAN_SB_INDEX] - 
				SimToolkit.getAgentsStats(Simulation.getAgentList(), SimToolkit.SB_INDEX, Statistics.SD, 0.1210375));
		distanceBetweenActualAndPredicted[MEAN_BIG_INDEX] = Math.abs(actualRel[MEAN_BIG_INDEX] - 
				SimToolkit.getAgentsStats(Simulation.getAgentList(), SimToolkit.BIG_INDEX, Statistics.SD, 0.3637565));
				
		return distanceBetweenActualAndPredicted;
	}
	public static ArrayList<Agent> getAgentList() {
		return agentList;
	}

	public static void setAgentList(ArrayList<Agent> pAgentList) {
		agentList = pAgentList;
	}

	public static double getSecularInsitution() {
		return secularInsitution;
	}

	public static void setSecularInsitution(double pSecularInsitution) {
		secularInsitution = pSecularInsitution;
	}

	public static double getReligiousInstitution() {
		return religiousInstitution;
	}

	public static void setReligiousInstitution(double pReligiousInstitution) {
		religiousInstitution = pReligiousInstitution;
	}

	public static double getRELIGIOUS_PRACTICE_THRESHOLD() {
		return RELIGIOUS_PRACTICE_THRESHOLD;
	}

	public static void setRELIGIOUS_PRACTICE_THRESHOLD(double pRELIGIOUS_PRACTICE_THRESHOLD) {
		RELIGIOUS_PRACTICE_THRESHOLD = pRELIGIOUS_PRACTICE_THRESHOLD;
	}

	public static SocialNetwork getSocialNetwork() {
		return socialNetwork;
	}

	public static void setSocialNetwork(SocialNetwork socialNetwork) {
		Simulation.socialNetwork = socialNetwork;
	}

	public static double getDOUGLAS_COBB_BETA() {
		return DOUGLAS_COBB_BETA;
	}

	public static void setDOUGLAS_COBB_BETA(double dOUGLAS_COBB_BETA) {
		DOUGLAS_COBB_BETA = dOUGLAS_COBB_BETA;
	}

	public static double getEDUCATION_HOMOPHILY() {
		return EDUCATION_HOMOPHILY;
	}

	public static void setEDUCATION_HOMOPHILY(double eDUCATION_HOMOPHILY) {
		EDUCATION_HOMOPHILY = eDUCATION_HOMOPHILY;
	}



}
