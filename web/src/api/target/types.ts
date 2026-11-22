import type { components } from "./schema";

type Schemas = components["schemas"];

export type User = Schemas["User"];

export type Assignment = Schemas["Assignment"];
export type AssignmentSummary = Schemas["AssignmentSummary"];
export type AssignmentAnalysisProgress =
  Schemas["AssignmentAnalysisProgress"];

export type CreateAssignmentRequest =
  Schemas["CreateAssignmentRequest"];

export type SubmissionList = Schemas["SubmissionList"];
export type SubmissionRow = Schemas["SubmissionRow"];
export type SubmissionDetail = Schemas["SubmissionDetail"];

export type Score = Schemas["Score"];
export type ScoreStatus = Score["status"];

export type SimilarWork = Schemas["SimilarWork"];
export type SuspiciousRange = Schemas["SuspiciousRange"];

export type PairResult = Schemas["PairResult"];
export type StructsimResult = Schemas["StructsimResult"];

export type ApiErrorBody = Schemas["Error"];