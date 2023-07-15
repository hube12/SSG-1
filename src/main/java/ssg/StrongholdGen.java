package ssg;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.block.BlockBox;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.structure.Stronghold;
import com.seedfinding.mcfeature.structure.generator.piece.stronghold.PortalRoom;
import com.seedfinding.mcfeature.structure.generator.structure.StrongholdGenerator;
import com.seedfinding.mcseed.rand.JRand;

import java.util.*;

public class StrongholdGen {
    private static final Map<Integer, Set<CPos>> RING_TO_CHUNKS = new HashMap<>();
    public static final Config DEFAULT_CONFIG = new Config(32, 128, 3);

    public static CPos getFirstStart(long worldSeed, JRand rand) {
        rand.setSeed(worldSeed);
        double randomRadian = rand.nextDouble() * Math.PI * 2.0D;
        double multiplier = 128.0D + (rand.nextDouble() - 0.5D) * 80.0D;
        int x = (int)Math.round(Math.cos(randomRadian) * multiplier);
        int z = (int)Math.round(Math.sin(randomRadian) * multiplier);
        return new CPos(x, z);
    }

    public static Set<CPos> getAllStarts(int ring, Config config) {
        Set<CPos> chunks = RING_TO_CHUNKS.get(ring);
        if(chunks != null) {
            return chunks;
        }
        chunks = new HashSet<>();
        double base = config.distance * (4 + ring * 6);
        double min = base + (0.0D - 0.5D) * (double)config.distance * 2.5D;
        double max = base + (1.0D - 0.5D) * (double)config.distance * 2.5D;
        for(double d = 0.0F; d < Math.PI * 2.0D; d += 0.0006D) {
            for(double d2 = min; d2 < max; d2 += 0.05D) {
                int x = (int)Math.round(Math.cos(d) * d2);
                int z = (int)Math.round(Math.sin(d) * d2);
                chunks.add(new CPos(x, z));
            }
        }
        chunks = Collections.unmodifiableSet(chunks);
        RING_TO_CHUNKS.put(ring, chunks);
        return chunks;
    }

    private static Collection<CPos> getGoodStarts(long structureSeed, CPos eyeChunk, CPos startChunk, MCVersion version) {
        Collection<CPos> goodStarts = new HashSet<>();
        for (int ox = -13; ox <= 13; ox++) {
            for (int oz = -13; oz <= 13; oz++) {
                StrongholdGenerator generator = new StrongholdGenerator(version);
                CPos testStart = new CPos(startChunk.getX() + ox, startChunk.getZ() + oz);
                generator.populateStructure(structureSeed, testStart.getX(), testStart.getZ(), new ChunkRand(), piece -> {
                    if (!(piece instanceof PortalRoom)) return true;
                    BlockBox chunkBB = new BlockBox(eyeChunk.getX() << 4, 0, eyeChunk.getZ() << 4, (eyeChunk.getX() << 4) + 15, 255, (eyeChunk.getZ() << 4) + 15);
                    BlockBox portalBB = PortalFrame.getPortalBB((PortalRoom) piece);
                    if (!portalBB.intersects(chunkBB)) return false;
                    for (Stronghold.Piece piece1 : generator.pieceList) {
                        if (piece1 == piece) continue;
                        if (piece1.getBoundingBox().intersects(chunkBB)) return false;
                    }
                    goodStarts.add(testStart);
                    return false;
                }, true);
            }
        }
        return goodStarts;
    }

    public static class Config {
        public final int distance;
        public final int count;
        public final int spread;

        public Config(int distance, int count, int spread) {
            this.distance = distance;
            this.count = count;
            this.spread = spread;
        }
    }
}
