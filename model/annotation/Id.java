package model.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.FIELD;

// Practical 7: Marker annotation for account identifier
@Target(FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {
}