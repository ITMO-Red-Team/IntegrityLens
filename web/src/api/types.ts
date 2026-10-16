export type Language = "java";
export type BatchStatus = "queued" | "processing" | "done" | "failed";
export type Decision = "confirmed" | "acceptable" | "false_positive";

export interface Stage {
  name: string;
  status: "pending" | "running" | "done" | "failed";
  durationMs?: number;
}

export interface Submission {
  id: string;
  fileName: string;
  submittedAt: string;
  loc: number;
}

export interface Batch {
  id: string;
  course: string;
  assignment: string;
  language: Language;
  status: BatchStatus;
  stages: Stage[];
  submissions: Submission[];
  pairCount: number;
  createdAt: string;
}

export interface Signals {
  baseline: number;
  structure: number;
  coverage: number;
  embedding: number;
}

export interface PairSummary {
  id: string;
  a: string;
  b: string;
  risk: number;
  signals: Signals;
  fragmentCount: number;
  decision?: Decision | null;
}

export interface PairList {
  pairs: PairSummary[];
  clusters: string[][];
  total: number;
}

export type LineKind = "code" | "blank" | "comment" | "template";
export interface CodeLine {
  text: string;
  kind: LineKind;
}

export interface SubmissionCode extends Submission {
  lines: CodeLine[];
}

export interface Fragment {
  index: number;
  a: [number, number];
  b: [number, number];
  length: number;
}

export interface FeedbackInput {
  decision: Decision;
  comment?: string;
}

export interface Feedback extends FeedbackInput {
  updatedAt: string;
}

export interface PairDetail extends PairSummary {
  batchId: string;
  rank: number;
  submissionA: SubmissionCode;
  submissionB: SubmissionCode;
  fragments: Fragment[];
  renames: [string, string][];
  explanation: string;
  feedback?: Feedback | null;
}

export interface NewBatchInput {
  course: string;
  assignment: string;
  language: Language;
  archive: File;
}

export type ExportFormat = "csv" | "json";