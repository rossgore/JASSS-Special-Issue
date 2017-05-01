import java.util.ArrayList;


public class Agent {

	private boolean helpedByReligiousInst;
	private boolean helpedBySecularInst;

	private int educationLevel;

	private String country;

	private double supernaturalBeliefs;
	private double religiousFormation;
	private double beliefInGod;
	private double religiousPractice;
	private double securityLevel;


	public Agent (double pSB, double pRF, double pBIG, double pRP, int pEducationLevel, String pCountry)
	{
		supernaturalBeliefs = pSB;
		religiousFormation = pRF;
		beliefInGod = pBIG;
		religiousPractice = pRP;
		country = pCountry;
		educationLevel = pEducationLevel;
		securityLevel = Math.random();
	}
	
	public void adjustNetwork(double[] socialNetwork, ArrayList<Agent> agentList, double pAdjustmentLevel)
	{
		for (int i=0; i<socialNetwork.length; i++)
		{
			double randomDraw = Math.random();
			if (socialNetwork[i] != 0.0 && this.getEducationLevel() == agentList.get(i).getEducationLevel())
			{
				if (randomDraw < pAdjustmentLevel)
				{
					socialNetwork[i] = SimToolkit.cobbDouglasFunction(1.0, socialNetwork[i], 0.2);
				}
			}
			if (socialNetwork[i] != 0.0 && this.getEducationLevel() != agentList.get(i).getEducationLevel())
			{
				if (randomDraw < pAdjustmentLevel)
				{
					socialNetwork[i] = SimToolkit.cobbDouglasFunction(0, socialNetwork[i], 0.2);
				}
			}
		}
	}

	public void askForHelp(double pSecularInstitution, double pReligiousPracticeThreshold)
	{

		if ( getReligiousPractice() > pReligiousPracticeThreshold)
		{
			setHelpedByReligiousInst(true);
		}
		else
		{
			setHelpedByReligiousInst(false);
		}
		if (securityLevel < pSecularInstitution)
		{
			setHelpedBySecularInst(true);
		}
		else
		{
			setHelpedBySecularInst(false);
		}
	}
	
	public double weightedAvgOfSN(double[] socialNetwork, ArrayList<Agent> agentList)
	{
		double eductationAvg = 0;
		double totalWeightOfConnections = 0;
		for (int i=0; i<socialNetwork.length; i++)
		{
			totalWeightOfConnections += socialNetwork[i];
		}

		for (int i=0; i<socialNetwork.length; i++)
		{
			if (socialNetwork[i] != 0.0)
			{
				eductationAvg += 1.0*agentList.get(i).getEducationLevel()*(socialNetwork[i]/totalWeightOfConnections);
			}
		}
		return eductationAvg;
	}

	public void beSocial(double[] socialNetwork, ArrayList<Agent> agentList)
	{
		double socialInfluenceOnSecurity = 0;
		double socialInfluenceOnReligiousPractice=0;
		double socialInfluenceOnSupernaturalBeliefs=0;
		double socialInfluenceOnBeliefInGod =0;

		double totalWeightOfConnections = 0;
		for (int i=0; i<socialNetwork.length; i++)
		{
			totalWeightOfConnections += socialNetwork[i];
		}

		for (int i=0; i<socialNetwork.length; i++)
		{
			if (socialNetwork[i] != 0.0)
			{
				socialInfluenceOnSecurity += agentList.get(i).getSecurityLevel()*(socialNetwork[i]/totalWeightOfConnections);
				socialInfluenceOnReligiousPractice += agentList.get(i).getReligiousPractice()*(socialNetwork[i]/totalWeightOfConnections);
			}
		}
		socialInfluenceOnBeliefInGod = SimToolkit.getBeliefInGod(socialInfluenceOnReligiousPractice, getReligiousFormation());
		socialInfluenceOnSupernaturalBeliefs= SimToolkit.getSupernaturalBeliefs(socialInfluenceOnReligiousPractice, getReligiousFormation(),
				this.getBeliefInGod(), this.getEducationLevel());
		
		double newSecurityLevel = SimToolkit.cobbDouglasFunction(socialInfluenceOnSecurity, 
				this.getSecurityLevel(), 
				Simulation.getDOUGLAS_COBB_BETA());

		double newReligiousPracticeLevel = SimToolkit.cobbDouglasFunction(socialInfluenceOnReligiousPractice, 
				this.getReligiousPractice(), 
				Simulation.getDOUGLAS_COBB_BETA());
		
		double newSupernaturalBeliefs = SimToolkit.cobbDouglasFunction(socialInfluenceOnSupernaturalBeliefs,
				this.getSupernaturalBeliefs(), 
				Simulation.getDOUGLAS_COBB_BETA());
		
		double newBeliefInGod = SimToolkit.cobbDouglasFunction(socialInfluenceOnBeliefInGod,
				this.getBeliefInGod(), 
				Simulation.getDOUGLAS_COBB_BETA());
		
		this.setSupernaturalBeliefs(newSupernaturalBeliefs);
		this.setBeliefInGod(newBeliefInGod);
		this.setReligiousPractice(newReligiousPracticeLevel);
		this.setSecurityLevel(newSecurityLevel);
	}

	public int getEducationLevel()
	{
		return educationLevel;
	}

	public void setEducationLevel(int pEL)
	{
		educationLevel = pEL;
	}

	public String getCountry()
	{
		return country;
	}

	public void setSupernaturalBeliefs(double pSB)
	{
		supernaturalBeliefs = pSB;
	}

	public double getSupernaturalBeliefs()
	{
		return supernaturalBeliefs;
	}

	public double getReligiousFormation()
	{
		return religiousFormation;
	}

	public void setReligiousFormation(double pRF)
	{
		religiousFormation = pRF;
	}

	public void setReligiousPractice(double pRP)
	{
		religiousPractice = pRP;
	}

	public double getReligiousPractice()
	{
		return religiousPractice;
	}

	public double getBeliefInGod()
	{
		return beliefInGod;
	}

	public void setBeliefInGod(double pBIG)
	{
		beliefInGod = pBIG;
	}

	public boolean isHelpedByReligiousInst() {
		return helpedByReligiousInst;
	}

	public void setHelpedByReligiousInst(boolean pHelpedByReligiousInst) {
		helpedByReligiousInst = pHelpedByReligiousInst;
	}

	public boolean isHelpedBySecularInst() {
		return helpedBySecularInst;
	}

	public void setHelpedBySecularInst(boolean pHelpedBySecularInst) {
		helpedBySecularInst = pHelpedBySecularInst;
	}

	public double getSecurityLevel() {
		return securityLevel;
	}

	public void setSecurityLevel(double pSecurityLevel) {
		securityLevel = pSecurityLevel;
	}



}
