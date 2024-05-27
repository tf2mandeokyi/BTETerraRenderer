package com.mndk.bteterrarenderer.draco_deprecated.rans;

/** Entropy coder from the ANS(Asymmetric numeral systems) family */
public class RAnsDecoder {
//    public static final int RABS_ANS_P8_PRECISION = 256;
//    public static final int rabs_ans_p10_precision = 1024;
//    public static final int RABS_L_BASE = 4096;
//    public static final int IO_BASE = 256;
//    public static final int L_RANS_BASE = 4096;
//    public static final int TAGGED_RANS_BASE = 16384;
//    public static final int TAGGED_RANS_PRECISION = 4096;
//
//    private static final int TAGGED_SYMBOLS = 0;
//    private static final int RAW_SYMBOLS = 1;
//
//    private final ByteBuf buf;
//    private int offset, state;
//
//    /**
//     * @implNote Draco 23.6: <a href="https://google.github.io/draco/spec/#ransinitdecoder">RansInitDecoder</a>
//     */
//    public RAnsDecoder(ByteBuf buf, int offset, int base) {
//        this.buf = buf;
//
//        int state;
//        int x = buf.getUnsignedByte(offset - 1) >> 6;
//        if(x == 0) {
//            this.offset = offset - 1;
//            state = buf.getByte(offset - 1) & 0x3F;
//        }
//        else if(x == 1) {
//            this.offset = offset - 2;
//            state = buf.getUnsignedShortLE(offset - 2) & 0x3FFF;
//        }
//        else if(x == 2) {
//            this.offset = offset - 3;
//
//            int     state24 = buf.getUnsignedByte(offset - 3);
//            state24 = state24 & buf.getUnsignedByte(offset - 2) << 8;
//            state24 = state24 & buf.getUnsignedByte(offset - 1) << 16;
//            state = state24 & 0x3FFFFF;
//        }
//        else if(x == 3) {
//            this.offset = offset - 4;
//            state = (int) (buf.getUnsignedInt(offset - 4) & 0x3FFFFFFF);
//        }
//        else {
//            throw new RuntimeException("this can't be happening!!!!!");
//        }
//        this.state = state + base;
//    }
//
//    /**
//     * @implNote Draco 23.7: <a href="https://google.github.io/draco/spec/#ransread">RansRead</a>
//     */
//    public int read(int base, int precision, SymbolTables symbolTables) {
//        while(this.state < base && this.offset > 0) {
//            this.state = this.state * IO_BASE + this.buf.getUnsignedByte(--this.offset);
//        }
//        int quotient = this.state / precision;
//        int remainder = this.state % precision;
//        ProbabilitySymbol symbol = symbolTables.fetchSymbol(remainder);
//        Probability probability = symbol.getProbability();
//        this.state = quotient * probability.getValue() + remainder - probability.getCumulative();
//        return symbol.getValue();
//    }
//
//    /**
//     * @implNote Draco 23.9: <a href="https://google.github.io/draco/spec/#rabsdescread">RabsDescRead</a>
//     */
//    public boolean rabsDescriptionRead(int p0) {
//        int p = RABS_ANS_P8_PRECISION - p0;
//        if(this.state < RABS_L_BASE) {
//            this.state = this.state * IO_BASE + this.buf.getUnsignedByte(--this.offset);
//        }
//        int x = this.state;
//        int quotient = x / RABS_ANS_P8_PRECISION;
//        int remainder = x % RABS_ANS_P8_PRECISION;
//        int xn = quotient * p;
//        boolean result = remainder < p;
//        if(result) {
//            this.state = xn + remainder;
//        }
//        else {
//            this.state = x - xn - p;
//        }
//        return result;
//    }
//
//    /**
//     * @implNote Draco 23.1: <a href="https://google.github.io/draco/spec/#decodesymbols">
//     *     DecodeSymbols</a>
//     */
//    public static void decodeSymbols(ByteBuf buf, int symbolCount, int componentCount, List<Integer> out) {
//        int scheme = buf.readUnsignedByte();
//        if(scheme == TAGGED_SYMBOLS) {
//            decodeTaggedSymbols(symbolCount, componentCount, buf, out);
//        }
//        else if(scheme == RAW_SYMBOLS) {
//            decodeRawSymbols(symbolCount, buf, out);
//        }
//    }
//
//    /**
//     * @implNote Draco 23.2: <a href="https://google.github.io/draco/spec/#decodetaggedsymbols">
//     *     DecodeTaggedSymbols</a>
//     */
//    private static void decodeTaggedSymbols(int valueCount, int componentCount, ByteBuf buf, List<Integer> out) {
//        int symbolCount = (int) BitUtils.readBase128LE(buf); // num_symbols_
//        SymbolTables tables = SymbolTables.build(symbolCount, buf);
//
//        int size = (int) BitUtils.readBase128LE(buf);
//        RAnsDecoder ansDecoder = new RAnsDecoder(buf.readBytes(size), size, TAGGED_RANS_BASE);
//
//        BitStreamBuf bitBuf = new BitStreamBuf(buf);
//        for(int i = 0; i < valueCount; i += componentCount) {
//            int valueSize = ansDecoder.read(TAGGED_RANS_BASE, TAGGED_RANS_PRECISION, tables);
//            int value = bitBuf.readBits(valueSize);
//            out.add(value);
//        }
//        bitBuf.resetBitReader();
//    }
//
//    /**
//     * @implNote Draco 23.3: <a href="https://google.github.io/draco/spec/#decoderawsymbols">
//     *     DecodeRawSymbols</a>
//     */
//    private static void decodeRawSymbols(int valueCount, ByteBuf buf, List<Integer> out) {
//        int maxBitLength = buf.readUnsignedByte();
//        int symbolCount = (int) BitUtils.readBase128LE(buf);
//        int ransPrecisionBits = (3 * maxBitLength) / 2;
//        if(ransPrecisionBits > 20) {
//            ransPrecisionBits = 20;
//        }
//        if(ransPrecisionBits < 12) {
//            ransPrecisionBits = 12;
//        }
//        int ransPrecision = 1 << ransPrecisionBits;
//        int ransBase = ransPrecision * 4;
//
//        SymbolTables table = SymbolTables.build(symbolCount, buf);
//        int size = (int) BitUtils.readBase128LE(buf);
//        RAnsDecoder ansDecoder = new RAnsDecoder(buf.readBytes(size), size, ransBase);
//        for(int i = 0; i < valueCount; i++) {
//            int value = ansDecoder.read(ransBase, ransPrecision, table);
//            out.add(value);
//        }
//    }
}
