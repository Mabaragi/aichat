"use client";

import { AuthPanel } from "@/components/AuthPanel";
import Link from "next/link";
import { useRouter } from "next/navigation";

export function LoginScreen() {
  const router = useRouter();

  function handleAuthenticated() {
    router.push("/");
    router.refresh();
  }

  return (
    <main className="login-shell">
      <header className="auth-top-navigation">
        <Link className="brand-mark" href="/" aria-label="AI Debate Arena 홈">
          <span>AI</span>
          Debate Arena
        </Link>
        <nav aria-label="로그인 메뉴">
          <Link href="/">공개 탐색</Link>
          <span>로그인</span>
        </nav>
      </header>

      <section className="login-stage" aria-label="로그인">
        <span className="login-shape login-shape-one" aria-hidden="true" />
        <span className="login-shape login-shape-two" aria-hidden="true" />
        <AuthPanel
          onAuthenticated={handleAuthenticated}
          onCancel={() => router.push("/")}
        />
      </section>
    </main>
  );
}
