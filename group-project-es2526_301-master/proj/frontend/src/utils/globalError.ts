export const showGlobalError = (message: string) => {
  try {
    window.dispatchEvent(new CustomEvent("global-error", { detail: { message } }));
  } catch (e) {
    // fallback: log
    console.error("Global error dispatch failed", e);
  }
};

export const clearGlobalError = () => {
  try {
    window.dispatchEvent(new CustomEvent("global-error-clear"));
  } catch (e) {
    // ignore
  }
};
