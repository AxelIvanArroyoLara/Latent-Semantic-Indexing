package com.lsi.persistence;

import com.lsi.model.LatentSpaceModel;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Logger;

public class FileLatentModelRepository
        implements LatentModelRepository {

    private static final Logger LOG =
            Logger.getLogger(
                    FileLatentModelRepository.class.getName()
            );

    private final String filePath;

    public FileLatentModelRepository(
            String filePath
    ) {

        if (filePath == null || filePath.isBlank()) {

            throw new IllegalArgumentException(
                    "File path cannot be null or blank"
            );
        }

        this.filePath = filePath;
    }

    @Override
    public void save(
            LatentSpaceModel model
    ) {

        validateModel(model);

        Path path = Paths.get(filePath);

        try {

            // =========================================
            // Crear directorios si no existen
            // =========================================

            Path parent = path.getParent();

            if (parent != null) {

                Files.createDirectories(parent);
            }

            // =========================================
            // Serializar modelo
            // =========================================

            try (ObjectOutputStream oos =
                         new ObjectOutputStream(
                                 new BufferedOutputStream(
                                         new FileOutputStream(
                                                 filePath
                                         )
                                 )
                         )) {

                oos.writeObject(model);

                oos.flush();
            }

            LOG.info(() ->
                    "Latent model saved to: "
                            + filePath
            );

        } catch (IOException e) {

            LOG.severe(() ->
                    "Failed to save latent model: "
                            + e.getMessage()
            );

            throw new RuntimeException(
                    "Failed to save latent model",
                    e
            );
        }
    }

    @Override
    public Optional<LatentSpaceModel> loadLatest() {

        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {

            LOG.warning(() ->
                    "Model file does not exist: "
                            + filePath
            );

            return Optional.empty();
        }

        try (ObjectInputStream ois =
                     new ObjectInputStream(
                             new BufferedInputStream(
                                     new FileInputStream(
                                             filePath
                                     )
                             )
                     )) {

            Object obj = ois.readObject();

            if (!(obj instanceof LatentSpaceModel model)) {

                throw new RuntimeException(
                        "Invalid serialized object type"
                );
            }

            LOG.info(() ->
                    "Latent model loaded from: "
                            + filePath
            );

            return Optional.of(model);

        } catch (IOException e) {

            LOG.severe(() ->
                    "I/O error while loading model: "
                            + e.getMessage()
            );

            return Optional.empty();

        } catch (ClassNotFoundException e) {

            LOG.severe(() ->
                    "Serialized class not found: "
                            + e.getMessage()
            );
            return Optional.empty();
        }
    }

    // =====================================================
    // Validation
    // =====================================================

    private void validateModel(
            LatentSpaceModel model
    ) {

        if (model == null) {

            throw new IllegalArgumentException(
                    "Model cannot be null"
            );
        }

        if (model.k() <= 0) {

            throw new IllegalArgumentException(
                    "Invalid latent dimension k"
            );
        }

        if (model.documentVectors() == null) {

            throw new IllegalArgumentException(
                    "Document vectors cannot be null"
            );
        }

        if (model.termVectors() == null) {

            throw new IllegalArgumentException(
                    "Term vectors cannot be null"
            );
        }

        if (model.singularValues() == null) {

            throw new IllegalArgumentException(
                    "Singular values cannot be null"
            );
        }
    }
}
