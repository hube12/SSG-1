package ssg;

import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcreversal.ChunkRandomReverser;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StructureSeedGenerator {

    public static void generate(int eyeCount,FileWriter writer) throws IOException {
        System.out.println("Looking for population seeds...");
        List<Long> populationSeeds = EyeSeeds.getPopulationSeeds(eyeCount).collect(Collectors.toList());
        System.out.println("Finished search with " + populationSeeds.size() + " seeds.");
        System.out.println("Creating chunk starts...");
        Set<CPos> starts = new HashSet<>();
        for(CPos ringStart: StrongholdGen.getAllStarts(0, StrongholdGen.DEFAULT_CONFIG)) {
            for(int xo = 0; xo < 53; xo++) {
                for(int zo = 0; zo < 53; zo++) {
                    starts.add(new CPos(ringStart.getX() - 26 + xo, ringStart.getZ() - 26 + zo));
                }
            }
        }
        System.out.println("Finished with " + starts.size() + " starts.");
        System.out.println("Starting search...");
        long count = 0;
        int i = 0;
        for(CPos start: starts) {
            for(long populationSeed: populationSeeds) {
                for(long structureSeed: ChunkRandomReverser.reversePopulationSeed(populationSeed,
                        start.getX() << 4, start.getZ() << 4, MCVersion.v1_16_1)) {
                    writer.write(structureSeed + " " + start.getX() + " " + start.getZ() + "\n");
                    count++;
                }
            }
            i++;
            if(i % 64 == 0) {
                System.out.println(((float)(i) / starts.size()) * 100.0D + "% complete at " + System.currentTimeMillis());
            }
            writer.flush();
        }
        System.out.println("Finished search with " + count + " seeds.");
    }
}
