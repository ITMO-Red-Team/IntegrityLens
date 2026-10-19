import type { components } from "./schema";

type S = components["schemas"];

export type Batch = S["Batch"];
export type Run = S["Run"];
export type Method = Run["method"];
export type BatchStatus = Batch["status"];
export type Submission = S["Submission"];
export type SubmissionInfo = S["SubmissionInfo"];
export type Submissions = S["Submissions"];
export type PairSummary = S["PairSummary"];
export type PairDetail = S["PairDetail"];
export type PairScore = S["PairScore"];
export type Pairs = S["Pairs"];
export type Reason = S["Reason"];
export type Feedback = S["Feedback"];
export type FeedbackRequest = S["FeedbackRequest"];
export type Decision = Feedback["decision"];
export type ApiErrorBody = S["Error"];

export type Language = "java";

export interface NewBatchInput {
  language: Language;
  archive: File;
  title?: string;
}