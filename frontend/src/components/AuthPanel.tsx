"use client";

import type { User } from "@/lib/api-types";
import { ApiError, readJson } from "@/lib/bff-fetch";
import { FormEvent, useState, useTransition } from "react";

type AuthPanelProps = {
  onAuthenticated: (user: User) => void;
  onCancel?: () => void;
};

type AuthMode = "login" | "signup";

export function AuthPanel({ onAuthenticated, onCancel }: AuthPanelProps) {
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

    startTransition(() => {
      void submitAuth(form, { email, password, nickname });
    });
  }

  async function submitAuth(
    form: HTMLFormElement,
    payload: { email: string; password: string; nickname: string },
  ) {
    setError(undefined);
    try {
      const response = await fetch(`/api/auth/${mode}`, {
        method: "POST",
        headers: { "content-type": "application/json" },
        body: JSON.stringify({
          email: payload.email,
          password: payload.password,
          ...(mode === "signup" ? { nickname: payload.nickname } : {}),
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
  }

  return (
    <section className="auth-panel" aria-label="로그인 또는 회원가입">
      <header className="auth-panel-header">
        <h1>{mode === "login" ? "로그인" : "회원가입"}</h1>
        {onCancel ? (
          <button
            className="ghost-button"
            type="button"
            aria-label="홈으로 이동"
            onClick={onCancel}
          >
            홈
          </button>
        ) : null}
      </header>

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
        <button className="primary-button wide" type="submit" disabled={isPending}>
          {isPending
            ? "처리 중"
            : mode === "login"
              ? "입장"
              : "가입"}
        </button>
      </form>
    </section>
  );
}
