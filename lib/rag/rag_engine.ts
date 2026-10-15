/**
 * ============================================================================
 * ESMH.TRADE / MWASA AI — RAG ENGINE (CENTRAL KNOWLEDGE & DECISION ENHANCER)
 * ============================================================================
 * Coordinates the Vector DB, Books Library, and RAG Retriever to inject
 * proven quant literature wisdom directly into Deep Thinking Step 6.
 */

import { RagRetriever, RagQueryPayload, RagRetrievalResult } from './rag_retriever';
import { RAG_BOOKS_COLLECTION, RagBookDoc } from './books_library';
import { RagVectorDb } from './vector_db';

export class RagEngine {
  private static instance: RagEngine;
  private retriever: RagRetriever;

  private constructor() {
    this.retriever = new RagRetriever();
  }

  public static getInstance(): RagEngine {
    if (!RagEngine.instance) {
      RagEngine.instance = new RagEngine();
    }
    return RagEngine.instance;
  }

  /**
   * Fast query for decision pipeline <20ms
   */
  public queryKnowledge(payload: RagQueryPayload): RagRetrievalResult {
    return this.retriever.retrieve(payload, 5);
  }

  /**
   * Deduplication check for safe learning
   */
  public verifySkillOriginality(keywords: string): { isOriginal: boolean; duplicateOf?: string; correlation: number } {
    const check = this.retriever.checkSkillDuplication(keywords);
    return {
      isOriginal: !check.isDuplicate,
      duplicateOf: check.similarSkillTitle,
      correlation: check.maxCorrelation
    };
  }

  public getBooksCatalog(): RagBookDoc[] {
    return RAG_BOOKS_COLLECTION;
  }

  public getTotalDocsCount(): number {
    return RagVectorDb.getInstance().getDocumentCount();
  }
}
