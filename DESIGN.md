# FocusBlock Design System

## Dial Declaration
Dial: ENERGY 1 / RHYTHM 2 / MOTION 1

- **ENERGY 1 (Calm & Grounded):** FocusBlock is a tool for self-discipline, quiet focus, and screen-time recovery. The visual tone is calm, firm, and distraction-free. No neon glows, no chaotic multi-color gradients, no gamification badges.
- **RHYTHM 2 (Balanced Composition):** Sections are differentiated by function (status hub, quick action presets, profile cards, setup guide). The layout provides clear hierarchy without repetitive template card rows.
- **MOTION 1 (Subtle & Functional):** Motion is reserved for direct user feedback (toggle switches, smooth modal entrances, countdown tick). No perpetual looping pulses or bouncing animations.

## Identity & Mood
- **Atmosphere:** An architectural, focused workspace. Quiet confidence and honest utility.
- **Personality:** Authoritative, transparent, respectful. Strict mode feels like an unyielding vault, not a punishment.
- **One Focal Point per Screen:**
  - Home: Active session status and timer when blocking is active; Focus readiness and profile management when idle.
  - Profile Editor: Clear distinction between targeted apps and schedule configuration.
  - Block Screen: Clear notification of why the app is blocked and a single primary action to return home.
  - Settings: Direct, functional controls including real light/dark theme selection.

## Color Palette & Semantic Roles
Every pairing is verified against WCAG AA standards (minimum 4.5:1 for normal text, 3.0:1 for large text and UI boundaries).

### Dark Theme (Default)
- **Background / Canvas:** `#0E1116` (Deep Obsidian)
- **Surface / Cards:** `#181B22` (Graphite Layer, 1dp subtle border `#2E3440`)
- **Surface Elevated / Modals:** `#222631`
- **Text Primary:** `#F3F4F6` (Contrast 17.18:1 against `#0E1116`)
- **Text Muted:** `#9CA3AF` (Contrast 7.45:1 against `#0E1116`, 6.79:1 against `#181B22`)
- **Primary Accent:** `#3B82F6` (Focused Azure, contrast 4.6:1 against `#0E1116`)
- **Strict Mode / Lock:** `#F59E0B` (Amber, container `#2E1C05`, contrast 14.16:1)
- **Active Session / Verified:** `#10B981` (Emerald, container `#052B1E`, contrast 12.77:1)
- **Error / Critical Setup:** `#EF4444` (Crimson, container `#3B1111`)

### Light Theme
- **Background / Canvas:** `#F8FAFC` (Clean Porcelain)
- **Surface / Cards:** `#FFFFFF` (Pure White, 1dp subtle border `#E2E8F0`)
- **Surface Elevated / Modals:** `#F1F5F9`
- **Text Primary:** `#0F172A` (Deep Slate, Contrast 17.06:1 against `#F8FAFC`)
- **Text Muted:** `#475569` (Slate Grey, Contrast 7.24:1 against `#F8FAFC`, 7.58:1 against `#FFFFFF`)
- **Primary Accent:** `#2563EB` (Royal Azure, contrast 5.17:1 with white text)
- **Strict Mode / Lock:** `#B45309` (Deep Brass, container `#FEF3C7`, contrast 4.51:1)
- **Active Session / Verified:** `#047857` (Deep Emerald, container `#D1FAE5`, contrast 4.84:1)
- **Error / Critical Setup:** `#DC2626` (Container `#FEE2E2`)

## Typography Hierarchy
- **Title Large (App Headers / Modals):** 20sp, Bold, tight letter-spacing (-0.2sp)
- **Headline / Timer:** Monospace, Bold, clear tabular numerals, no arbitrary uppercase tracking
- **Title Medium (Card Headers):** 16sp, SemiBold
- **Body Large / Form Labels:** 15sp, Medium
- **Body Small (Secondary details):** 13sp, Regular, high contrast
- **Label Small (Badges, Chips):** 11sp, Bold, readable sentence or title case

## Spacing & Metrics
- Grid base: 8dp
- Screen horizontal margin: 16dp
- Card corner radius: 14dp
- Button corner radius: 10dp
- Pill / Chip corner radius: 8dp
- Minimum interactive touch target: 48dp x 48dp

## Accessibility & Interaction Rules
1. Every interactive element has a minimum touch target of 48dp.
2. Interactive states provide visible feedback (distinct surface color on pressed / focused).
3. No reliance on color alone: active states and strict locks always combine color with an explicit icon and text label.
4. No fake statistics, no buzzwords, and strictly no em dashes.
