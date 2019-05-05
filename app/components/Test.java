package components;

import com.google.inject.ImplementedBy;

@ImplementedBy(EnglishHello.class)
public interface Test {
    public String sayHello();
}
