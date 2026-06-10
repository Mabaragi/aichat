"use client";

import type { User } from "@/lib/api-types";
import { ApiError, readJson } from "@/lib/bff-fetch";
import { FormEvent, useState, useTransition } from "react";

type AuthPanelProps = {
  onAuthenticated: (user: User) => void;
};

type AuthMode = "login" | "signup";

export function AuthPanel({ onAuthenticated }: AuthPanelProps) {
  const [mode, setMode] = useState<AuthMode>("login");
  const [error, setError] = useState<string>();
  const [isPending, startTransition] = useTransition();

  function switchMode(nextMode: AuthMode) {
    setError(undefined);
    setMode(nextMode);
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    const formData = new FormData(form);
    const email = String(formData.get("email") ?? "").trim();
    const password = String(formData.get("password") ?? "");
    const nickname = String(formData.get("nickname") ?? "").trim();

    if (!email || !password || (mode === "signup" && !nickname)) {
      setError("필수 항목을 모두 입력해 주세요.");
      return;
    }
    if (mode === "signup" && password.length < 8) {
      setError("비밀번호는 8자 이상이어야 합니다.");
      return;
    }

    startTransition(async () => {
      setError(undefined);
      try {
        const response = await fetch(`/api/auth/${mode}`, {
          method: "POST",
          headers: { "content-type": "application/json" },
          body: JSON.stringify({
            email,
            password,
            ...(mode === "signup" ? { nickname } : {}),
          }),
        });
        const data = await readJson<{ user: User }>(response);
        form.reset();
        onAuthenticated(data.user);
      } catch (caught) {
        setError(
          caught instanceof ApiError
            ? caught.message
            : "인증 요청을 처리하지 못했습니다.",
        );
      }
    });
  }

  return (
    <main className="auth-shell">
      <section className="auth-intro" aria-labelledby="auth-title">
        <p className="eyebrow">AI DEBATE STUDIO</p>
        <h1 id="auth-title">
          관점을 만들고,
          <br />
          토론을 설계하세요.
        </h1>
        <p className="auth-copy">
          캐릭터의 성격과 말투를 정한 뒤 두 참가자를 한 무대에 올립니다.
        </p>
        <div className="auth-rule" aria-hidden="true">
          <span>01</span>
          <span>CHARACTER</span>
          <span>02</span>
          <span>DEBATE</span>
        </div>
      </section>

      <section className="auth-form-panel" aria-label="인증">
        <div className="auth-tabs" role="tablist" aria-label="인증 방식">
          <button
            type="button"
            role="tab"
            aria-selected={mode === "login"}
            className={mode === "login" ? "active" : ""}
            onClick={() => switchMode("login")}
          >
            로그인
          </button>
          <button
            type="button"
            role="tab"
            aria-selected={mode === "signup"}
            className={mode === "signup" ? "active" : ""}
            onClick={() => switchMode("signup")}
          >
            회원가입
          </button>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            이메일
            <input
              name="email"
              type="email"
              autoComplete="email"
              placeholder="name@example.com"
              required
            />
          </label>
          {mode === "signup" ? (
            <label className="field-reveal">
              닉네임
              <input
                name="nickname"
                type="text"
                autoComplete="nickname"
                placeholder="토론가"
                required
              />
            </label>
          ) : null}
          <label>
            비밀번호
            <input
              name="password"
              type="password"
              autoComplete={mode === "signup" ? "new-password" : "current-password"}
              minLength={mode === "signup" ? 8 : undefined}
              placeholder={mode === "signup" ? "8자 이상" : "비밀번호"}
              required
            />
          </label>
          {error ? <p className="form-error">{error}</p> : null}
          <button className="primary-button" type="submit" disabled={isPending}>
            {isPending
              ? "처리 중"
              : mode === "login"
                ? "작업 공간 열기"
                : "계정 만들기"}
          </button>
        </form>
      </section>
    </main>
  );
}
