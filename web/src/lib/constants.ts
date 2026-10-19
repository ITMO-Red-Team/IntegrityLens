import type { Method } from "../api/types";

export const DEFAULT_THRESHOLD = 45;
export const DEFAULT_K = 5;

export const DEFAULT_METHOD: Method = "jplag";

export const METHODS: { value: Method; label: string; hint: string }[] = [
  { value: "jplag", label: "JPlag", hint: "базовый метод: сравнение по токенам кода" },
  { value: "structsim", label: "structsim", hint: "исследуемый метод: сравнение структуры кода" },
];

export const methodLabel = (m: string) => METHODS.find((x) => x.value === m)?.label ?? m;

export const POLL_MS = 1000;

export const MAX_ARCHIVE_MB = 10;
export const MAX_ARCHIVE_BYTES = MAX_ARCHIVE_MB * 1024 * 1024;
export const MIN_WORKS = 5;
export const MAX_WORKS = 50;