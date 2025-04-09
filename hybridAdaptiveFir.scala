import chisel3._
import chisel3.util._

class HybridAdaptiveFIRFilter(val tapCount: Int, val segmentSize: Int) extends Module {
  require(tapCount % segmentSize == 0, "tapCount must be divisible by numSegments")
  // we can prob just have it be 4 bc 60 and 80 are both divisible by 4
  val io = IO(new Bundle {
    val din          = Input(SInt(3.W))
    val dinValid     = Input(Bool())
    val dout         = Output(SInt(18.W))
    val desired      = Input(SInt(18.W))
    val doutValid    = Output(Bool())
  })

  // make sections divided with output pipeline registers where the 
  // partition count tells us how many taps we have per group
  val numGroups = tapCount / segmentSize
  val numInputReg = (numGroups * (segmentSize - 1)) + 1
  // maybe want too add one more register at the front
  val inputShifters = RegInit(VecInit(Seq.fill(numInputReg)(0.S(3.W))))
  val outputShifters = RegInit(VecInit(Seq.fill(numGroups - 1)(0.S(24.W))))
  // Delay line for weight claculation for the input 
  val numInputTrackingRegs = (numGroups * (segmentSize - 1) + 2)
  val inputWeightShifters = RegInit(VecInit(Seq.fill(numInputTrackingRegs)(0.S(18.W))))

  // each group is a fir filter so we can use a simple fir module
  val segments = Seq.fill(numGroups)(Module(new FIRSegment(segmentSize)))

  // the bottom x(n) shift registers in the diagram
  when(io.dinValid) {
    for (i <- numInputTrackingRegs - 1 to 1 by -1) {
      inputWeightShifters(i) := inputWeightShifters(i - 1)
    }
    inputTrackingShifters(0) := io.din
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

  when(io.dinValid) {
    for (i <- 0 until numGroups) {
      val slice = inputShifters.slice((i * (segmentSize - 1)), (i * (segmentSize - 1)) + (segmentSize - 1))
      segments(i).io.inputs := VecInit(slice)

      // set input delay vector for weight calc
      val slice = inputWeightShifters.slice((i * (segmentSize - 1)) + 1, (i * (segmentSize - 1)) + (segmentSize + 1))
      segments(i).io.weightCalcIns := VecInit(slice)

      // set error
      segments(i).io.error := error

      // set valid 
      segments(i).io.valid := dinValid
    }
  }

  val firOutput := segments(0).io.dout
  val error = io.desired - firOutput

  io.dataOut := firOutput
}
