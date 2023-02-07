package Simulation;

import Simulation.entities.Intervention;
import Visualisation.District;
import Visualisation.Patrol;

import java.util.ArrayList;
import java.util.List;

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
    private int numberOfSWATSquads = 0;
    private int numberOfInterventions = 0;
    private int numberOfFirings = 0;
    private int numberOfSolvedInterventions = 0;
    private int numberOfSolvedFirings = 0;
    private int usedSwat = 0;
    private int numberOfNeutralizedPatrols = 0;
    private int durationOfInterventions = 0;
    private int durationMoreThanHour = 0;
    private int durationLessThanHour = 0;
    private int durationOfFirings = 0;
    private int coveredDistanceByPatrol = 0;
    private int coveredDistanceBySWAT = 0;
    private int elapsedTimeBySWAT = 0;
    private int sumonedPatrols = 0;
    private int coveredDistanceByAmbulance = 0;
    private List<String> interventionsDistricts = new ArrayList<>();
    private List<String> patrolsSolvingFiringLevel = new ArrayList<>();
    private List<String> firingDistricts = new ArrayList<>();
    private List<String> neutralizedPatrolDistrict = new ArrayList<>();
    private List<String> neutralizedPatrolSafetyLevel = new ArrayList<>();
    private List<String> swatStates = new ArrayList<>();

    public void reset() {
        this.numberOfPatrols = 0;
        this.numberOfSWATSquads = 0;
        this.numberOfInterventions = 0;
        this.numberOfFirings = 0;
        this.numberOfSolvedInterventions = 0;
        this.numberOfSolvedFirings = 0;
        this.usedSwat = 0;
        this.numberOfNeutralizedPatrols = 0;
        this.durationOfInterventions = 0;
        this.durationOfFirings = 0;
        this.coveredDistanceByPatrol = 0;
        this.coveredDistanceBySWAT = 0;
        this.elapsedTimeBySWAT = 0;
        this.sumonedPatrols = 0;
        this.coveredDistanceByAmbulance = 0;
        this.durationMoreThanHour = 0;
        this.durationLessThanHour = 0;
    }

    public void increaseNumberOfPatrols() {
        this.numberOfPatrols++;
    }

    public void increaseNumberOfSWATSquads() {
        this.numberOfSWATSquads++;
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

    public void increaseDurationOfInterventions(int duration) {
        this.durationOfInterventions += duration;
    }

    public void increaseDurationMoreThanHour() {
        this.durationMoreThanHour++;
    }

    public void increaseDurationLessThanHour() {
        this.durationLessThanHour++;
    }

    public void increaseDurationOfFirings(int duration) {
        this.durationOfFirings += duration;
    }

    public void increaseCoveredDistanceByPatrol(double distance) {
        this.coveredDistanceByPatrol += distance;
    }

    public int getCoveredDistanceByPatrol() {
        return coveredDistanceByPatrol;
    }

    public void increaseCoveredDistanceBySWAT(double distance) {
        this.coveredDistanceBySWAT += distance;
    }

    public int getCoveredDistanceBySWAT() {
        return coveredDistanceBySWAT;
    }

    public void increaseUsedSWAT() {
        this.usedSwat++;
    }

    public int getUsedSWAT() {
        return usedSwat;
    }

    public void increaseElapsedTimeBySWAT(double distance) {
        this.elapsedTimeBySWAT += distance;
    }

    public int getElapsedTimeBySWAT() {
        return elapsedTimeBySWAT;
    }

    public void increaseCoveredDistanceByAmbulance(double distance) {
        this.coveredDistanceByAmbulance += distance;
    }

    public void increaseSumonedPatrols() {
        this.sumonedPatrols++;
    }

    public int getSumonedPatrols() {
        return sumonedPatrols;
    }

    public int getCoveredDistanceByAmbulance() {
        return coveredDistanceByAmbulance;
    }

    public int getNumberOfPatrols() {
        return numberOfPatrols;
    }

    public int getNumberOfSWATSquads() {
        return numberOfSWATSquads;
    }

    public int getNumberOfInterventions() {
        return numberOfInterventions;
    }

    public int getAmountOfDurationMoreThanHour() {
        return durationMoreThanHour;
    }

    public int getAmountOfDurationLessThanHour() {
        return durationLessThanHour;
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

    public int getDurationOfInterventions() {
        return durationOfInterventions;
    }

    public int getDurationOfFirings() {
        return durationOfFirings;
    }

    public List<String> getInterventionsDistricts() {
        return interventionsDistricts;
    }

    public void addInterventionsDistricts(String district) {
        interventionsDistricts.add(district);
    }

    public List<String> getPatrolsSolvingFiringLevel() {
        return patrolsSolvingFiringLevel;
    }

    public void addPatrolsSolvingFiringLevel(String safetyLevel) {
        patrolsSolvingFiringLevel.add(safetyLevel);
    }

    public List<String> getFiringDistricts() {
        return firingDistricts;
    }

    public void addFiringDistricts(String district) {
        firingDistricts.add(district);
    }

    public List<String> getNeutralizedPatrolDistrict() {
        return neutralizedPatrolDistrict;
    }

    public void addNeutralizedPatrolDistrict(String district) {
        neutralizedPatrolDistrict.add(district);
    }

    public List<String> getNeutralizedPatrolSafetyLevel() {
        return neutralizedPatrolSafetyLevel;
    }

    public void addNeutralizedPatrolSafetyLevel(String district) {
        neutralizedPatrolSafetyLevel.add(district);
    }

    public List<String> getSwatStates() {
        return swatStates;
    }

    public void addSwatState(String state) {
        swatStates.add(state);
    }
}
