package dev.lemonclient.utils.notebot.instrumentdetect;

import net.minecraft.block.BlockState;
import net.minecraft.block.enums.Instrument;
import net.minecraft.util.math.BlockPos;

public interface InstrumentDetectFunction {
    /**
     * Detects an instrument for noteblock
     *
     * @param noteBlock Noteblock state
     * @param blockPos  Noteblock position
     * @return Detected instrument
     */
    Instrument detectInstrument(BlockState noteBlock, BlockPos blockPos);
}
