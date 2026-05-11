# Semantic module fixtures

This folder contains small input and output examples for the semantic module.

## Input

student worry academic pressure emotional support university support

## Expected output

student anxiety academic_pressure emotional_support institutional_support

## What this proves

- worry is normalized to anxiety.
- academic + pressure is resolved as academic_pressure.
- emotional + support is resolved as emotional_support.
- university + support is resolved as institutional_support.
