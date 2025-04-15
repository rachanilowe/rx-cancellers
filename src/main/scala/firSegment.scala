package cancellers

import chisel3._
import chisel3.util._

class FIRSegment(val segmentSize: Int) extends Module {
  val io = IO(new Bundle {
    val inputs       = Input(Vec(segmentSize, SInt(3.W)))
    // val weights   = Input(Vec(segmentSize, SInt(8.W))) // do weight calc in here
    val weightCalcIns = Input(Vec(segmentSize, SInt(3.W))) // the delay of inputs for weight calculation
    val dout      = Output(SInt(24.W))
    val partialSum = Input(SInt(24.W))
    val error = Input(SInt(24.W))
    val valid = Input(Bool())

    // For debugging
    val weightPeek = Output(Vec(segmentSize, SInt(8.W)))
  })

  val weights = RegInit(VecInit(Seq.fill(segmentSize)(0.S(8.W))))

  // Update weights using LMS: w_i(n+1) = w_i(n) + mu * e(n) * x(n-i+1)
  // Tap-leakage update : w_i(n+1) = (1-alpha*mu)w_i(n) - alpha * e(n) * x(n)
  when (io.valid) {
    for (i <- 0 until segmentSize) {
      val deltaW = (io.weightCalcIns(i) * io.error)  // TODO: switch to shift later
      weights(i) := weights(i) - deltaW
    }
  }

  val sum = weights.zip(io.inputs).map { case (w, d) => w * d }.reduce(_ + _)

  io.dout := sum + io.partialSum

  io.weightPeek := weights
}