import { parseOptionalJsonObject } from "@/lib/json-object";
import { describe, expect, it } from "vitest";

describe("parseOptionalJsonObject", () => {
  it("accepts an object and an empty optional value", () => {
    expect(parseOptionalJsonObject('{"tone":"calm"}', "말투")).toEqual({
      tone: "calm",
    });
    expect(parseOptionalJsonObject("  ", "말투")).toBeUndefined();
  });

  it.each(["[]", '"calm"', "null", "1"])(
    "rejects non-object JSON: %s",
    (value) => {
      expect(() => parseOptionalJsonObject(value, "말투")).toThrow(
        "말투 항목은 JSON object여야 합니다.",
      );
    },
  );

  it("rejects malformed JSON before submission", () => {
    expect(() => parseOptionalJsonObject("{", "성격")).toThrow(
      "성격 항목은 올바른 JSON이어야 합니다.",
    );
  });
});
