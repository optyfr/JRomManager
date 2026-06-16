package jrm.aui.basic;

/**
 * ResultColUpdater is an interface that defines methods for updating the result column in a table. It provides methods to update
 * the result text for a specific row and to clear all results.
 */
public interface ResultColUpdater {
    /**
     * Update the result column text to <code>result</code> at row <code>row</code>
     * 
     * @param row the row index to update
     * @param result the text to set
     */
    void updateResult(int row, String result);

    /**
     * Clear all results
     */
    void clearResults();

}
