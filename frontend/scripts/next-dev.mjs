import { spawn } from "node:child_process";

const MODE_ARG = "--api-mode=";
const args = process.argv.slice(2);
const explicitMode = args
  .find((arg) => arg.startsWith(MODE_ARG))
  ?.slice(MODE_ARG.length);
const passthroughArgs = args.filter((arg) => !arg.startsWith(MODE_ARG));
const apiMode = explicitMode ?? process.env.FRONTEND_API_MODE ?? "mock";

const child = spawn("next", ["dev", ...passthroughArgs], {
  env: {
    ...process.env,
    AUTH_COOKIE_SECURE: process.env.AUTH_COOKIE_SECURE ?? "false",
    FRONTEND_API_MODE: apiMode,
  },
  shell: true,
  stdio: "inherit",
});

child.on("exit", (code, signal) => {
  if (signal) {
    process.kill(process.pid, signal);
    return;
  }
  process.exit(code ?? 0);
});
