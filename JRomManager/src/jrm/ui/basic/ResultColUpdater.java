package jrm.ui.basic;

public interface ResultColUpdater
{
	/**
	 * Update the result column text to <code>result</code> at row <code>row</code>
	 * @param row the row index to update
	 * @param result the text to set
	 */
	void updateResult(int row, String result);

}
