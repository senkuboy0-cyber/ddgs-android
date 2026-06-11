# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Jsoup
-keeppackagenames org.jsoup.nodes
