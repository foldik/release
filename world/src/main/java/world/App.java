package world;

import java.util.*;
import model.World;

public class App {

    public static void main(String[] args) throws Exception {
      while(true) {
        Thread.sleep(2000);
        World world = new World(UUID.randomUUID().toString(), 12);
        System.out.println("Hello " + world.getName());
      }
    }
}
