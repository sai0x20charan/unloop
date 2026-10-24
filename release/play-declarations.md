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

**Does your app use exact alarm permissions?** Yes.

**Permissions:** `USE_EXACT_ALARM` and `SCHEDULE_EXACT_ALARM`.

**Core functionality:** Select Timer, or the combined alarm/timer option if the form uses that wording. Unloop's relevant functionality is its focus timer. Do not select Calendar.

**Describe the functionality:**

> Unloop's core functionality includes a focus timer. The user chooses a session duration and the apps to block, then starts the timer. The app blocks those selected apps during the focus period and ends blocking when the timer expires. The user can pause, resume or stop the timer. Users can also create and enable routines with chosen start and end times. Exact alarms keep these focus periods aligned with the user's selected times while Unloop is in the background or the device is idle.

**Why is precise timing needed?**

> If a focus timer's end alarm is delayed, apps can remain blocked after the duration the user selected. If a routine's start alarm is delayed, selected apps remain accessible after the chosen start time. Precise alarms keep the start and end of the user's focus period aligned with their timer or schedule.

`USE_EXACT_ALARM` is automatically granted on Android 13 and later, so the restored implementation has no added Alarms & reminders request screen. Google reviews whether the core timer functionality qualifies for this restricted permission. This declaration describes the implemented timer; it does not guarantee approval.

The existing closed-testing draft **version 0.0.1, version code 5** already uses these permissions and matches the restored implementation. You can keep that bundle and complete its declarations. The previously prepared **version code 6** with the mandatory permission-request flow was not uploaded to Play and is superseded. Do not upload that earlier replacement bundle.

## Official references

- [Foreground service declarations](https://support.google.com/googleplay/android-developer/answer/13392821?hl=en)
- [Exact alarm permission policy](https://support.google.com/googleplay/android-developer/answer/16558241?hl=en)
- [Android exact alarm permission flow](https://developer.android.com/develop/background-work/services/alarms)
