package ssg;

import com.seedfinding.latticg.util.LCG;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.block.BlockBox;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.structure.Stronghold;
import com.seedfinding.mcfeature.structure.generator.piece.stronghold.PortalRoom;
import com.seedfinding.mcfeature.structure.generator.structure.StrongholdGenerator;
import com.seedfinding.mcseed.rand.JRand;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WorldSeedGenerator {
    private static final LCG RING_SKIP = LCG.JAVA.combine(4);

    public static void generate(BufferedReader reader,BufferedWriter writer) throws IOException {
        List<String> eyes = reader.lines().toList();
        JRand rand = new JRand(0L);
        ChunkRand chunkRand = new ChunkRand();
        for (String s : eyes) {
            String[] line = s.trim().split(Pattern.quote(" "));
            long structureSeed = Long.parseLong(line[0]);
            CPos eyeChunk = new CPos(Integer.parseInt(line[1]), Integer.parseInt(line[2]));
            CPos startChunk = new CPos(Integer.parseInt(line[3]), Integer.parseInt(line[4]));
            long rngSeed = RING_SKIP.nextSeed(structureSeed ^ LCG.JAVA.multiplier);

            StrongholdGenerator generator = new StrongholdGenerator(MCVersion.v1_16_1);
            Collection<CPos> goodStarts = getGoodStarts(generator,chunkRand,structureSeed, eyeChunk, startChunk);
            System.out.println("DOWN");
            int lastZero = getLastZero(rand, rngSeed);
            int lastX = goodStarts.stream().mapToInt(CPos::getX).max().getAsInt();
            int lastZ = goodStarts.stream().mapToInt(CPos::getZ).max().getAsInt();
            for (long upperBits = 0; upperBits < 1L << 16; upperBits++) {
                long worldSeed = (upperBits << 48) | structureSeed;
                BiomeChecker source = new BiomeChecker(MCVersion.v1_16_1, worldSeed);
                rand.setSeed(rngSeed, false);
                CPos start = source.getStrongholdStart(
                        (startChunk.getX() << 4) + 8, (startChunk.getZ() << 4) + 8,
                        Stronghold.VALID_BIOMES_16, rand, lastZero, lastX, lastZ);
                if (start == null || !goodStarts.contains(start)) {
                    continue;
                }
                BPos p = getPortalCenter(structureSeed, start,chunkRand);
                String msg = "Seed:" + worldSeed + " " + p.getX() + " " + p.getZ();
                System.out.print(msg);
                try {
                    writer.write(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
            try {
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
        writer.close();
        System.out.println("Finish");
    }

    private static Collection<CPos> getGoodStarts(StrongholdGenerator Generator,ChunkRand rand,long structureSeed, CPos eyeChunk, CPos startChunk) {
        Collection<CPos> goodStarts = new HashSet<>();
        Set<BlockBox> pieceBoundingBoxes = new HashSet<>();
        StrongholdGeneratorExtended generator = new StrongholdGeneratorExtended(Generator);
        BlockBox chunkBB = new BlockBox(eyeChunk.getX() << 4, 0, eyeChunk.getZ() << 4, (eyeChunk.getX() << 4) + 15, 60, (eyeChunk.getZ() << 4) + 15);
        for (int ox = -13; ox <= 13; ox++) {
            for (int oz = -13; oz <= 13; oz++) {
                CPos testStart = new CPos(startChunk.getX() + ox, startChunk.getZ() + oz);
                generator.populateStructure(structureSeed, testStart.getX(), testStart.getZ(),rand, piece -> {
                    if (!(piece instanceof PortalRoom)) return true;
                    BlockBox portalBB = PortalFrame.getPortalBB((PortalRoom) piece);
                    if (!portalBB.intersects(chunkBB)) return false;
                    for (BlockBox pieceBB : pieceBoundingBoxes) {
                        if (pieceBB.intersects(chunkBB)) return false;
                    }
                    pieceBoundingBoxes.add(piece.getBoundingBox());
                    goodStarts.add(testStart);
                    return false;
                }, true);
            }
        }
        return goodStarts;
    }

    public static int getLastZero(JRand rand, long rngSeed) {
        rand.setSeed(rngSeed, false);
        int lastZero = 0;
        for (int i = 1; i < 3249; i++) {
            boolean b = rand.nextInt(i + 1) == 0;
            if (b) lastZero = i;
        }
        return lastZero;
    }

    private static BPos getPortalCenter(long structureSeed, CPos start, ChunkRand rand) {
        StrongholdGenerator strongholdGenerator = new StrongholdGenerator(MCVersion.v1_16_1);
        StrongholdGeneratorExtended generator = new StrongholdGeneratorExtended(strongholdGenerator);
        final BlockBox[] portalBB = new BlockBox[1];
        generator.populateStructure(structureSeed, start.getX(), start.getZ(), rand, piece -> {
            if (!(piece instanceof PortalRoom)) return true;
            portalBB[0] = PortalFrame.getPortalBB((PortalRoom) piece);
            return false;
        }, true);
        return new BPos(portalBB[0].minX + 1, 0, portalBB[0].minZ + 1);
    }
}