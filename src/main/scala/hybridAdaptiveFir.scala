package cancellers

import chisel3._
import chisel3.util._

class HybridAdaptiveFIRFilter(val tapCount: Int, val segmentSize: Int, val gammaFactor: Int, val muFactor: Int) extends Module {
  require(tapCount % segmentSize == 0, "tapCount must be divisible by numSegments")
  // we can prob just have it be 4 bc 60 and 80 are both divisible by 4
  val io = IO(new Bundle {
    val din          = Input(SInt(3.W))
    val dinValid     = Input(Bool())
    // val dout         = Output(SInt(14.W))
    val dout         = Output(SInt(14.W))
    val desired      = Input(SInt(8.W))
    // val error        = Input(SInt(14.W))
    val error        = Input(SInt(8.W))

    // // For debugging
    // val weightPeek   = Output(Vec(segmentSize, SInt(16.W)))
    // val errors       = Output(Vec(tapCount/segmentSize - 1, SInt(20.W)))
    // val inputWeightShifters = Output(Vec(((tapCount/segmentSize * (segmentSize - 1)) + tapCount/segmentSize), SInt(7.W)))
  })

  // make sections divided with output pipeline registers where the 
  // partition count tells us how many taps we have per group
  val numGroups = tapCount / segmentSize
  val numInputReg = (numGroups * (segmentSize - 1)) + 1

  val inputShifters = RegInit(VecInit(Seq.fill(numInputReg)(0.S(3.W))))
  // val outputShifters = RegInit(VecInit(Seq.fill(numGroups - 1)(0.S(14.W))))
  val outputShifters = RegInit(VecInit(Seq.fill(numGroups - 1)(0.S(14.W))))

  // Delay line for weight calculation for the input 
  val numInputTrackingRegs = ((numGroups * (segmentSize - 1)) + numGroups)
  val inputWeightShifters = RegInit(VecInit(Seq.fill(numInputTrackingRegs)(0.S(3.W))))

  // The last FIRSegment should be directly connected to (desired - dout) * mu
  // val errorShifters = RegInit(VecInit(Seq.fill(numGroups - 1)(0.S(14.W))))
  val errorShifters = RegInit(VecInit(Seq.fill(numGroups - 1)(0.S(8.W))))
  val desiredDelayed = RegInit(0.S(8.W))

  // each group is a fir filter so we can use a simple fir module
  val segments = Seq.fill(numGroups)(Module(new FIRSegment(segmentSize, gammaFactor, muFactor)))

  for ((seg, idx) <- segments.zipWithIndex) {
    seg.io.inputs := VecInit(Seq.fill(segmentSize)(0.S(3.W)))
    seg.io.weightCalcIns := VecInit(Seq.fill(segmentSize)(0.S(3.W)))
    // seg.io.partialSum := 0.S(13.W)
    // seg.io.error := 0.S(13.W)
    seg.io.partialSum := 0.S(14.W)
    seg.io.error := 0.S(8.W)
    seg.io.valid := false.B 

    // Connect output even if unused
    val dout = seg.io.dout
  }

  // the bottom x(n) shift registers in the diagram
  when(io.dinValid) {
    for (i <- numInputTrackingRegs - 1 to 1 by -1) {
      inputWeightShifters(i) := inputWeightShifters(i - 1)
    }
    inputWeightShifters(0) := io.din
  }

  when(io.dinValid) {
    for (i <- numInputReg - 1 to 1 by -1) {
      inputShifters(i) := inputShifters(i - 1)
    }
    inputShifters(0) := io.din
  }

  // connect segments
  // for each segment we need to input the inputs, delayed inputs, partial sum, and error
  when(io.dinValid) {
    for (i <- 0 until (numGroups-1)) {
      segments(i).io.partialSum := outputShifters(i) // puts the patial sum of output reg i into seg i to add
      outputShifters(i) := segments(i+1).io.dout // puts the output of seg i+1 into output reg i
    }
  }

  val firOutput = (segments(0).io.dout)

  when(io.dinValid) {
    for (i <- 0 until numGroups) {
      val sliceInputShifters = inputShifters.slice((i * (segmentSize - 1)), ((i+1) * (segmentSize - 1) + 1))
      segments(i).io.inputs := VecInit(sliceInputShifters)

      // set input delay vector for weight calc
      val sliceInputWeightShifters = inputWeightShifters.slice((i * (segmentSize - 1)) + numGroups - 1, ((i+1) * (segmentSize - 1) + numGroups))
      segments(i).io.weightCalcIns := VecInit(sliceInputWeightShifters)

      // set error
      // This is when we are trying to feed error * mu directly into the final FIRSegment
      if (i == numGroups - 1) {
        segments(i).io.error := io.error
      } else {
        segments(i).io.error := errorShifters(numGroups - i - 2)
      }

      // set valid 
      segments(i).io.valid := io.dinValid
    }
  }

  when (io.dinValid) {
    // Shift the weight adjustment
    errorShifters(0) := io.error
    for (i <- 1 until numGroups - 1) {
      errorShifters(i) := errorShifters(i - 1)
    }
  }

  io.dout := firOutput

  // Debugging
  // io.errors := errorShifters
  // io.inputWeightShifters := inputWeightShifters
  // io.weightPeek := segments(0).io.weightPeek
}
