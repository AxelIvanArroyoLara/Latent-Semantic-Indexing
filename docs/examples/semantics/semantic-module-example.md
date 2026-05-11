# Semantic Module Example

This document describes the expected behavior of the semantic module implemented for Dev 4.

The module receives normalized tokens from the preprocessing stage and produces semantic terms ready to be consumed by the indexing module.

## Purpose

The semantic module adds a controlled semantic layer before indexing. It does not use external NLP models. Instead, it relies on:

- a controlled synonym dictionary;
- simple context-based polysemy rules;
- deterministic transformations;
- traceable input and output examples.

This keeps the module simple, testable, and easy to integrate with the rest of the LSI pipeline.

## Corpus domain

The current document collection is in English and focuses on college student mental health and wellbeing. Because of this, the semantic resources include terms related to:

- stress;
- anxiety;
- depression;
- burnout;
- mental health;
- wellbeing;
- counseling;
- campus support;
- academic performance;
- sleep quality;
- social media;
- help seeking.

## Synonym normalization

The synonym dictionary maps equivalent or closely related terms to a canonical term.

Examples:

worry -> anxiety  
nervousness -> anxiety  
pressure -> stress  
well-being -> wellbeing  
counselling -> counseling  
therapy -> counseling  
exercise -> physical_activity  
focus -> concentration  

This helps the system avoid treating related terms as completely different words.

## Polysemy resolution

Some words can have different meanings depending on their context. The module handles this using simple context rules.

Examples:

support + university -> institutional_support  
support + emotional -> emotional_support  
health + mental -> mental_health  
stress + academic -> academic_stress  
media + social -> social_media  
sleep + quality -> sleep_quality  

The goal is not to solve every possible meaning. The goal is to show a controlled and explainable semantic treatment that satisfies the project requirement.

## Example input

student worry academic pressure emotional support university support mental health campus wellbeing counseling services sleep quality social media help seeking

## Expected output

student anxiety academic_pressure emotional_support institutional_support mental_health campus_wellbeing counseling_services sleep_quality social_media help_seeking

## Integration contract

Input expected from preprocessing:

List<String> tokens

Output expected for indexing:

List<String> canonicalTerms

The indexing module should consume the final semantic terms as the vocabulary source for frequency counting and FrecT construction.

## Notes

This module does not read documents directly. It does not calculate frequencies. It does not execute LSI or SVD. Its responsibility is only semantic normalization before indexing.
