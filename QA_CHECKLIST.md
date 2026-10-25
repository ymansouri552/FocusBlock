# FocusBlock QA Checklist

## 1. Schedule engine (unit tests)
- [ ] Recurring within window -> active
- [ ] Recurring outside window -> not active
- [ ] Recurring crossing midnight (e.g. 22:00-02:00) -> active at 01:00
- [ ] Recurring crossing midnight -> not active before start (21:00) and after end (03:00)
- [ ] One-off within range -> active; before/after -> not active
- [ ] Quick block within duration -> active; after duration -> not active
- [ ] Multiple profiles: any active profile blocks its apps
- [ ] Disabled profile -> not active
- [ ] Profile with no schedules -> not active
- [ ] DST transition day -> no crash, zone-aware result

## 2. Strict mode rules (unit tests)
- [ ] Strict + active: cannot deactivate, delete, edit name, remove apps, shorten end time
- [ ] Strict + active: can add apps, extend end time
- [ ] Strict + inactive: can deactivate/delete/edit
- [ ] Non-strict + active: can deactivate

## 3. Profile CRUD UI
- [ ] Create profile with name + blocked apps + schedule
- [ ] Edit existing profile
- [ ] Toggle enabled/disabled
- [ ] Delete profile (blocked when strict + active)
- [ ] Strict toggle shows confirmation with end time

## 4. Blocking engine
- [ ] Blocked app foreground -> block screen shown, sent home
- [ ] Block screen shows app name, profile name, time remaining
- [ ] Back button does not return to blocked app
- [ ] "Go home" button returns to home
- [ ] Persistent notification shows active blocks

## 5. Strict Mode enforcement
- [ ] Device Admin enable/disable flow works
- [ ] `onDisableRequested` warning shown
- [ ] While strict active: Settings app / admin settings / app info blocked (Targeted or Aggressive)
- [ ] Clock change backward does not shorten strict block
- [ ] Reboot -> block state restored
- [ ] Debug flag shortens strict durations and allows immediate unlock

## 6. Onboarding / permissions
- [ ] Permission wizard shows status for each permission
- [ ] Deep-link to fix each permission
- [ ] Android 13+ "Allow restricted settings" instructions for sideload
- [ ] Play flavor: consent screen before Accessibility + Device Admin

## 7. OEM variants (Samsung / Xiaomi / Oppo etc.)
- [ ] Settings package names configurable and extensible
- [ ] Test strict-level blocking on at least one OEM skin

## 8. Privacy / distribution
- [ ] No `INTERNET` permission
- [ ] No `QUERY_ALL_PACKAGES`
- [ ] All data local; export to JSON works locally
