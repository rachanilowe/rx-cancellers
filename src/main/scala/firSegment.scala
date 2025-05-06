package cancellers

import chisel3._
import chisel3.util._

class FIRSegment(val segmentSize: Int, val gammaFactor: Int, val muFactor: Int) extends Module {
  val io = IO(new Bundle {
    val inputs       = Input(Vec(segmentSize, SInt(3.W)))
    val weightCalcIns = Input(Vec(segmentSize, SInt(3.W))) // the delay of inputs for weight calculation
    // val dout      = Output(SInt(14.W))
    // val partialSum = Input(SInt(14.W))
    val dout      = Output(SInt(14.W))
    val partialSum = Input(SInt(14.W))
    val error = Input(SInt(8.W))
    val valid = Input(Bool())

    // For debugging
    // val weightPeek = Output(Vec(segmentSize, SInt(16.W)))
  })

  val weights = RegInit(VecInit(Seq.fill(segmentSize)(0.S(7.W))))
  when (io.valid) {
    for (i <- 0 until segmentSize) {
      // val deltaW = (io.weightCalcIns(i) * (io.error))
      // val errorW = io.error.pad(14) 

      // val deltaW = MuxLookup(io.weightCalcIns(i).asUInt, 0.S, Seq(
      //   0.U -> 0.S,                                //  0
      //   1.U -> errorW,                            //  1
      //   2.U -> (errorW << 1),                   //  2
      //   3.U -> (errorW + (errorW << 1)),     //  3
      //   4.U -> -(errorW << 2),                 // -4
      //   5.U -> -(errorW + (errorW << 1)),   // -3
      //   6.U -> -(errorW << 1),               // -2
      //   7.U -> -errorW                       // -1
      // ))

      // Removed 1.max
      weights(i) := (((((1 << gammaFactor) - 1)).asSInt * weights(i)) >> gammaFactor) + (((io.weightCalcIns(i) * (io.error)) >> muFactor))
      // weights(i) := ((weights(i)) >> gammaFactor) + (((io.weightCalcIns(i) * (io.error)) >> muFactor))
      // weights(i) := ((weights(i)) >> gammaFactor) + ((deltaW) >> muFactor)
      // weights(i) := weightUpdate
    }
  }

  val sum = weights.zip(io.inputs).map { case (w, d) => w * d }.reduce(_ + _)

  io.dout := sum + io.partialSum

  // Debugging
  // io.weightPeek := weights
}