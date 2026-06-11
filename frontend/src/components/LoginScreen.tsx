"use client";

import { AuthPanel } from "@/components/AuthPanel";
import { useRouter } from "next/navigation";

export function LoginScreen() {
  const router = useRouter();

  function handleAuthenticated() {
    router.push("/");
    router.refresh();
  }

  return (
    <main className="login-shell">
      <section className="login-visual" aria-hidden="true">
        <div className="geometric-shape shape-one" />
        <div className="geometric-shape shape-two" />
        <p>AI Debate Arena</p>
        <h1>내 캐릭터와 토론을 이어가려면 로그인하세요.</h1>
      </section>
      <AuthPanel
        onAuthenticated={handleAuthenticated}
        onCancel={() => router.push("/")}
      />
    </main>
  );
}
