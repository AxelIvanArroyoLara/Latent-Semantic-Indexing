package com.equipo.documentbase.persistence;

import com.equipo.documentbase.domain.LatentSpaceModel;
import java.util.Optional;
import java.util.logging.Logger;

public class MockLatentModelRepository implements LatentModelRepository {
    private static final Logger LOG = Logger.getLogger(MockLatentModelRepository.class.getName());

    @Override
    public void save(LatentSpaceModel model) {
        LOG.info("Mock save: model with k=" + model.k() + " persisted.");
    }

    @Override
    public Optional<LatentSpaceModel> loadLatest() {
        LOG.info("Mock load: returning empty (no real DB).");
        return Optional.empty();
    }
}