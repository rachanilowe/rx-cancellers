package cancellers

import chisel3._
import chisel3.util._

class FIRSegment(val segmentSize: Int) extends Module {
  val io = IO(new Bundle {
    val inputs       = Input(Vec(segmentSize, SInt(6.W)))
    val weightCalcIns = Input(Vec(segmentSize, SInt(6.W))) // the delay of inputs for weight calculation
    val dout      = Output(SInt(20.W))
    val partialSum = Input(SInt(20.W))
    val error = Input(SInt(20.W))
    val valid = Input(Bool())

    // For debugging
    val weightPeek = Output(Vec(segmentSize, SInt(16.W)))
  })

  val weights = RegInit(VecInit(Seq.fill(segmentSize)(0.S(16.W))))

  // Update weights using LMS: w_i(n+1) = w_i(n) + mu * e(n) * x(n-i+1)
  // Tap-leakage update : w_i(n+1) = (1-alpha*mu)w_i(n) - alpha * e(n) * x(n)
  when (io.valid) {
    for (i <- 0 until segmentSize) {

      // Cap weight values at 4-bit maximums on positive and negative side
      val maxWeight = 511.S(10.W)  
      val minWeight = -512.S(10.W) 
      
      // TODO: implement tap-leakage algorithm
      val deltaW = (io.weightCalcIns(i) * (io.error))  // TODO: switch to shift later
      // val weightUpdate = ((weights(i))) - ((deltaW >> 7))
      val weightUpdate = ((255.S * weights(i)) >> 8) + ((deltaW >> 7))
      // val weightUpdate = ((5.S * weights(i)) >> 8) - ((3.S * deltaW) >> 4)
      // weights(i) := Mux(weightUpdate > maxWeight, maxWeight, Mux(weightUpdate < minWeight, minWeight, weightUpdate))
      weights(i) := weightUpdate
    }
  }

  val sum = weights.zip(io.inputs).map { case (w, d) => w * d }.reduce(_ + _)

  // Attempt to shrink output data
  io.dout := ((sum) + io.partialSum) >> 5

  io.weightPeek := weights
}