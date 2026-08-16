# AURA-X NEXUS — Hybrid RAG Retrieval Pass

## Implemented

The knowledge base now has an explicit integration point for a real local `EmbeddingRuntime`.

Indexed chunks may contain:

- `embedding`
- `embeddingModel`
- canonical chunk identity/source/text

Hybrid retrieval uses:

```text
Query
  ├── lexical TF/IDF
  └── local model embedding
           ↓
       cosine similarity
           ↓
  weighted hybrid score
           ↓
        top-K
```

The semantic branch is fail-closed. If the runtime is unavailable, embedding fails, or vector dimensions are incompatible, the system returns the deterministic lexical results instead.

## Important boundary

This commit does **not** claim that a dedicated embedding model is bundled. The `EmbeddingRuntime` contract is now real and the vector storage/retrieval path is ready, but capability status must remain gated until a model-backed implementation is installed and verified.

## Compatibility

Legacy records without embeddings remain searchable through lexical retrieval. Legacy pseudo-vector data is not treated as semantic data.

## Next

1. Connect a verified local model-backed embedding implementation.
2. Persist its model identity and dimension during indexing.
3. Re-index stale chunks when the embedding model changes.
4. Add bounded hybrid re-ranking.
5. Add RAG provenance/citations to generated context.
6. Add unit/integration coverage for vector corruption and model changes.
