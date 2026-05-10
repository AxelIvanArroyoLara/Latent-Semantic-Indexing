package com.equipo.documentbase.persistence;

import com.equipo.documentbase.domain.LatentSpaceModel;
import java.util.Optional;

public interface LatentModelRepository {
    void save(LatentSpaceModel model);
    Optional<LatentSpaceModel> loadLatest();
}