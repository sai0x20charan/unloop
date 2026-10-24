# Play Console declarations

These answers describe Unloop's current focus-session functionality. Submit them under **Monitor and improve > App content**. The foreground service needs a reviewer-accessible demonstration video URL before you can submit it.

## Foreground service

**Does your app use foreground service permissions?** Yes.

**Type:** Special use, `FOREGROUND_SERVICE_SPECIAL_USE`.

**Use case:** Choose Other or enter a custom use case if the form offers it.

**Use case name:** Focus-session app blocking.

**Describe the functionality:**

> Unloop helps users limit distracting apps during focus sessions. The user selects apps to block and starts a timed session, or creates an enabled routine with a start time and end time. During the session, a foreground service monitors which app is open and displays a blocking screen when a selected app is opened. Monitoring continues while Unloop is in the background. An ongoing notification shows the session status and provides pause or resume and stop controls. Monitoring stops when the session ends or the user stops it.

**Why must it start immediately? What happens if it is deferred?**

> Blocking must begin when the user starts a focus session or when a routine reaches the start time the user selected. If the task is deferred, selected distracting apps remain accessible after the session should have started, so Unloop cannot enforce the user's chosen focus period.

**What happens if it is interrupted?**

> If monitoring is interrupted during an active session, Unloop cannot detect a selected app opening or display its blocking screen. The user can access distracting apps during a period they chose to block. Continuous monitoring is needed for the duration of the session; periodic background work cannot detect each app switch in time.

**How is it initiated and controlled by the user?**

> The user chooses the blocked apps and starts a session in Unloop, or explicitly creates and enables a scheduled routine. The user can pause or end the session in the app or through the ongoing notification.

**Demonstration video:** Supply your own unlisted YouTube URL or another video URL that a reviewer can open without requesting access. Record these steps:

1. Open Unloop and choose an app to block.
2. Start a focus session.
3. Show the ongoing session notification and its controls.
4. Open the selected app and show Unloop's blocking screen.
5. Return to Unloop or use the notification to stop the session.
6. Open the selected app again to show that blocking has ended.

If you describe scheduled routines in the declaration, also show creating or enabling a routine and its session starting at the selected time.

## Exact alarms

The originally uploaded **version code 5** contains both `USE_EXACT_ALARM` and `SCHEDULE_EXACT_ALARM`. Do not claim it has no exact-alarm permissions.

The replacement **version 0.0.1, version code 6** removes `USE_EXACT_ALARM` and retains `SCHEDULE_EXACT_ALARM`. It asks the user for Alarms & reminders access so routines and sessions can start and end at their chosen times. Build it with:

```sh
VERSION_NAME=0.0.1 VERSION_CODE=6 ./gradlew bundleRelease
```

Before answering a `USE_EXACT_ALARM` declaration, upload the replacement AAB to the closed-testing draft and remove version code 5 from that draft. Check any other active or pending releases that still contain the old permission.

- If the question specifically asks whether the app uses **`USE_EXACT_ALARM`**, answer **No** for the replacement bundle. Do not select an alarm-clock or calendar eligibility category to bypass the declaration.
- If it asks whether the app uses **any exact alarm permissions**, answer **Yes**, because `SCHEDULE_EXACT_ALARM` remains. Select that permission if the form lists it.
- If asked for an explanation, paste:

> Unloop uses SCHEDULE_EXACT_ALARM, with the user's Alarms & reminders approval, to start scheduled focus routines and end focus sessions at the times the user selects. Precise timing keeps app blocking aligned with the user's chosen focus period. The app checks access before scheduling a session and reschedules enabled routines when access is granted. The replacement bundle does not request USE_EXACT_ALARM.

Google may still show a declaration requirement while an older artifact containing `USE_EXACT_ALARM` remains in scope. Check the affected version codes rather than answering No for an artifact that still requests it.

## Official references

- [Foreground service declarations](https://support.google.com/googleplay/android-developer/answer/13392821?hl=en)
- [Exact alarm permission policy](https://support.google.com/googleplay/android-developer/answer/16558241?hl=en)
- [Android exact alarm permission flow](https://developer.android.com/develop/background-work/services/alarms)
