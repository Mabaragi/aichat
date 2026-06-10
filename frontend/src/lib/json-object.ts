export function parseOptionalJsonObject(
  value: string,
  label: string,
): Record<string, unknown> | undefined {
  if (!value.trim()) {
    return undefined;
  }

  let parsed: unknown;
  try {
    parsed = JSON.parse(value);
  } catch {
    throw new Error(`${label}은 올바른 JSON이어야 합니다.`);
  }

  if (
    parsed === null ||
    Array.isArray(parsed) ||
    typeof parsed !== "object"
  ) {
    throw new Error(`${label}은 JSON object여야 합니다.`);
  }

  return parsed as Record<string, unknown>;
}
