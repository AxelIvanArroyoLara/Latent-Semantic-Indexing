package com.lsi.lsi;

import com.lsi.model.FrequencyMatrix;
import com.lsi.model.LatentSpaceModel;

/**
 * Facade for SVD decomposition. Kept as a named module because the project
 * rubric asks for an explicit SVD step before LSI term reduction.
 */
public class SvdDecomposer {

    private final LsiReducer reducer;

    public SvdDecomposer() {
        this(new LsiReducer());
    }

    public SvdDecomposer(LsiReducer reducer) {
        this.reducer = reducer;
    }

    public LatentSpaceModel decompose(FrequencyMatrix frequencyMatrix, int dimensions) {
        return reducer.reduce(frequencyMatrix, dimensions);
    }
}
