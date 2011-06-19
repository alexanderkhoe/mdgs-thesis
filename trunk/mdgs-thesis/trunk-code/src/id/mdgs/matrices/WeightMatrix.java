package id.mdgs.matrices;
import id.mdgs.neural.data.FuzzyNode;
import id.mdgs.neural.data.Node;

import java.io.Serializable;

import org.encog.EncogError;
import org.encog.mathutil.matrices.MatrixError;


/**
 * @author madeagus
 * @version 1.0
 * @created 27-Apr-2011 3:29:55 PM
 */
public class WeightMatrix implements Cloneable, Serializable {

	private static final long serialVersionUID = -4782881703937768188L;
		
	private Node[][] matrix;

//	/**
//	 * Create a matrix from an array of doubles n x 3.
//	 * Assume rows is number of input, 
//	 * @param sourceMatrix    An array of doubles.
//	 */
//	public WeightMatrix(final int rows, final int cols, final double[][] sourceMatrix){
//		if(sourceMatrix.length != rows*cols){
//			throw new MatrixError("source Matrix dimension not match.");
//		}
//		
//		this.matrix = new FuzzyNode[rows][cols];
//		FuzzyNode fn = new FuzzyNode();
//		for (int r = 0; r < getRows(); r++) {
//			for (int c = 0; c < getCols(); c++) {
//				fn.min 	= sourceMatrix[r*getCols()+c][0];
//				fn.mean = sourceMatrix[r*getCols()+c][1];
//				fn.max 	= sourceMatrix[r*getCols()+c][2];
//				set(r, c, fn.clone());
//			}
//		}		
//	}

	/**
	 * Create a blank array with the specified number of rows and columns.
	 * 
	 * @param rows    How many rows in the matrix.
	 * @param cols    How many columns in the matrix.
	 */
	public WeightMatrix(final int rows, final int cols){
		this.matrix = new FuzzyNode[rows][cols];
		set(new FuzzyNode());		
	}

	public WeightMatrix(final int rows, final int cols, final double[][] w){
		if (rows*cols != w.length){
			throw new MatrixError(
					String.format("WeightMatrix: rows: %d and cols:%d " + 
							"did not macth with input array length: %d",
							rows, cols, w.length));
		}
		
		this.matrix = new FuzzyNode[rows][cols];
		for (int c = 0; c < cols; c++) {
			for (int r = 0; r < rows; r++) {
				int idx = (c * rows) + r;
				set(r, c, new FuzzyNode(w[idx]));
			}
		}		
	}	
	
	public WeightMatrix(final Node[][] fns){
		this.matrix = new FuzzyNode[fns.length][fns[0].length];
		for (int r = 0; r < getRows(); r++) {
			for (int c = 0; c < getCols(); c++) {
				set(r, c, fns[r][c].clone());
			}
		}		
	}

	public WeightMatrix(final WeightMatrix wm){
		this.matrix = new FuzzyNode[wm.getRows()][wm.getCols()];
		for (int r = 0; r < getRows(); r++) {
			for (int c = 0; c < getCols(); c++) {
				set(r, c, wm.get(r, c).clone());
			}
		}		
	}	
	

	/**
	 * Set all rows and columns to zero.
	 */
	public void clear(){
		Node zero = new FuzzyNode(0, 0, 0);
		for (int r = 0; r < getRows(); r++) {
			for (int c = 0; c < getCols(); c++) {
				get(r,c).copy(zero);
			}
		}
	}

	/**
	 * Create a copy of the matrix.
	 * @return A clone of the matrix.
	 */
	@Override
	public WeightMatrix clone(){
		return new WeightMatrix(this);
	}

	/**
	 * Read the specified cell in the matrix.
	 * @return The value at the specified row and column.
	 * 
	 * @param row    The row to read.
	 * @param col    The column to read.
	 */
	public Node get(final int row, final int col){
		validate(row, col);
		return this.matrix[row][col];
	}

	/**
	 * Get the columns in the matrix.
	 * @return The number of columns in the matrix.
	 */
	public int getCols(){
		return this.matrix[0].length;
	}

	/**
	 * Get the number of rows in the matrix.
	 * @return The number of rows in the matrix.
	 */
	public int getRows(){
		return this.matrix.length;
	}

	/**
	 * 
	 * @param min
	 * @param max
	 */
	public void randomize(double min, double max){
		for (int row = 0; row < getRows(); row++) {
			for (int col = 0; col < getCols(); col++) {
				get(row,col).randomize(min, max);
			}
		}		
	}

	/**
	 * Set every value in the matrix to the specified value.
	 * 
	 * @param fn    The value to set the matrix to.
	 */
	public void set(final FuzzyNode fn){
		for (int r = 0; r < getRows(); r++) {
			for (int c = 0; c < getCols(); c++) {
				//Here we use clone() to make every cell has their own 
				//specific object/reference
				this.matrix[r][c] = fn.clone();
			}
		}
	}

	/**
	 * Set an individual cell in the matrix to the specified value.
	 * 
	 * @param row    The row to set.
	 * @param col    The column to set.
	 * @param value    The value to be set.
	 */
	public void set(final int row, final int col, final Node fn){
		validate(row, col);
		this.matrix[row][col] = fn;
	}

	/**
	 * Set this matrix's values to that of another matrix.
	 * 
	 * @param matrix    The other matrix.
	 */
	public void set(final WeightMatrix wm){
		for (int r = 0; r < getRows(); r++) {
			for (int c = 0; c < getCols(); c++) {
				this.matrix[r][c].copy(wm.get(r, c));
			}
		}
	}

	/**
	 * Get the size of the array. This is the number of elements it would take to
	 * store the matrix as a packed array.
	 * @return The size of the matrix.
	 */
	public int size(){
		return this.matrix[0].length * this.matrix.length;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString(){
		final StringBuilder result = new StringBuilder();
		result.append("[WeightMatrix: rows=");
		result.append(getRows());
		result.append(",cols=");
		result.append(getCols());
		result.append("]");
		result.append("\n");
		
		for (int c = 0; c < getCols(); c++) {
			for (int r = 0; r < getRows(); r++) {
				result.append(this.matrix[r][c].toString());
				result.append("\n");
			}
			result.append("\n");
		}
		return result.toString();
	}

	/**
	 * Validate that the specified row and column are within the required ranges.
	 * Otherwise throw a MatrixError exception.
	 * 
	 * @param row    The row to check.
	 * @param col    The column to check.
	 */
	private void validate(final int row, final int col){
		if ((row >= getRows()) || (row < 0)) {
			final String str = "The row: " + row + " is out of range: "
					+ getRows();
			throw new MatrixError(str);
		}

		if ((col >= getCols()) || (col < 0)) {
			final String str = "The col: " + col + " is out of range: "
					+ getCols();
			throw new MatrixError(str);
		}
	}

}