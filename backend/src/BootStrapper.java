// Needed because the .jar complains unless we have a Java-main-method, for some strange reason it doesn't accept Kotlin
public class BootStrapper {
    public static void main(String[] args) {
        HueIT_backendKt.startServer();
    }
}
