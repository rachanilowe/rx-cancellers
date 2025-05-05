package cancellers

import chisel3._
import chisel3.util._

class FIRSegment(val segmentSize: Int, val gammaFactor: Int, val muFactor: Int) extends Module {
  val io = IO(new Bundle {
    val inputs       = Input(Vec(segmentSize, SInt(3.W)))
    val weightCalcIns = Input(Vec(segmentSize, SInt(3.W))) // the delay of inputs for weight calculation
    val dout      = Output(SInt(16.W))
    val partialSum = Input(SInt(16.W))
    val error = Input(SInt(10.W))
    val valid = Input(Bool())

    // For debugging
    val weightPeek = Output(Vec(segmentSize, SInt(16.W)))
  })

  val weights = RegInit(VecInit(Seq.fill(segmentSize)(0.S(16.W))))
  when (io.valid) {
    for (i <- 0 until segmentSize) {
      val deltaW = (io.weightCalcIns(i) * (io.error))
      val weightUpdate = ((1.max(((1 << gammaFactor) - 1)).asSInt * weights(i)) >> gammaFactor) + ((deltaW >> muFactor))
      weights(i) := weightUpdate
    }
  }

  val sum = weights.zip(io.inputs).map { case (w, d) => w * d }.reduce(_ + _)

  io.dout := sum + io.partialSum

  io.weightPeek := weights
}