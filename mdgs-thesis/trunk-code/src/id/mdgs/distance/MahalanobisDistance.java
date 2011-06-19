/**
 * 
 */
package id.mdgs.distance;

import org.apache.commons.math.DimensionMismatchException;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealMatrixImpl;
import org.apache.commons.math.stat.descriptive.moment.VectorialCovariance;

/**
 * @author I Made Agus Setiawan
 *
 */
@SuppressWarnings("deprecation")
public class MahalanobisDistance {
    private final double sums[];
    private       double count;
    private       double means[];

    private final VectorialCovariance covariance;
    private       RealMatrix          covarianceMatrix;

    public MahalanobisDistance(int dimensions)
    {
        sums             = new double[dimensions];
        means            = null;

        covariance       = new VectorialCovariance(dimensions, false);
        covarianceMatrix = null;
    }


    //--------------------------------------------------------------------
    public void add(double values[], double valueCount)
    {
        for (int i = 0; i < valueCount; i++)
        {
            add(values);
        }
    }
    public void add(double values[])
    {
        covarianceMatrix = null;

        try {
            covariance.increment(values);
        } catch (DimensionMismatchException e) {
            throw new Error( e );
        }

        for (int i = 0; i < values.length; i++)
        {
            sums[ i ] += values[ i ];
        }
        count++;
    }


    //--------------------------------------------------------------------
    public double distance(double to[])
    {
        assert count > 0;

        if (covarianceMatrix == null)
        {
            covarianceMatrix = covariance.getResult();
        }

        if (means == null)
        {
            means = new double[ sums.length ];
            for (int i = 0; i < means.length; i++)
            {
                means[ i ] = sums[ i ] / count;
            }
        }

        RealMatrix uT = new RealMatrixImpl(means);
        RealMatrix xT = new RealMatrixImpl(to   );

        RealMatrix xMinusU  = xT.subtract( uT );
        RealMatrix sInverse = covarianceMatrix.inverse();

        RealMatrix distSquared =
                xMinusU.transpose()
                    .multiply( sInverse )
                .multiply( xMinusU );

        return Math.sqrt(distSquared.getEntry(0, 0));
    }
}