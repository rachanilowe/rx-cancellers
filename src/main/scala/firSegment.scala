package cancellers

import chisel3._
import chisel3.util._

class FIRSegment(val segmentSize: Int) extends Module {
  val io = IO(new Bundle {
    val inputs       = Input(Vec(segmentSize, SInt(7.W)))
    val weightCalcIns = Input(Vec(segmentSize, SInt(7.W))) // the delay of inputs for weight calculation
    val dout      = Output(SInt(10.W))
    val partialSum = Input(SInt(10.W))
    val error = Input(SInt(10.W))
    val valid = Input(Bool())

    // For debugging
    val weightPeek = Output(Vec(segmentSize, SInt(10.W)))
  })

  val weights = RegInit(VecInit(Seq.fill(segmentSize)(0.S(3.W))))

  // Update weights using LMS: w_i(n+1) = w_i(n) + mu * e(n) * x(n-i+1)
  // Tap-leakage update : w_i(n+1) = (1-alpha*mu)w_i(n) - alpha * e(n) * x(n)
  when (io.valid) {
    for (i <- 0 until segmentSize) {

      // Cap weight values at 4-bit maximums on positive and negative side
      val maxWeight = 3.S(3.W)  
      val minWeight = -4.S(3.W) 
      
      // TODO: implement tap-leakage algorithm
      val deltaW = (io.weightCalcIns(i) * (io.error))  // TODO: switch to shift later
      val weightUpdate = (weights(i)) - (deltaW >> 5)
      // val weightUpdate = ((127.S * weights(i)) >> 12) - ((63.S * deltaW) >> 6)
      weights(i) := Mux(weightUpdate > maxWeight, maxWeight, Mux(weightUpdate < minWeight, minWeight, weightUpdate))
      // weights(i) := weightUpdate
    }
  }

  val sum = weights.zip(io.inputs).map { case (w, d) => w * d }.reduce(_ + _)

  // Attempt to shrink output data
  io.dout := (sum + io.partialSum) >> 4

  io.weightPeek := weights
}