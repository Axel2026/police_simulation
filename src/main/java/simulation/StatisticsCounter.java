package simulation;

public class StatisticsCounter {

    private StatisticsCounter() {

    }

    private static StatisticsCounter instance;

    public static StatisticsCounter getInstance() {
        // Result variable here may seem pointless, but it's needed for DCL (Double-checked locking).
        var result = instance;
        if (instance != null) {
            return result;
        }
        synchronized (StatisticsCounter.class) {
            if (instance == null) {
                instance = new StatisticsCounter();
            }
            return instance;
        }
    }

    private int numberOfPatrols = 0;
    private int numberOfInterventions = 0;
    private int numberOfFirings = 0;
    private int numberOfSolvedInterventions = 0;
    private int numberOfSolvedFirings = 0;
    private int numberOfNeutralizedPatrols = 0;

    public void reset() {
        this.numberOfPatrols = 0;
        this.numberOfInterventions = 0;
        this.numberOfFirings = 0;
        this.numberOfSolvedInterventions = 0;
        this.numberOfSolvedFirings = 0;
        this.numberOfNeutralizedPatrols = 0;
    }

    public void increaseNumberOfPatrols() {
        this.numberOfPatrols++;
    }

    public void increaseNumberOfInterventions() {
        this.numberOfInterventions++;
    }

    public void increaseNumberOfFirings() {
        this.numberOfFirings++;
    }

    public void increaseNumberOfSolvedInterventions() {
        this.numberOfSolvedInterventions++;
    }

    public void increaseNumberOfSolvedFirings() {
        this.numberOfSolvedFirings++;
    }

    public void increaseNumberOfNeutralizedPatrols() {
        this.numberOfNeutralizedPatrols++;
    }

    public int getNumberOfPatrols() {
        return numberOfPatrols;
    }

    public int getNumberOfInterventions() {
        return numberOfInterventions;
    }

    public int getNumberOfFirings() {
        return numberOfFirings;
    }

    public int getNumberOfSolvedInterventions() {
        return numberOfSolvedInterventions;
    }

    public int getNumberOfSolvedFirings() {
        return numberOfSolvedFirings;
    }

    public int getNumberOfNeutralizedPatrols() {
        return numberOfNeutralizedPatrols;
    }
}
