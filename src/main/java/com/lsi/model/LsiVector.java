package com.lsi.model;

import java.util.Arrays;

public record LsiVector(
        String ownerCode,
        double[] components
) {
    public LsiVector {
        if (ownerCode == null || ownerCode.isBlank()) {
            throw new IllegalArgumentException("ownerCode cannot be blank");
        }

        if (components == null) {
            throw new IllegalArgumentException("components cannot be null");
        }

        components = Arrays.copyOf(components, components.length);
    }

    @Override
    public double[] components() {
        return Arrays.copyOf(components, components.length);
    }
}
