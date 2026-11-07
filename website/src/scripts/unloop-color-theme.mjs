import { argbFromHex, hexFromArgb, themeFromSourceColor, Hct } from '@material/material-color-utilities';

let cachedSeed, cachedTheme;
export function colorThemeTokens(seed, dark) {
  if (!/^#[\da-f]{6}$/i.test(seed)) throw new Error('Expected a six-digit hex color.');
  if (cachedSeed !== seed) {
    cachedTheme = themeFromSourceColor(argbFromHex(seed));
    cachedSeed = seed;
  }
  const scheme = dark ? cachedTheme.schemes.dark : cachedTheme.schemes.light;
  const light = cachedTheme.schemes.light;
  const night = cachedTheme.schemes.dark;
  const hex = hexFromArgb;
  const primary = hex(scheme.primary);
  const ink = hex(scheme.onPrimaryContainer);
  return {
    '--surface': hex(cachedTheme.palettes.neutral.tone(dark ? 6 : 98)),
    '--text': hex(scheme.onSurface),
    '--muted': hex(scheme.onSurfaceVariant),
    '--primary': primary,
    '--on-primary': hex(scheme.onPrimary),
    '--container': hex(cachedTheme.palettes.neutral.tone(dark ? 12 : 94)),
    '--primary-container': hex(scheme.secondaryContainer),
    '--tertiary-container': hex(scheme.tertiaryContainer),
    '--outline': hex(scheme.outlineVariant),
    '--focus-bg': hex(scheme.primaryContainer),
    '--focus-ink': ink,
    '--timer-track': `${ink}26`,
    '--timer-fill': primary,
    '--preset-bg': `${ink}14`,
    '--preset-color': hex(scheme.onPrimary),
    '--swatch-light-surface': hex(cachedTheme.palettes.neutral.tone(98)),
    '--swatch-light-primary': hex(light.primary),
    '--swatch-dark-surface': hex(cachedTheme.palettes.neutral.tone(6)),
    '--swatch-dark-primary': hex(night.primary),
  };
}

export function hueFromColor(color) {
  return Hct.fromInt(argbFromHex(color)).hue;
}
export function sourceColorFromHue(hue) {
  return hexFromArgb(Hct.from(Math.max(0, Math.min(360, hue)), 48, 50).toInt());
}
const hueGradients = new Map();
export function themeHueGradient(dark = false) {
  if (!hueGradients.has(dark)) {
    const tone = dark ? 70 : 58;
    const stops = Array.from({ length: 25 }, (_, index) => hexFromArgb(Hct.from(index * 15, 48, tone).toInt()));
    hueGradients.set(dark, `linear-gradient(to right, ${stops.join(', ')})`);
  }
  return hueGradients.get(dark);
}
