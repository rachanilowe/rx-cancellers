package cancellers

import chisel3._
import chisel3.util._

class AdaptiveFIRFilter(val tapCount: Int) extends Module {
  val io = IO(new Bundle {
    val din          = Input(SInt(3.W))
    val dinValid     = Input(Bool())
    val dout         = Output(SInt(18.W))
    val desired      = Input(SInt(18.W))
    val doutValid    = Output(Bool())
  })

  val shifters = RegInit(VecInit(Seq.fill(tapCount)(0.S(3.W))))

  val weights = VecInit(Seq.fill(tapCount)(0.S(8.W)))

  val firOutput = (weights.zip(shifters).map { case (c, d) => c * d }.reduceTree(_ + _) >> log2Ceil(tapCount))

  // only want to shift if dinValid == 1
  // Update delay line (Shift Register)
  // for (i <- tapCount - 1 to 1 by -1) {
  //   shifters(i) := shifters(i - 1)
  // }

  when(io.dinValid) {
  for (i <- (tapCount - 1) to 1 by -1) {
    shifters(i) := shifters(i - 1)
  }
  shifters(0) := io.din
  }

  val error = io.desired - firOutput

  // Update weights using LMS: w_i(n+1) = w_i(n) + mu * e(n) * x(n-i+1)
  // Tap-leakage update : w_i(n+1) = (1-alpha*mu)w_i(n) - alpha * e(n) * x(n)
  // Only update when we know the desired is Valid?
  when (dinValid) {
    for (i <- 0 until tapCount) {
      val deltaW = (shifters(i) << log2Ceil(error)) >> 5  // shifting for mu and error for efficiency
      weights(i) := weights(i) + deltaW
    }
  }
  io.doutValid := io.dinValid
  io.dataOut := firOutput
}
