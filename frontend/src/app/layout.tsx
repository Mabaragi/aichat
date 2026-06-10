import type { Metadata } from "next";
import "@/app/globals.css";

export const metadata: Metadata = {
  title: "AI Debate Studio",
  description: "캐릭터 기반 AI 토론 설계 workspace",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}
