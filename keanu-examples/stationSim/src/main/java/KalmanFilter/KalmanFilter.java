package KalmanFilter;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import Jama.Matrix;

public class KalmanFilter {

    public double[] stateMatrix; // initial state matrix before predict() and update()
    public double[][] uncertaintyCovarMatrix; // initial covar matrix (at time k-1) SHAPE: n*n
    public double[][] stateTransformationFunc; // function to transform state: X'=F*x as matrix SHAPE: n*n
    public double[] inputEffectMatrix; // SHAPE: n*1

    public int N = 3;

    //INDArray stateMatrix = Nd4j.zeros(1, 4);
    //INDArray uncertaintyCovarMatrix = Nd4j.zeros(N, N);


    public static void predict(double[] stateMatrix,
                               double[][] uncertaintyCovarMatrix,
                               double[][] stateTransformFunc,
                               double[][] motionNoiseMatrix,
                               double[] inputEffectMatrix,
                               double[] unknownMatrix) {

        /*
        for (int i=0; i<stateMatrix.length; i++) {
            for (int j=0; j<uncertaintyCovarMatrix.length; j++) {
                stateMatrix[i]
            }
        }
        */
    }


    public static void update() {

    }


    public static void main (String[] args) {

    }
}
