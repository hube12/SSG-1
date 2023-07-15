package ssg;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
//        Write10eyes();
//        Filter10eyes();
        Run();

    }

    public static void Run() {
        try {
            BufferedReader reader = Files.newBufferedReader(Path.of("10eyes_filtered.txt"));
            BufferedWriter fileWriter = Files.newBufferedWriter(Path.of("out.txt"));
            WorldSeedGenerator.generate(reader, fileWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void Filter10eyes() {
        try {
            BufferedReader reader = Files.newBufferedReader(Path.of("10eyes.txt"));
            BufferedWriter writer = Files.newBufferedWriter(Path.of("10eyes_filtered.txt"));
            StructureSeedStartFilter.filter(reader,writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static void Write10eyes() {
        try {
            FileWriter fileWriter = new FileWriter("10eyes.txt");
            StructureSeedGenerator.generate(10, fileWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
