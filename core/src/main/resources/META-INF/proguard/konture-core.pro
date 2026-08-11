# Preserve kotlinx.serialization generated serializers for Konture core models
-keepclassmembers class * implements kotlinx.serialization.KSerializer {
    public static *** INSTANCE;
}
-keepattributes *Annotation*,Signature,InnerClasses
