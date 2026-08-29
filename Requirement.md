**1. Project Overview**

For the final project, each group designs and builds a complete, working Android application on a topic of your own choosing — no prior topic approval is required. The app should solve a realistic problem for a clearly identified set of users (for example, a personal expense tracker, a study-planning tool, a local-events finder, or a small-business ordering app). It must go beyond a single static screen: expect at least three to four meaningfully connected screens, persistent local data, and integration with an external data source or device capability such as a REST API, camera, location services, or notifications. You are free to use any stack covered in the course (Kotlin/Java with Android SDK, Jetpack Compose, or Flutter/React Native), provided the final product is a native-installable Android app.

**2. Group Formation**

Work in groups of **2 to 4 members**. Groups form themselves, and no advance registration of members or topic is needed. Divide the work clearly among all members and document that division in your report.

**3. Deliverables**

Submit a single compressed file named after the student IDs of all group members, separated by underscores and listed in ascending order — for example `2012345_2012350_2012361.zip`.

Inside the archive, organise your files exactly as follows:

```
2012345_2012350_2012361/
├── README.md              # Group members (ID + full name), project title,
│                          # demo video link, build instructions, test account
│                          # credentials if the app requires login
├── src/                   # Full application source code
│   └── ...                # (exclude build, .gradle, node_modules, .idea)
├── apk/
│   └── app-release.apk    # Installable build, Android API 24+
├── report/
│   └── report.pdf         # 10–30 pages
└── video/
    └── demo-link.txt      # The demo video link goes here
```

Each item is required:

- **Full source code** of the application (exclude build output directories such as `build`, `.gradle`, and `node_modules`). If you use Git, include the `.git` folder so commit history is visible; otherwise export the log to `src/git-log.txt`.
- **An installable Android build** (`.apk`, release or signed debug) that installs and runs on a physical device or emulator running Android API 24 or higher. *This is mandatory — a project without a working APK will not be graded.*
- **A report** (PDF, 10–30 pages) covering the project topic, target users, application architecture, technologies used, setup and installation instructions, the work-division table, and a self-assessment of what was completed.
- **A demo video (5–10 minutes)** that includes the presentation: introduce the topic, explain your technical approach, and demonstrate the main features live on a device. All members must speak, and the video should make clear who presents which part. Upload the video to Google Drive or YouTube (unlisted is fine) and put the link in `video/demo-link.txt` — there is no need to enter it in the form. **Make sure sharing is set so that anyone with the link can view it**; a link that cannot be opened counts as no video submitted.

**4. Deadline and Submission**

Deadline: **23h59 on 5/9/2026**.

Submit through the **Google Form** at: https://forms.gle/KdcR2BaASEGx2RCeA 

Submission instructions:

- **Only one representative per group** fills in the form; other members should not submit again, to avoid duplicate entries.
- Upload the `.zip` file directly through the form, using the student-ID naming format and folder structure described above. If the file exceeds the size limit, upload it to Google Drive instead, paste the link into the form, and **set sharing so that anyone with the link can view it**.
- After submitting, you will receive a confirmation email from Google Forms — keep it as proof of submission. **No confirmation means your work has not been submitted.**

**5. Grading Criteria (out of 10)**

| No.  | Criterion                            | What is assessed                                             | Weight |
| ---- | ------------------------------------ | ------------------------------------------------------------ | ------ |
| 1    | Functional completeness              | The app installs and runs reliably on Android; the features described in the report are all implemented; no critical defects such as crashes, freezes, or data loss | 30%    |
| 2    | Technical quality & source code      | Sound architecture with clear separation of UI, logic, and data; correct application of course techniques (lifecycle handling, local storage, API calls, asynchronous work, runtime permissions); readable, well-organised code | 25%    |
| 3    | User interface & user experience     | Intuitive and consistent UI; sensible navigation; adapts to different screen sizes; handles loading, empty, error, and offline states gracefully | 20%    |
| 4    | Originality & complexity             | A practical idea with a creative element; difficulty appropriate to the group size; not a copy of an existing template or tutorial project | 15%    |
| 5    | Report, presentation & collaboration | Complete, well-structured report; clear demo video and coherent presentation; every member has contributed | 10%    |



### Submission status

| Submission status | This assignment does not require you to submit anything online |
| ----------------- | ------------------------------------------------------------ |
| Grading status    | Not graded                                                   |
| Time remaining    | 7 days 3 hours remaining                                     |