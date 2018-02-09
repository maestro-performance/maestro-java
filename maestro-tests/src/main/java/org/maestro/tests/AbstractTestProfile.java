package org.maestro.tests;

public abstract class AbstractTestProfile implements TestProfile {
    private int testExecutionNumber;

    public int getTestExecutionNumber() {
        return testExecutionNumber;
    }


    public void incrementTestExecutionNumber() {
        testExecutionNumber++;
    }

    @Override
    public String toString() {
        return "AbstractTestProfile{" +
                "testExecutionNumber=" + testExecutionNumber +
                '}';
    }
}
