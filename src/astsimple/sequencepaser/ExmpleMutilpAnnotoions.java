package astsimple.sequencepaser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface FirstAnnotation {
  String value();
}

// Define the second annotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface SecondAnnotation {
  int number();
}
public class ExmpleMutilpAnnotoions {
  // Apply both annotations to a method
  @FirstAnnotation(value = "Example of the first annotation")
  @SecondAnnotation(number = 42)
  public void annotatedMethod() {
    // Method implementation
  }
}
