package com.lsi.persistence;

import com.lsi.model.LatentSpaceModel;
import java.util.Optional;

public interface LatentModelRepository {
    void save(LatentSpaceModel model);
    Optional<LatentSpaceModel> loadLatest();
}
