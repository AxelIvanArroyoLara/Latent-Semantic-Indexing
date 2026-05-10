package com.equipo.documentbase.indexing;

import com.equipo.documentbase.domain.SemanticDocument;
import java.util.*;

public class FixtureSemanticDocuments {
    public static List<SemanticDocument> build() {
        List<SemanticDocument> docs = new ArrayList<>();

        docs.add(new SemanticDocument("D1", Arrays.asList(
            "estres", "academico", "estudiante", "universidad", "presion", "trabajo", "clases", "examenes"
        ), Map.of()));

        docs.add(new SemanticDocument("D2", Arrays.asList(
            "ansiedad", "desempeno", "escolar", "estudiante", "universidad", "nervios", "preocupacion", "rendimiento"
        ), Map.of()));

        docs.add(new SemanticDocument("D3", Arrays.asList(
            "burnout", "estudiante", "universidad", "agotamiento", "estres", "falta", "motivacion", "cansancio"
        ), Map.of()));

        docs.add(new SemanticDocument("D4", Arrays.asList(
            "depresion", "adultos", "jovenes", "universidad", "tristeza", "desesperanza", "aislamiento", "soledad"
        ), Map.of()));

        docs.add(new SemanticDocument("D5", Arrays.asList(
            "sueno", "calidad", "salud", "mental", "estudiante", "universidad", "insomnio", "descanso"
        ), Map.of()));

        docs.add(new SemanticDocument("D6", Arrays.asList(
            "ejercicio", "regulacion", "emocional", "estudiante", "universidad", "actividad", "fisica", "bienestar"
        ), Map.of()));

        docs.add(new SemanticDocument("D7", Arrays.asList(
            "alimentacion", "concentracion", "estudiante", "universidad", "nutricion", "dieta", "energia", "rendimiento"
        ), Map.of()));

        docs.add(new SemanticDocument("D8", Arrays.asList(
            "servicios", "apoyo", "psicologico", "universidad", "estudiante", "consejeria", "terapia", "salud", "mental"
        ), Map.of()));

        docs.add(new SemanticDocument("D9", Arrays.asList(
            "redes", "sociales", "excesivo", "ansiedad", "estudiante", "universidad", "tecnologia", "comparacion"
        ), Map.of()));

        docs.add(new SemanticDocument("D10", Arrays.asList(
            "tecnicas", "respiracion", "autorregulacion", "emocional", "estudiante", "universidad", "relajacion", "mindfulness"
        ), Map.of()));

        // Añadimos un par más basados en los archivos subidos (COVID, bienestar europeo, etc.)
        docs.add(new SemanticDocument("D11", Arrays.asList(
            "covid", "pandemia", "salud", "mental", "estudiante", "universidad", "confinamiento", "ansiedad", "depresion"
        ), Map.of()));

        docs.add(new SemanticDocument("D12", Arrays.asList(
            "bienestar", "mental", "estudiante", "europa", "servicios", "apoyo", "psicologico", "consejeria"
        ), Map.of()));

        return docs;
    }
}