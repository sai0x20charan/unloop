# Play Console declarations

These answers describe Unloop's focus-session and app-blocking functionality. Submit them under **Monitor and improve > App content**. The foreground service requires a reviewer-accessible demonstration video link before you can submit it.

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

**Demonstration video:**
The final video is generated and ready in the repository:
- **Video file:** [release/unloop-foreground-service-declaration.mp4](file:///Users/saicharan/Developer/AndroidProjects/unloop/release/unloop-foreground-service-declaration.mp4) (90 seconds, 1080p, 2.4 MB)
- **Visual contact sheet:** [release/unloop-foreground-service-contact-sheet.png](file:///Users/saicharan/Developer/AndroidProjects/unloop/release/unloop-foreground-service-contact-sheet.png)

### Video breakdown:
1. **0:00 – 0:03**: Title card showing app name (`Unloop`), permission (`FOREGROUND_SERVICE_SPECIAL_USE`), and FGS subtype (`Monitoring app usage for Unloop routines`).
2. **0:03 – 0:07**: Step 1: User selects session duration (15m preset).
3. **0:07 – 0:21**: Step 2: User selects distracting apps to block (Google Chrome).
4. **0:21 – 0:27**: Step 3: User taps "Start session" to immediately launch `RoutineForegroundService`.
5. **0:27 – 0:37**: Step 4: User inspects the persistent foreground notification in the shade, showing "Routine Active", live countdown timer, and Pause/Stop controls.
6. **0:37 – 0:48**: Step 5: User leaves Unloop and opens Google Chrome; foreground monitoring detects it and displays the full-screen blocking overlay.
7. **0:48 – 0:59**: Step 6: User pulls down notification shade and taps "Pause"; notification reflects paused status.
8. **0:59 – 1:08**: Step 7: User resumes session, then taps "Stop"; foreground service cleanly terminates, dismisses the notification, and restores full Chrome access.
9. **1:08 – 1:16**: Step 8: Scheduled routine demonstration; exact alarm triggers at 10:15 PM and foreground service launches automatically, blocking Chrome.
10. **1:16 – 1:25**: Step 9: User stops the scheduled routine directly from the persistent notification.
11. **1:25 – 1:30**: Policy summary outro card.

### How to submit the video:
1. Upload [release/unloop-foreground-service-declaration.mp4](file:///Users/saicharan/Developer/AndroidProjects/unloop/release/unloop-foreground-service-declaration.mp4) to **YouTube** as **Unlisted** (or **Google Drive** with permission set to "Anyone with the link can view").
2. Paste the shared link into the **Demonstration video** field in Google Play Console.

---

## Exact alarms

**Does your app use exact alarm permissions?** Yes.

**Permissions:** `USE_EXACT_ALARM` and `SCHEDULE_EXACT_ALARM`.

**Core functionality:** Focus timer with app blocking.

Google's written policy includes apps whose core functionality is an alarm or timer. The Console form reported here shows only Alarm clock and Calendar; this document does not establish how its Alarm clock option maps to timers. Describe the implemented focus timer accurately. Unloop does not provide calendar functionality.

**Describe the functionality:**
> Unloop's core functionality includes a focus timer. The user chooses a session duration and the apps to block, then starts the timer. The app blocks those selected apps during the focus period and ends blocking when the timer expires. The user can pause, resume or stop the timer. Users can also create and enable routines with chosen start and end times. Exact alarms keep these focus periods aligned with the user's selected times while Unloop is in the background or the device is idle.

**Why is precise timing needed?**
> If a focus timer's end alarm is delayed, apps can remain blocked after the duration the user selected. If a routine's start alarm is delayed, selected apps remain accessible after the chosen start time. Precise alarms keep the start and end of the user's focus period aligned with their timer or schedule.

`USE_EXACT_ALARM` is automatically granted on Android 13 and later, so the restored implementation has no added Alarms & reminders request screen. Google reviews whether the core timer functionality qualifies for this restricted permission. This declaration describes the implemented timer; it does not guarantee approval.

The existing closed-testing draft **version 0.0.1, version code 5** already uses these permissions and matches the restored implementation. The local **version code 6** bundles that remove `USE_EXACT_ALARM` are superseded and have not been uploaded to Play. Do not upload those replacement bundles.

## Official references

- [Foreground service declarations](https://support.google.com/googleplay/android-developer/answer/13392821?hl=en)
- [Exact alarm permission policy](https://support.google.com/googleplay/android-developer/answer/16558241?hl=en)
- [Android exact alarm permission flow](https://developer.android.com/develop/background-work/services/alarms)
