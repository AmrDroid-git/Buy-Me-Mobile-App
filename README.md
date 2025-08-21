BuyMe (Shopping List) — README.txt
Author: AmrDroid

========================================
ENGLISH
========================================

## Overview

BuyMe is a tiny, fast, Arabic-first shopping list app built with Jetpack Compose + Room.
Create categories (e.g., Fruits, Vegetables, Dairy) and add items under each. Check items, share the checked list, uncheck all, and import/export your data as JSON via the Storage Access Framework.

## Key Features

- Category titles in **red**; add/delete categories
- Per-category inline **Add (+)** with auto-focus + keyboard
  - If the add field loses focus while empty, it auto-closes
- Items with **checkbox on the left, text on the right**
- **Uncheck All** (global) with confirmation dialog
- **Share** checked items via any app (WhatsApp, Messenger, SMS, etc.)
- **Import/Export** full data set as **JSON**
- Full **RTL** layout (Arabic UI)

## Tech Stack

- Kotlin, **Jetpack Compose** (Material 3)
- **Room** + room-ktx, Coroutines/Flow
- **Gson** for JSON import/export
- **Storage Access Framework** (OpenDocument / CreateDocument)

## Project Structure (key files)

app/
src/main/
AndroidManifest.xml
java/com/example/buyme/
Graph.kt (Room DB provider)
data/
AppDatabase.kt (Room database)
Entities.kt (CategoryEntity, ItemEntity)
Dao.kt (CategoryDao, ItemDao)
ui/
MainActivity.kt
screens/
CategoryOverviewScreen.kt (main screen: import/export/share/uncheck all)
res/
values/strings.xml (Arabic strings)
mipmap-anydpi-v26/ic_launcher.xml (adaptive app icon)

## JSON Format (Import/Export)

UTF-8 JSON file with this shape:

{
"version": 1,
"categories": [
{
"name": "Fruits",
"items": [
{"name": "Apple", "checked": true},
{"name": "Strawberry", "checked": false}
]
}
]
}

- Export creates a file like: buyme_export.json
- Import **replaces** all existing data with the file contents

## Getting Started

1. Requirements

   - Android Studio Ladybug (or newer)
   - Android Gradle Plugin 8.x
   - **JDK 17**

2. Clone & open
   git clone https://github.com/<your-user>/buyme.git
   Open in Android Studio and run **Gradle Sync**.

3. Run
   Choose a device/emulator, then **Run**.

## Gradle Dependencies (important)

In app/build.gradle.kts:

dependencies {
implementation("androidx.room:room-runtime:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")

    implementation("androidx.compose.ui:ui:<latest>")
    implementation("androidx.compose.material3:material3:<latest>")
    implementation("androidx.activity:activity-compose:<latest>")

    implementation("com.google.code.gson:gson:2.10.1")

}

Note (KSP option):

- If you prefer KSP, replace kapt with:
  ksp("androidx.room:room-compiler:2.6.1")
  and remove kapt.

## Build APK / AAB

- Debug APK:
  app/build/outputs/apk/debug/app-debug.apk

- Signed Release:
  Android Studio → Build → Generate Signed Bundle / APK…
  Choose APK or Android App Bundle, pick/create keystore, finish.
  Outputs under:
  app/build/outputs/(apk|bundle)/release/…

## App Icon

Use **Image Asset** to create an adaptive icon:
Android Studio → File → New → Image Asset → Launcher Icons (Adaptive and Legacy)
Resources are generated in mipmap-\* and referenced by ic_launcher.xml.

## How to Use (in app)

- Add category: type name → **إضافة**
- Add item: press **+** on a category → type → **إضافة** (or keyboard Done)
- Uncheck all: header → **إلغاء تحديد الكل** → confirm
- Share: header → share icon → choose the target app
- Export: header → **تصدير** → choose destination (JSON)
- Import: header → **استيراد** → pick JSON file (replaces current data)

## Troubleshooting

- JVM target mismatch (1.8 vs 17):
  Set both Java & Kotlin to 17 (toolchains):
  kotlin { jvmToolchain(17) }
  compileOptions {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
  }

- Room/KAPT errors or “suspend inside transaction”:
  Use **room-ktx** and **withTransaction { }** for suspend DAO calls (already used).

- Room verifier / SQLite issues on Windows:
  Use Room 2.6.1 + JDK 17, then **Build → Clean Project** and **Rebuild**.

## Roadmap

- Merge-on-import (instead of replace)
- Multilingual (values-en/strings.xml)
- Drag & drop reorder for categories/items
- Custom dark theme colors

## Contributing

PRs are welcome. For larger changes, open an issue first.

## License

MIT © AmrDroid

========================================
العَرَبِيَّة
========================================

## نظرة عامة

BuyMe تطبيق خفيف وسريع لإدارة قوائم التسوق بواجهة عربية واتجاه RTL مبني بواسطة Jetpack Compose + Room.
أنشئ فئات (مثل: فواكه، خضار، ألبان) وأضف عناصر لكل فئة. يمكنك تحديد العناصر، مشاركة العناصر المحددة، إلغاء تحديد الكل، وأيضًا استيراد/تصدير البيانات كملف JSON عبر Storage Access Framework.

## المزايا

- عناوين الفئات باللون **الأحمر** مع إمكانية الإضافة/الحذف
- إضافة عنصر داخل الفئة بزر **(+)** مع فتح لوحة المفاتيح تلقائيًا
  - إذا فقد حقل الإضافة التركيز وهو فارغ يغلق تلقائيًا
- العناصر بمربع تحديد يسار والنص يمين
- **إلغاء تحديد الكل** مع نافذة تأكيد
- **مشاركة** العناصر المحددة عبر أي تطبيق (واتساب، ماسنجر، رسائل…)
- **استيراد/تصدير** كامل البيانات بصيغة **JSON**
- واجهة عربية كاملة واتجاه من اليمين لليسار

## التقنيات

- Kotlin، **Jetpack Compose** (Material 3)
- **Room** + room-ktx، وCoroutines/Flow
- **Gson** لعمليات الاستيراد/التصدير JSON
- **Storage Access Framework** (فتح/إنشاء مستند)

## هيكل المشروع (أهم الملفات)

app/
src/main/
AndroidManifest.xml
java/com/example/buyme/
Graph.kt (موفر قاعدة البيانات)
data/
AppDatabase.kt (قاعدة بيانات Room)
Entities.kt (CategoryEntity, ItemEntity)
Dao.kt (CategoryDao, ItemDao)
ui/
MainActivity.kt
screens/
CategoryOverviewScreen.kt (الشاشة الرئيسية: استيراد/تصدير/مشاركة/إلغاء تحديد الكل)
res/
values/strings.xml (سلاسل عربية)
mipmap-anydpi-v26/ic_launcher.xml (أيقونة تكيفية)

## صيغة JSON (الاستيراد/التصدير)

ملف UTF-8 بالشكل التالي:

{
"version": 1,
"categories": [
{
"name": "فواكه",
"items": [
{"name": "تفاح", "checked": true},
{"name": "فراولة", "checked": false}
]
}
]
}

- التصدير ينشئ ملفًا مثل: buyme_export.json
- الاستيراد **يستبدل** جميع البيانات الحالية بمحتوى الملف

## البدء السريع

1. المتطلبات

   - Android Studio Ladybug أو أحدث
   - Android Gradle Plugin 8.x
   - **JDK 17**

2. الاستنساخ والفتح
   git clone https://github.com/<your-user>/buyme.git
   افتح المشروع في Android Studio ثم نفّذ **Gradle Sync**.

3. التشغيل
   اختر جهازًا/محاكيًا ثم **Run**.

## اعتماديات Gradle المهمة

داخل app/build.gradle.kts:

dependencies {
implementation("androidx.room:room-runtime:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")

    implementation("androidx.compose.ui:ui:<latest>")
    implementation("androidx.compose.material3:material3:<latest>")
    implementation("androidx.activity:activity-compose:<latest>")

    implementation("com.google.code.gson:gson:2.10.1")

}

ملاحظة (استخدام KSP بدل KAPT):

- استبدل kapt بـ:
  ksp("androidx.room:room-compiler:2.6.1")
  واحذف kapt.

## بناء APK / AAB

- مسار الـ Debug APK:
  app/build/outputs/apk/debug/app-debug.apk

- إصدار موقّع (Release):
  Android Studio → Build → Generate Signed Bundle / APK…
  اختر APK أو Android App Bundle، ثم اختر/أنشئ keystore وأكمل.
  ستجد المخرجات في:
  app/build/outputs/(apk|bundle)/release/…

## أيقونة التطبيق

من Android Studio:
File → New → Image Asset → Launcher Icons (Adaptive and Legacy)
سيتم إنشاء الموارد تلقائيًا ضمن mipmap-\* وربطها في ic_launcher.xml.

## طريقة الاستخدام داخل التطبيق

- إضافة فئة: اكتب الاسم ثم اضغط **إضافة**.
- إضافة عنصر: اضغط **+** بجانب الفئة، اكتب الاسم ثم **إضافة** أو Done من لوحة المفاتيح.
- إلغاء تحديد الكل: من الترويسة اضغط **إلغاء تحديد الكل** ثم أكد العملية.
- مشاركة: من الترويسة اضغط **أيقونة المشاركة** ثم اختر التطبيق.
- تصدير: من الترويسة اضغط **تصدير** واختر مكان حفظ JSON.
- استيراد: من الترويسة اضغط **استيراد** واختر ملف JSON؛ سيتم **استبدال** البيانات الحالية.

## استكشاف الأخطاء وإصلاحها

- تعارض هدف JVM (1.8 مقابل 17):
  اضبط Java/Kotlin على 17:
  kotlin { jvmToolchain(17) }
  compileOptions {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
  }

- أخطاء Room/KAPT أو “suspend داخل معاملة”:
  استخدم **room-ktx** و **withTransaction { }** لاستدعاءات DAO المعلّقة (موجودة بالشفرة).

- مشاكل Room verifier/SQLite على Windows:
  استخدم Room 2.6.1 + JDK 17، ثم **Clean/Rebuild**:
  Build → Clean Project ثم Rebuild Project.

## خارطة الطريق

- الدمج أثناء الاستيراد بدل الاستبدال الكامل
- دعم لغات متعددة (values-en/strings.xml)
- سحب لإعادة ترتيب الفئات/العناصر
- ألوان نمط داكن مخصصة

## المساهمة

مرحبًا بالمساهمات. من الأفضل فتح Issue قبل التغييرات الكبيرة.

## الترخيص

MIT © AmrDroid
