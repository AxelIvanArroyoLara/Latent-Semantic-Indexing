package com.lsi.indexing;

import com.lsi.config.AppConfig;
import com.lsi.lsi.LsiReducer;
import com.lsi.model.Document;
import com.lsi.model.FrequencyMatrix;
import com.lsi.model.LatentSpaceModel;
import com.lsi.model.SemanticDocument;
import com.lsi.preprocessing.PreprocessingPipeline;
import com.lsi.semantic.SemanticPipeline;
import com.lsi.persistence.DocumentRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Coordinates preprocessing, semantic normalization, frequency matrix creation,
 * and LSI reduction for an in-memory corpus.
 */
public class IndexingService {

    private final PreprocessingPipeline preprocessingPipeline;
    private final SemanticPipeline semanticPipeline;
    private final FrequencyMatrixBuilder frequencyMatrixBuilder;
    private final LsiReducer lsiReducer;
    private final DocumentRepository documentRepository = new DocumentRepository();

    public IndexingService() {
        this(
                new PreprocessingPipeline(),
                new SemanticPipeline(),
                new FrequencyMatrixBuilder(),
                new LsiReducer()
        );
    }

    public IndexingService(
            PreprocessingPipeline preprocessingPipeline,
            SemanticPipeline semanticPipeline,
            FrequencyMatrixBuilder frequencyMatrixBuilder,
            LsiReducer lsiReducer
    ) {
        this.preprocessingPipeline = preprocessingPipeline;
        this.semanticPipeline = semanticPipeline;
        this.frequencyMatrixBuilder = frequencyMatrixBuilder;
        this.lsiReducer = lsiReducer;
    }

    public IndexedCorpus index(List<Document> documents) {
        return index(documents, AppConfig.DEFAULT_LSI_DIMENSIONS, true);
    }

    public IndexedCorpus index(
            List<Document> documents,
            int dimensions,
            boolean useTfIdf
    ) {
        if (documents == null || documents.isEmpty()) {
            throw new IllegalArgumentException("Document list cannot be null or empty");
        }

        List<SemanticDocument> semanticDocuments = new ArrayList<>();
        Map<String, String> titlesByCode = new LinkedHashMap<>();

        for (Document document : documents) {
            List<String> tokens = preprocessingPipeline.process(document.textForProcessing());
            List<String> canonicalTerms = semanticPipeline.process(tokens);

            semanticDocuments.add(new SemanticDocument(
                    document.code(),
                    canonicalTerms,
                    Map.of(
                            "title", document.title(),
                            "source", document.source(),
                            "rawText", document.rawText(),
                            "normalizedText", document.textForProcessing()
                    )
            ));
            titlesByCode.put(document.code(), document.title());
        }

        FrequencyMatrix frequencyMatrix = frequencyMatrixBuilder.build(semanticDocuments, useTfIdf);
        LatentSpaceModel model = lsiReducer.reduce(frequencyMatrix, dimensions);

        return new IndexedCorpus(semanticDocuments, frequencyMatrix, model, titlesByCode);
    }

    public IndexedCorpus indexQueryFixtures() {
        return indexQueryFixtures(AppConfig.QUERY_FIXTURE_DIR, AppConfig.DEFAULT_LSI_DIMENSIONS);
    }

    public IndexedCorpus indexQueryFixtures(int dimensions) {
        return indexQueryFixtures(AppConfig.QUERY_FIXTURE_DIR, dimensions);
    }

    public IndexedCorpus indexFromDatabase(int dimensions) {
        return index(loadDocumentsFromDatabase(), dimensions, true);
    }

    public List<Document> loadDocumentsFromDatabase() {
        List<DocumentRepository.DocumentRow> rows = documentRepository.findAll();
        List<Document> documents = new ArrayList<>();

        for (DocumentRepository.DocumentRow row : rows) {
            String normalizedText = row.normalizedText();

            if (normalizedText == null || normalizedText.isBlank()) {
                normalizedText = row.rawText();
            }

            documents.add(new Document(
                    row.code(),
                    row.title(),
                    row.source(),
                    row.rawText(),
                    normalizedText
            ));
        }

        return documents;
    }

    public IndexedCorpus indexQueryFixtures(Path fixtureDir, int dimensions) {
        return index(loadDocumentsFromQueryFixture(fixtureDir), dimensions, true);
    }

    public IndexedCorpus indexRawPdfDocuments(Path rawDocumentDir, int dimensions) {
        return index(loadDocumentsFromRawPdfs(rawDocumentDir), dimensions, true);
    }

    public IndexedCorpus filterToSelectedTerms(
            IndexedCorpus source,
            Collection<String> selectedTerms,
            int dimensions
    ) {
        if (source == null) {
            throw new IllegalArgumentException("Source corpus cannot be null");
        }

        if (selectedTerms == null || selectedTerms.isEmpty()) {
            throw new IllegalArgumentException("Selected terms cannot be empty");
        }

        Set<String> allowedTerms = selectedTerms.stream()
                .filter(term -> term != null && !term.isBlank())
                .map(term -> term.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<SemanticDocument> filteredDocuments = new ArrayList<>();

        for (SemanticDocument document : source.semanticDocuments()) {
            List<String> filteredTerms = document.canonicalTerms()
                    .stream()
                    .filter(allowedTerms::contains)
                    .toList();

            filteredDocuments.add(new SemanticDocument(
                    document.code(),
                    filteredTerms,
                    document.metadata()
            ));
        }

        FrequencyMatrix frequencyMatrix = frequencyMatrixBuilder.build(filteredDocuments, true);
        LatentSpaceModel model = lsiReducer.reduce(frequencyMatrix, dimensions);

        return new IndexedCorpus(filteredDocuments, frequencyMatrix, model, source.titlesByCode());
    }

    public List<Document> loadDocumentsFromQueryFixture(Path fixtureDir) {
        Path path = fixtureDir.resolve("document_terms.csv");

        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            List<Document> documents = new ArrayList<>();

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();

                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split(",", 3);

                if (parts.length < 3) {
                    continue;
                }

                documents.add(new Document(
                        parts[0].trim(),
                        parts[1].trim(),
                        path.toString(),
                        parts[2].trim(),
                        parts[2].trim()
                ));
            }

            return documents;
        } catch (IOException e) {
            throw new IllegalStateException("Could not load query fixture documents from " + path, e);
        }
    }

    public List<Document> loadDocumentsFromRawPdfs(Path rawDocumentDir) {
        try {
            List<Path> pdfs = Files.list(rawDocumentDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".pdf"))
                    .sorted()
                    .toList();

            if (pdfs.size() < 10) {
                throw new IllegalStateException(
                        "At least 10 PDF documents are required in " + rawDocumentDir
                                + ". Found: " + pdfs.size()
                );
            }

            List<Document> documents = new ArrayList<>();
            int index = 1;

            for (Path pdf : pdfs) {
                ExtractedPdf extractedPdf = extractPdf(pdf);

                if (extractedPdf.text().isBlank()) {
                    continue;
                }

                documents.add(new Document(
                        "D" + index,
                        extractedPdf.title(),
                        pdf.toString(),
                        extractedPdf.text(),
                        extractedPdf.text()
                ));
                index++;
            }

            if (documents.size() < 10) {
                throw new IllegalStateException(
                        "At least 10 PDF documents with extractable text are required in "
                                + rawDocumentDir
                                + ". Extracted: "
                                + documents.size()
                );
            }

            return documents;
        } catch (IOException e) {
            throw new IllegalStateException("Could not load PDF documents from " + rawDocumentDir, e);
        }
    }

    private ExtractedPdf extractPdf(Path pdf) {
        try (PDDocument document = Loader.loadPDF(pdf.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            String metadataTitle = document.getDocumentInformation() == null
                    ? ""
                    : document.getDocumentInformation().getTitle();
            String title = metadataTitle == null || metadataTitle.isBlank()
                    ? titleFromFileName(pdf)
                    : metadataTitle.trim();

            return new ExtractedPdf(title, text == null ? "" : text);
        } catch (IOException e) {
            throw new IllegalStateException("Could not extract text from PDF: " + pdf, e);
        }
    }

    private String titleFromFileName(Path pdf) {
        String fileName = pdf.getFileName().toString();
        int extensionIndex = fileName.toLowerCase(Locale.ROOT).lastIndexOf(".pdf");

        if (extensionIndex > 0) {
            fileName = fileName.substring(0, extensionIndex);
        }

        return fileName
                .replace('-', ' ')
                .replace('_', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    private record ExtractedPdf(String title, String text) {
    }

    public record IndexedCorpus(
            List<SemanticDocument> semanticDocuments,
            FrequencyMatrix frequencyMatrix,
            LatentSpaceModel latentSpaceModel,
            Map<String, String> titlesByCode
    ) {
        public IndexedCorpus {
            semanticDocuments = List.copyOf(semanticDocuments);
            titlesByCode = Map.copyOf(titlesByCode);
        }

        public Map<String, Set<String>> termsByDocumentCode() {
            Map<String, Set<String>> result = new LinkedHashMap<>();

            for (SemanticDocument document : semanticDocuments) {
                result.put(
                        document.code(),
                        new LinkedHashSet<>(document.canonicalTerms())
                );
            }

            return result;
        }
    }
}

