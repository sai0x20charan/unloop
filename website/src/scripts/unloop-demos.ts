import { createFocusTimer, focusProgress } from './unloop-demo-state.mjs';
import { colorThemeTokens, hueFromColor, sourceColorFromHue, themeHueGradient } from './unloop-color-theme.mjs';

const get = <T extends HTMLElement>(id: string) => document.getElementById(id) as T;

// Focus timer
const timer = createFocusTimer();
let duration = 25 * 60;
const progressRing = document.querySelector<SVGCircleElement>('#focus-progress')!;
const time = get<HTMLOutputElement>('focus-time');
const status = get<HTMLElement>('focus-state');
const toggle = get<HTMLButtonElement>('focus-toggle');
const lock = get<HTMLInputElement>('lock-controls');
const presets = document.querySelectorAll<HTMLButtonElement>('[data-minutes]');
let interval: ReturnType<typeof setInterval> | undefined;
let started = false;
function renderTimer() {
  const seconds = timer.seconds();
  time.value = `${Math.floor(seconds / 60).toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}`;
  progressRing.style.strokeDashoffset = String(100 - focusProgress(seconds, duration));
  const state = timer.running ? (lock.checked ? 'Locked' : 'Focusing') : seconds === 0 ? 'Done' : started ? 'Paused' : '';
  if (status.textContent !== state) status.textContent = state;
  toggle.textContent = timer.running ? 'Pause' : seconds === 0 ? 'Again' : started ? 'Resume' : 'Start';
  toggle.disabled = timer.running && lock.checked;
  toggle.title = toggle.disabled ? 'Session controls are locked. Turn off Lock session controls to pause.' : '';
  presets.forEach(button => { button.disabled = timer.running; });
  if (!timer.running) { clearInterval(interval); interval = undefined; }
}
toggle.addEventListener('click', () => {
  if (timer.running) timer.pause();
  else { timer.start(); started = true; interval = setInterval(renderTimer, 250); }
  renderTimer();
});
presets.forEach(button => button.addEventListener('click', () => {
  duration = Number(button.dataset.minutes) * 60;
  timer.reset(duration / 60); started = false;
  presets.forEach(other => other.setAttribute('aria-pressed', String(other === button)));
  renderTimer();
}));
document.addEventListener('visibilitychange', renderTimer);

// Routine days
const days = document.querySelectorAll<HTMLButtonElement>('[data-day]');
function renderRoutine() {
  const picked = Array.from(days).map(day => day.getAttribute('aria-pressed') === 'true');
  const names = Array.from(days).filter((_, index) => picked[index]).map(day => day.dataset.day!.slice(0, 3));
  const key = picked.map(Number).join('');
  get<HTMLOutputElement>('routine-summary').value =
    key === '1111111' ? 'Every day' : key === '1111100' ? 'Weekdays' : key === '0000011' ? 'Weekends' : names.length ? names.join(', ') : 'Pick a day';
}
days.forEach(button => button.addEventListener('click', () => {
  button.setAttribute('aria-pressed', String(button.getAttribute('aria-pressed') !== 'true'));
  renderRoutine();
}));
renderRoutine();

// Blocking rules
const strict = get<HTMLInputElement>('strict-mode');
const wait = get<HTMLInputElement>('unlock-wait');
function renderRules() {
  wait.disabled = strict.checked;
  if (strict.checked) wait.checked = false;
  renderTimer();
}
[strict, wait, lock].forEach(input => input.addEventListener('change', renderRules));
renderRules();

// Statistics
const stats = document.querySelectorAll<HTMLButtonElement>('[data-stats]');
const chart = get<HTMLElement>('stats-chart');
const samples = {
  weekly: { labels: ['M', 'T', 'W', 'T', 'F', 'S', 'S'], values: [25, 30, 0, 45, 15, 35, 0] },
  monthly: { labels: ['W1', 'W2', 'W3', 'W4'], values: [150, 210, 180, 270] },
};
function renderStats(kind: 'weekly' | 'monthly') {
  stats.forEach(button => button.setAttribute('aria-pressed', String(button.dataset.stats === kind)));
  const sample = samples[kind];
  const minutes = sample.values.reduce((sum, value) => sum + value, 0);
  get<HTMLElement>('stats-total').textContent = `${Math.floor(minutes / 60)}h ${minutes % 60}m`;
  chart.setAttribute('aria-label', `Example ${kind} focus minutes: ${sample.values.join(', ')}`);
  const max = Math.max(...sample.values);
  chart.replaceChildren(...sample.values.map((value, index) => {
    const column = document.createElement('div');
    column.className = 'stats-column';
    const bar = document.createElement('span');
    bar.style.height = `${Math.max(6, value / max * 100)}%`;
    if (value === max) bar.className = 'peak';
    const label = document.createElement('small'); label.textContent = sample.labels[index];
    column.append(bar, label); return column;
  }));
}
stats.forEach(button => button.addEventListener('click', () => renderStats(button.dataset.stats as 'weekly' | 'monthly')));
renderStats('weekly');

// Website theme
const scheme = matchMedia('(prefers-color-scheme: dark)');
let selectedAccent: string | null = null;
let selectedHue: number | null = null;
let selectedAppearance = 'system';
const appearanceButtons = document.querySelectorAll<HTMLButtonElement>('[data-appearance]');
const hueInput = get<HTMLInputElement>('theme-hue');
const accentStatus = get<HTMLOutputElement>('accent-status');
const appliedTokens = new Set<string>();

let isInteractingWithHue = false;
hueInput.addEventListener('pointerdown', () => { isInteractingWithHue = true; });
window.addEventListener('pointerup', () => { isInteractingWithHue = false; });
window.addEventListener('pointercancel', () => { isInteractingWithHue = false; });
hueInput.addEventListener('keydown', () => { isInteractingWithHue = true; });
hueInput.addEventListener('keyup', () => { isInteractingWithHue = false; });

const defaultHue = Math.round(hueFromColor('#35693e'));

try {
  const appearance = localStorage.getItem('unloop-theme');
  if (appearance === 'light' || appearance === 'dark') selectedAppearance = appearance;
  const stored = localStorage.getItem('unloop-accent');
  if (stored && /^#[\da-f]{6}$/i.test(stored)) {
    selectedAccent = stored.toLowerCase();
    const storedHue = localStorage.getItem('unloop-hue');
    if (storedHue !== null && !isNaN(Number(storedHue))) {
      selectedHue = Number(storedHue);
    } else {
      selectedHue = Math.round(hueFromColor(selectedAccent)) % 360;
    }
  }
} catch {}
function renderTheme() {
  document.documentElement.dataset.theme = selectedAppearance;
  const dark = selectedAppearance === 'dark' || (selectedAppearance === 'system' && scheme.matches);
  hueInput.style.setProperty('--hue-gradient', themeHueGradient(dark));
  appearanceButtons.forEach(button => button.setAttribute('aria-pressed', String(button.dataset.appearance === (dark ? 'dark' : 'light'))));
  if (selectedAccent) {
    const tokens = colorThemeTokens(selectedAccent, dark);
    Object.entries(tokens).forEach(([key, value]) => {
      document.documentElement.style.setProperty(key, value); appliedTokens.add(key);
    });
  } else {
    appliedTokens.forEach(key => document.documentElement.style.removeProperty(key));
    appliedTokens.clear();
  }
  const surface = getComputedStyle(document.documentElement).getPropertyValue('--surface').trim();
  document.querySelector('meta[name="theme-color"]')?.setAttribute('content', surface);
  const hue = selectedHue ?? defaultHue;
  if (!isInteractingWithHue && document.activeElement !== hueInput) {
    hueInput.value = String(hue);
  }
  hueInput.setAttribute('aria-valuetext', `${hue} degrees`);
  accentStatus.value = selectedAccent ? 'Theme color updated.' : 'Default Unloop colors restored.';
}
scheme.addEventListener('change', renderTheme);
renderTheme();

let accentFrame = 0;
function setAccent(color: string | null, hue?: number) {
  selectedAccent = color;
  selectedHue = hue !== undefined ? hue : (color ? Math.round(hueFromColor(color)) % 360 : null);
  try {
    if (color) {
      localStorage.setItem('unloop-accent', color);
      if (selectedHue !== null) localStorage.setItem('unloop-hue', String(selectedHue));
    } else {
      localStorage.removeItem('unloop-accent');
      localStorage.removeItem('unloop-hue');
    }
  } catch {}
  cancelAnimationFrame(accentFrame);
  accentFrame = requestAnimationFrame(renderTheme);
}

appearanceButtons.forEach(button => button.addEventListener('click', () => {
  selectedAppearance = button.dataset.appearance!;
  try { localStorage.setItem('unloop-theme', selectedAppearance); } catch {}
  renderTheme();
}));

hueInput.addEventListener('input', () => {
  const hue = Number(hueInput.value);
  setAccent(sourceColorFromHue(hue), hue);
});
const resetBtn = get<HTMLButtonElement>('theme-color-reset');
resetBtn.addEventListener('click', () => {
  setAccent(null);
});
