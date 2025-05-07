package cancellers

import chisel3._
import chisel3.util._

class FIRSegment(val segmentSize: Int) extends Module {
  val io = IO(new Bundle {
    val inputs       = Input(Vec(segmentSize, SInt(3.W)))
    val weightCalcIns = Input(Vec(segmentSize, SInt(3.W))) // the delay of inputs for weight calculation
    // val dout      = Output(SInt(14.W))
    // val partialSum = Input(SInt(14.W))
    val dout      = Output(SInt(13.W))
    val partialSum = Input(SInt(13.W))
    val error = Input(SInt(8.W))
    val valid = Input(Bool())

    // For debugging
    val weightPeek = Output(Vec(segmentSize, SInt(10.W)))
  })

  val weights = RegInit(VecInit(Seq.fill(segmentSize)(0.S(8.W))))
  when (io.valid) {
    for (i <- 0 until segmentSize) {
      // Removed 1.max
      weights(i) := (((((1 << gammaFactor) - 1)).asSInt * weights(i)) >> gammaFactor) + (((io.weightCalcIns(i) * (io.error)) >> muFactor))
    }
  }

  // val sum = weights.zip(io.inputs).map { case (w, d) => w * d }.reduce(_ + _)
  // dontTouch(sum)

  // Moved sum to dout
  io.dout := weights.zip(io.inputs).map { case (w, d) => w * d }.reduce(_ + _) + io.partialSum

  io.weightPeek := weights
}