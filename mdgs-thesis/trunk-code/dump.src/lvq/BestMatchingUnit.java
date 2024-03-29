/*
 * Encog(tm) Core v3.0 - Java Version
 * http://www.heatonresearch.com/encog/
 * http://code.google.com/p/encog-java/
 
 * Copyright 2008-2011 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *   
 * For more information on Heaton Research copyrights, licenses 
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */
package id.mdgs.neural.lvq;

import org.encog.engine.util.BoundMath;
import org.encog.mathutil.matrices.Matrix;
import org.encog.neural.data.NeuralData;

/**
 * The "Best Matching Unit" or BMU is a very important concept in the training
 * for a Lvq. The BMU is the output neuron that has weight connections to the
 * input neurons that most closely match the current input vector. This neuron
 * (and its "neighborhood") are the neurons that will receive training.
 * 
 * This class also tracks the worst distance (of all BMU's). This gives some
 * indication of how well the network is trained, and thus becomes the "error"
 * of the entire network.
 * 
 * @author jeff
 * @author I Made Agus Setiawan , 
 * 
 * Modified to conform Lvq behavior
 * 
 */
public class BestMatchingUnit {

	class BMUInfo {
		public int[] index;
		public double[] distance;
		
		public BMUInfo(int k){
			index = new int[k];
			distance = new double[k];
			reset();
		}
		
		public void reset(){
			for(int i=0; i<index.length;i++){
				index[i] = -1;
				distance[i] = Double.MAX_VALUE;
			}
		}
		
		public int size(){
			return index.length;
		}
	}
	/**
	 * The owner of this class.
	 */
	private final Lvq1 network;

	/**
	 * What is the worst BMU distance so far, this becomes the error for the
	 * entire LVQ.
	 */
	private double worstDistance;

	/**
	 * Construct a BestMatchingUnit class.  The training class must be provided.
	 * @param training The parent class.
	 */
	public BestMatchingUnit(final Lvq1 network) {
		this.network = network;
	}

	/**
	 * Calculate the best matching unit (BMU). This is the output neuron that
	 * has the lowest Euclidean distance to the input vector.
	 * 
	 * @param matrix
	 *            The matrix to calculate for.
	 * @param input
	 *            The input vector.
	 * @return The output neuron number that is the BMU.
	 */
	public int calculateBMU(final NeuralData input) {
		int result = 0;
		
		// Track the lowest distance so far.
		double lowestDistance = Double.MAX_VALUE;

		for (int i = 0; i < this.network.getOutputCount(); i++) {
			final double distance = calculateEuclideanDistance(
					this.network.getWeights(), input, i);

			// Track the lowest distance, this is the BMU.
			if (distance < lowestDistance) {
				lowestDistance = distance;
				result = i;
			}
		}

		// Track the worst distance, this is the error for the entire network.
		if (lowestDistance > this.worstDistance) {
			this.worstDistance = lowestDistance;
		}

		return result;
	}

	
	public int[] calculateBMU(final NeuralData input, final BMUInfo win) {
		win.reset();
		
		for (int i = 0; i < this.network.getOutputCount(); i++) {
			final double diff = calculateEuclideanDistance(
					this.network.getWeights(), input, i);

			int j;
			for(j=0; j<win.size() && diff > win.distance[j];j++);
			
			if(j < win.size()){
				for(int x = win.size() - 1;x > j;x++){
					win.distance[x] = win.distance[x-1];
					win.index[x]	= win.index[x-1];
				}
				
				win.distance[j] = diff;
				win.index[j] 	= i;
			}
		}

		// Track the worst distance, this is the error for the entire network.
		if (win.distance[0] > this.worstDistance) {
			this.worstDistance = win.distance[0];
		}

		return win.index;
	}
	
	/**
	 * Calculate the Euclidean distance for the specified output neuron and the
	 * input vector.  This is the square root of the squares of the differences
	 * between the weight and input vectors.
	 * 
	 * @param matrix
	 *            The matrix to get the weights from.
	 * @param input
	 *            The input vector.
	 * @param outputNeuron
	 *            The neuron we are calculating the distance for.
	 * @return The Euclidean distance.
	 */
	public double calculateEuclideanDistance(final Matrix matrix,
			final NeuralData input, final int outputNeuron) {
		double result = 0;
		
		// Loop over all input data.
		for (int i = 0; i < input.size(); i++) {
			final double diff = input.getData(i)
					- matrix.get(i, outputNeuron);
			result += diff * diff;
		}
		return BoundMath.sqrt(result);
	}

	/**
	 * @return What is the worst BMU distance so far, this becomes the error 
	 * for the entire LVQ.
	 */
	public double getWorstDistance() {
		return this.worstDistance;
	}

	/**
	 * Reset the "worst distance" back to a minimum value.  This should be
	 * called for each training iteration.
	 */
	public void reset() {
		this.worstDistance = Double.MIN_VALUE;
	}
}
