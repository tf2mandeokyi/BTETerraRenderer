package com.mndk.bteterrarenderer.draco_deprecated.symbol;

import io.netty.buffer.ByteBuf;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SymbolTables {
    private final Probability[] probabilityTable;
    private final int[] lutTable;

    /**
     * @implNote Draco 23.4: <a href="https://google.github.io/draco/spec/#buildsymboltables">
     *     BuildSymbolTables</a>
     */
    public static SymbolTables build(int symbolCount, ByteBuf buf) {
        int[] tokenProbabilities = new int[symbolCount];

        for(int i = 0; i < symbolCount; i++) {
            // Decode the first byte and extract the number of extra bytes we need to
            // get, or the offset to the next symbol with non-zero probability.
            int probabilityData = buf.readUnsignedByte();
            int token = probabilityData & 3;
            if(token == 3) {
                int offset = probabilityData >> 2;
                for(int j = 0; j < offset + 1; j++) {
                    tokenProbabilities[i + j] = 0;
                }
                i += offset;
            }
            else {
                int probability = probabilityData >> 2;
                for(int j = 0; j < token; j++) {
                    int eb = buf.readUnsignedByte();
                    probability = probability | (eb << (8 * (j + 1) - 2));
                }
                tokenProbabilities[i] = probability;
            }
        }

        return buildLookupTable(tokenProbabilities);
    }

    /**
     * @implNote Draco 23.5: <a href="https://google.github.io/draco/spec/#rans_build_look_up_table">
     *     rans_build_look_up_table</a>
     */
    private static SymbolTables buildLookupTable(int[] tokenProbabilities) {
        List<Integer> lutList = new ArrayList<>();
        Probability[] probabilityTable = new Probability[tokenProbabilities.length];

        int cumulativeProbability = 0;
        int activeProbability = 0;
        for(int i = 0; i < tokenProbabilities.length; i++) {
            probabilityTable[i] = new Probability(tokenProbabilities[i], cumulativeProbability);

            cumulativeProbability += tokenProbabilities[i];
            for(int j = activeProbability; j < cumulativeProbability; j++) {
                lutList.add(i);
            }
            activeProbability = cumulativeProbability;
        }

        int[] lutTable = lutList.stream().mapToInt(Integer::intValue).toArray();
        return new SymbolTables(probabilityTable, lutTable);
    }

    /**
     * @implNote Draco 23.8: <a href="https://google.github.io/draco/spec/#fetch_sym">
     *     fetch_sym</a>
     */
    public ProbabilitySymbol fetchSymbol(int value) {
        int symbol = this.lutTable[value];
        return new ProbabilitySymbol(symbol, this.probabilityTable[symbol]);
    }
}
