// Timestamp-based timing also stays accurate when the browser throttles the tab.
export function createFocusTimer(minutes = 25) {
  let duration = minutes * 60, remaining = duration, deadline = null;
  return {
    get running() { return deadline !== null; },
    seconds(now = Date.now()) {
      if (deadline !== null) remaining = Math.max(0, Math.ceil((deadline - now) / 1000));
      if (!remaining) deadline = null;
      return remaining;
    },
    start(now = Date.now()) { if (!remaining) remaining = duration; deadline = now + remaining * 1000; },
    pause(now = Date.now()) { this.seconds(now); deadline = null; },
    reset(nextMinutes = duration / 60) { duration = nextMinutes * 60; remaining = duration; deadline = null; },
  };
}

export function focusProgress(remaining, duration) {
  return duration > 0 ? Math.max(0, Math.min(100, remaining / duration * 100)) : 0;
}
