package ssg;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.block.BlockBox;
import com.seedfinding.mcfeature.structure.generator.piece.StructurePiece;
import com.seedfinding.mcfeature.structure.generator.structure.StrongholdGenerator;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class StrongholdGeneratorExtended {
    private final StrongholdGenerator strongholdGenerator;
    private final Set<BlockBox> pieceBoundingBoxes;

    public StrongholdGeneratorExtended(StrongholdGenerator strongholdGenerator) {
        this.strongholdGenerator = strongholdGenerator;
        this.pieceBoundingBoxes = new HashSet<>();
    }

    public void populateStructure(long structureSeed, int chunkX, int chunkZ, ChunkRand chunkRand, Function<StructurePiece, Boolean> pieceValidator, boolean shouldGenerateEndPortal) {
        strongholdGenerator.populateStructure(structureSeed, chunkX, chunkZ, chunkRand, piece -> {
            pieceBoundingBoxes.add(piece.getBoundingBox());
            return pieceValidator.apply(piece);
        }, shouldGenerateEndPortal);
    }

    public Set<BlockBox> getPieceBoundingBoxes() {
        return pieceBoundingBoxes;
    }
}