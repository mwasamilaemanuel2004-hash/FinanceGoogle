/**
 * ============================================================================
 * ESMH.TRADE / MWASA AI — RAG VECTOR DATABASE (LOCAL INDEXEDDB / IN-MEMORY)
 * ============================================================================
 * High-performance, low-latency (<20ms) vector similarity search
 * operating over 200-dimensional embeddings using Cosine Similarity.
 */

export interface VectorDocument {
  id: string;
  sourceType: 'BOOK' | 'SKILL' | 'HISTORICAL_TRADE';
  title: string;
  authorOrVersion: string;
  content: string;
  metadata: Record<string, any>;
  embedding: number[];
}

export class RagVectorDb {
  private static instance: RagVectorDb;
  private documents: Map<string, VectorDocument> = new Map();

  private constructor() {}

  public static getInstance(): RagVectorDb {
    if (!RagVectorDb.instance) {
      RagVectorDb.instance = new RagVectorDb();
    }
    return RagVectorDb.instance;
  }

  public upsert(doc: VectorDocument): void {
    this.documents.set(doc.id, doc);
  }

  public upsertBatch(docs: VectorDocument[]): void {
    for (const d of docs) {
      this.documents.set(d.id, d);
    }
  }

  public getDocumentCount(): number {
    return this.documents.size;
  }

  /**
   * Cosine similarity between two vectors
   */
  public cosineSimilarity(a: number[], b: number[]): number {
    if (a.length !== b.length || a.length === 0) return 0;
    let dot = 0;
    let normA = 0;
    let normB = 0;
    for (let i = 0; i < a.length; i++) {
      dot += a[i] * b[i];
      normA += a[i] * a[i];
      normB += b[i] * b[i];
    }
    if (normA === 0 || normB === 0) return 0;
    return dot / (Math.sqrt(normA) * Math.sqrt(normB));
  }

  /**
   * Fast vector similarity query (<20ms)
   */
  public query(queryVector: number[], topK: number = 5, minSimilarity: number = 0.25): Array<{ doc: VectorDocument; score: number }> {
    const results: Array<{ doc: VectorDocument; score: number }> = [];

    for (const doc of this.documents.values()) {
      const sim = this.cosineSimilarity(queryVector, doc.embedding);
      if (sim >= minSimilarity) {
        results.push({ doc, score: Number(sim.toFixed(4)) });
      }
    }

    results.sort((a, b) => b.score - a.score);
    return results.slice(0, topK);
  }

  public getAll(): VectorDocument[] {
    return Array.from(this.documents.values());
  }

  public clear(): void {
    this.documents.clear();
  }
}
