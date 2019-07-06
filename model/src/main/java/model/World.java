package model;

import java.util.Objects;

public class World {

  private final String name;
  private final int age;

  public World(final String name, final int age) {
    this.name = Objects.requireNonNull(name);
    if (age < 0) {
      throw new IllegalArgumentException("Age must be greater or equal than zero");  
    }
    this.age = age;
  }

  public String getName() {
    return name;
  }

  public int getAge() {
    return age;
  }

  @Override
  public String toString() {
    return "World { name = " + name + ", age = " + age + " }";
  }
}
