package com.equipo.documentbase.lsi;

import com.equipo.documentbase.domain.FrequencyMatrix;
import com.equipo.documentbase.domain.LatentSpaceModel;

import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleSVD;

import java.util.logging.Logger;

public class LsiReducer {

    private static final Logger LOG =
            Logger.getLogger(LsiReducer.class.getName());

    /**
     * Reduce la matriz término-documento usando LSI/SVD.
     *
     * Convención utilizada:
     *
     * documentVectors[documentIndex][latentDimension]
     * termVectors[termIndex][latentDimension]
     *
     * A = U * S * V^T
     *
     * termVectors = U_k * S_k
     * documentVectors = V_k * S_k
     */
    public LatentSpaceModel reduce(
            FrequencyMatrix freqMatrix,
            int requestedK
    ) {

        validateInputs(freqMatrix, requestedK);

        // =========================================
        // 1. Construir matriz A
        // términos x documentos
        // =========================================

        double[][] raw = freqMatrix.values();

        SimpleMatrix A = new SimpleMatrix(raw);

        int termCount = A.numRows();
        int docCount = A.numCols();

        LOG.info(() ->
                "Running SVD for matrix "
                        + termCount
                        + "x"
                        + docCount
        );

        // =========================================
        // 2. SVD
        // A = U * S * V^T
        // =========================================

        SimpleSVD<SimpleMatrix> svd = A.svd();

        SimpleMatrix U = svd.getU();
        SimpleMatrix W = svd.getW();
        SimpleMatrix V = svd.getV();

        // =========================================
        // 3. Determinar k real
        // =========================================

        int rank = svd.rank();
        int actualK = Math.min(requestedK, rank);

        if (actualK < requestedK) {
            LOG.warning(() ->
                    "Requested k="
                            + requestedK
                            + " reduced to "
                            + actualK
                            + " due to matrix rank."
            );
        }

        // =========================================
        // 4. Truncar matrices
        // =========================================

        SimpleMatrix Uk = U.extractMatrix(
                0,
                U.numRows(),
                0,
                actualK
        );

        SimpleMatrix Sk = W.extractMatrix(
                0,
                actualK,
                0,
                actualK
        );

        SimpleMatrix Vk = V.extractMatrix(
                0,
                V.numRows(),
                0,
                actualK
        );

        // =========================================
        // 5. Espacio latente
        // =========================================

        // terms x k
        SimpleMatrix termVectors =
                Uk.mult(Sk);

        // docs x k
        SimpleMatrix documentVectors =
                Vk.mult(Sk);

        // =========================================
        // 6. Singular values
        // =========================================

        double[] singularValues =
                new double[actualK];

        for (int i = 0; i < actualK; i++) {

            singularValues[i] =
                    Sk.get(i, i);
        }

        // =========================================
        // 7. Crear modelo
        // =========================================

        return new LatentSpaceModel(
                actualK,
                toArray(documentVectors),
                toArray(termVectors),
                singularValues,
                freqMatrix.documentCodes(),
                freqMatrix.terms()
        );
    }

    // =====================================================
    // Validaciones
    // =====================================================

    private void validateInputs(
            FrequencyMatrix freqMatrix,
            int k
    ) {

        if (freqMatrix == null) {
            throw new IllegalArgumentException(
                    "FrequencyMatrix cannot be null"
            );
        }

        if (k <= 0) {
            throw new IllegalArgumentException(
                    "k must be positive. Got: " + k
            );
        }

        if (freqMatrix.values() == null) {
            throw new IllegalArgumentException(
                    "Matrix values cannot be null"
            );
        }

        if (freqMatrix.values().length == 0) {
            throw new IllegalArgumentException(
                    "Matrix cannot be empty"
            );
        }
    }

    // =====================================================
    // Utility
    // =====================================================

    private double[][] toArray(SimpleMatrix matrix) {

        double[][] arr =
                new double[matrix.numRows()]
                        [matrix.numCols()];

        for (int i = 0; i < matrix.numRows(); i++) {

            for (int j = 0; j < matrix.numCols(); j++) {

                arr[i][j] = matrix.get(i, j);
            }
        }

        return arr;
    }
}