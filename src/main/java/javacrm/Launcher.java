package javacrm;

/**
 * Classpath entry point for the application.
 *
 * On Java 11 JavaFX is no longer bundled with the JDK and is provided by the
 * OpenJFX artifacts on the classpath. Launching a class that extends
 * {@link javafx.application.Application} directly from the classpath fails with
 * "JavaFX runtime components are missing". This thin launcher does not extend
 * {@code Application}, so it can be used as the {@code Main-Class} and delegates
 * to {@link JavaCRM#main(String[])}.
 */
public class Launcher {

    public static void main(String[] args) {
        JavaCRM.main(args);
    }
}
