package world;

import java.util.*;
import model.World;

public class App {

    public static void main(String[] args) {
      World world = new World(UUID.randomUUID().toString(), 12);
      System.out.println("Hello " + world.getName());
    }
}
