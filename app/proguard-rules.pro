# AdMob SDK Proguard rules
-keep public class com.google.android.gms.ads.** {
   public *;
}

# User Messaging Platform (UMP) Proguard rules
-keep public class com.google.android.ump.** {
   public *;
}

# Room & WorkManager reflection keep rules (used by Google Mobile Ads SDK)
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class androidx.work.** { *; }
-keep class androidx.room.** { *; }
