import type { Language } from "../api/types";
export const DEMO_BATCH_ID = "demo-hw4";
export const DEFAULT_THRESHOLD = 45;
export const DEFAULT_K = 5;
export const MAX_ARCHIVE_MB = 10;
export const MAX_ARCHIVE_BYTES = MAX_ARCHIVE_MB * 1024 * 1024;
export const MIN_WORKS = 5;
export const MAX_WORKS = 50;
export const LANGUAGES: { value: Language; label: string; ext: string }[] = [{ value: "java", label: "Java", ext: ".java" }];