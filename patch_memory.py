import sys

path = r'C:\workspace\memory\projects\trackless\technical-context.md'
content = open(path, encoding='utf8').read()

# Update version mention
content = content.replace("Version is currently `v1.6.3`.", "Version is currently `v1.6.5`.")
content = content.replace("- Current release: `1.5.2`", "- Current release: `1.6.5`")

# Add 1.6.5 notes before Technical working rules
old_rules = "## Technical working rules"
new_notes = """## 1.6.4 / 1.6.5 Updates
- **Liquid Glass Custom Time Picker:** Replaced the native Android `DatePickerDialog` and `TimePickerDialog` (which looked out of place) with a unified `CustomTimePickerDialog`. It includes quick offset pills (-5m, -15m, -1h), a native-feeling number scroller, and relative date pills (Today, Yesterday, 2 days ago).
- **UI Tweaks:** Moved the trigger button for the Custom Time Picker from `HeroCard` to the top-right corner of the `StatsGrid` "History" section for better visibility and to prevent accidental Undo clicks. Restored the Undo button to its sole position in `HeroCard`.
- **Encoding Fix:** Fixed a UTF-16 BOM issue in `VERSION.txt` caused by PowerShell `echo` which resulted in corrupted tag names in GitHub Releases. Enforced strict UTF-8 for `VERSION.txt`.

## Technical working rules"""

if "## 1.6.4" not in content:
    content = content.replace(old_rules, new_notes)

with open(path, 'w', encoding='utf8') as f:
    f.write(content)
print("Updated memory technical-context.md")
