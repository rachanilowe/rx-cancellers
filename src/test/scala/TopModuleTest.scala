package cancellers

import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import scala.collection.mutable.ArrayBuffer
import scala.math._
import scala.io.Source
import scala.util.Random

import cancellers.CancellersTopModule

class TopModuleBlock(echoTapCount: Int, nextTapCount: Int, segSizeEcho: Int, segSizeNext: Int, val echoGammaFactor: Int, val echoMuFactor: Int, val nextGammaFactor: Int, val nextMuFactor: Int) extends Module {
    val io = IO(new Bundle {
      val tx0 = Input(SInt(3.W)) // echo
      val tx1 = Input(SInt(3.W)) // next 1
      val tx2 = Input(SInt(3.W)) // next 2
      val tx3 = Input(SInt(3.W)) // next3
      val txValid = Input(Bool())

      val desired   = Input(SInt(8.W)) // RX signal
      val desiredCancelled = Output(SInt(8.W)) // Cancelled RX signal

    })
    val dut = Module(new CancellersTopModule(echoTapCount, nextTapCount, segSizeEcho, segSizeNext, echoGammaFactor, echoMuFactor, nextGammaFactor, nextMuFactor))
    dut.io.tx0 := io.tx0
    dut.io.tx1 := io.tx1
    dut.io.tx2 := io.tx2
    dut.io.tx3 := io.tx3

    dut.io.txValid := io.txValid
    dut.io.desired := io.desired

    io.desiredCancelled := dut.io.desiredCancelled
}            

class TopModuleTest extends AnyFreeSpec with ChiselScalatestTester {

  "Simulated Incoming Rx Data" in {
    test(
  new TopModuleBlock(
    echoTapCount = 18,  // Number of taps for echo canceller
    nextTapCount = 4,  // Number of taps for NEXT cancellers
    segSizeEcho = 9,    // Number of segments for hybrid FIR
    segSizeNext = 2,
    echoGammaFactor = 0,
    echoMuFactor = 0,
    nextGammaFactor = 0, 
    nextMuFactor = 0
  )
).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
  val steps = 1000
  
  // Signal containers
  val originalData = ArrayBuffer(0, -4, -3, 2, -3, 1, 2, -1, 1, 2, -4, -4, -1, -2, 2, -1, 0, -2, -1, 1, 3, 1, -4, 3, -2, 2, -3, -1, -2, -2, 2, 1, 1, 0, -4, 3, 3, -4, 1, -3, -3, -4, -4, 0, 2, -1, 3, -4, 0, 0, 1, -4, -2, 2, -2, 3, -4, 0, -2, 1, -2, -3, 0, -4, -2, 0, -2, 0, -2, -3, 2, -4, 0, -4, 0, 0, 3, -4, 0, 1, -3, 0, -2, -2, 3, 0, 0, 3, -2, -3, -3, -1, 2, 1, -2, -2, 3, 1, 0, -4, -1, 1, -3, 0, -4, 1, -1, 3, 3, 1, -3, -1, -3, 2, 1, -4, -1, 1, -3, -3, -1, -3, 1, -4, 2, 1, -2, 2, -4, -3, 0, -3, 2, 0, 2, 1, 1, -4, -3, -2, -4, 3, -4, 1, 0, 0, -1, 0, -3, 0, -2, -3, 2, 2, -2, -2, -2, -2, 0, -1, -3, 0, 3, -4, 3, 0, -4, -4, 3, 3, 3, 0, 2, -3, 1, -2, 1, -4, 1, -4, 1, 3, -2, -2, 2, -1, 1, -3, 2, 0, -1, -1, -1, -4, -2, 3, -3, -2, 2, -3, 2, -2, -3, 3, -4, 1, -1, 2, -4, -2, 0, 2, -4, 2, -1, 0, -2, -3, 2, 0, 2, -1, -2, 0, 0, 0, 1, -2, -3, -4, -1, 2, 3, 0, 1, -1, -2, -2, 1, -3, -4, 0, 1, 0, 2, -1, -1, 1, -3, 0, 1, -3, 1, -4, 3, -3, -2, 1, -4, 1, 0, -3, 0, -3, 0, -2, 1, 0, 1, 2, -3, 2, 2, 1, -1, -3, 2, -1, -2, 2, 3, 3, 3, 3, 3, 1, 3, 0, -2, 2, 0, 1, -2, -1, -3, -2, -1, -2, 2, -1, -1, -3, -2, 1, 1, 2, -3, -4, 2, -2, -3, -3, -3, -3, 2, 3, -1, -1, 1, -3, 1, -3, 0, 3, -2, -1, 3, -4, 0, 0, -2, 1, -1, 2, -3, -1, 0, 2, -3, -4, -1, -1, 0, 1, -2, 0, 2, 1, -2, -1, 3, -1, -1, 3, 0, 3, 0, -3, -2, 3, -4, -1, 0, -4, 1, 0, 2, -2, -2, -4, -2, -1, -4, -1, -4, 1, -4, 3, -1, 0, -1, -3, 3, -2, -3, -1, 2, 3, 3, -2, 1, -4, 2, 1, 2, 1, 1, 3, -2, -3, 0, 0, -3, -3, 0, 2, -2, 2, 0, -2, 1, -3, -3, 1, 2, 2, -1, -3, -2, 1, 2, -3, 2, 1, 2, 3, 2, -1, -2, -2, -4, 2, 0, 0, 1, -3, 2, 0, -1, 3, -3, 1, -3, 2, 2, -4, 0, 1, 3, 3, -4, -4, -3, 1, 3, -2, -1, -1, -1, -3, -2, 2, -3, 2, 0, 3, -1, 3, 2, -4, -3, -2, 2, 2, -2, -4, -1, -1, 3, 0, -2, -2, -4, 1, 3, -2, 3, -3, 2, -4, 0, -3, 2, -3, -3, 3, 2, 1, 1, -3, -4, -3, 3, -2, -2, -2, -1, -4, 3, 3, -2, -2, 1, -2, 3, 3, 2, 2, -3, -4, 2, 0, -3, 1, -2, -1, 1, 2, -3, 0, -3, -3, 1, 3, 0, 3, -1, -4, -3, 0, 0, 1, -1, 2, -1, 0, 1, -2, -3, 2, -2, -3, 3, -4, -3, 3, 1, 2, -1, -1, 1, -3, 0, 3, 3, 0, -2, -4, -1, 3, -1, 2, 3, 0, -4, -1, 1, 2, 0, 0, 0, 0, -2, 0, -1, -4, -2, 1, -4, 3, -4, 1, 0, 1, 0, -3, -3, -3, 3, -1, -1, -2, 3, -2, -1, 1, -2, -1, 2, -2, -3, 0, 2, -2, -1, -2, 0, 1, 2, 3, 3, 0, 2, 1, 3, 1, 1, -3, -2, 3, 3, 1, 1, -1, 3, -4, 2, 3, -2, 2, 1, 2, -1, 2, 3, -3, -1, 1, 1, -3, -1, -4, -3, 3, 1, -1, 0, -4, -2, 0, 2, -2, 1, 1, -3, -4, 0, 0, 1, -4, -3, -3, 3, 0, 0, -2, -3, -4, -2, -3, 1, -2, 1, -1, -3, 2, -3, 0, 1, 3, -3, 1, -3, 2, -2, -1, 1, -3, -4, 2, -2, 1, 2, 1, 3, 1, -2, -1, -1, -4, 0, 0, -3, 1, -4, 3, -4, 1, 2, 1, 2, 0, -4, -2, 2, 3, 2, 0, -1, 0, 2, -1, -2, 0, 0, -2, 1, -1, -2, 0, -2, -3, -2, 3, -2, 3, -3, 2, 0, -1, 0, 2, 2, 1, 1, 0, -3, 2, -4, 2, -4, 1, -2, -4, -1, -2, -4, -4, -4, 3, -2, 0, 3, 3, 2, -4, 0, -4, 0, -4, -1, -3, 0, -1, 2, -3, 0, 2, 3, -1, 1, -2, 1, 1, -1, -4, 3, 1, 2, 1, -3)
  val input0   = ArrayBuffer(-4, 2, 3, -4, -1, 3, 3, 0, -2, -4, 3, 1, -3, -1, 1, -4, 2, -2, 1, 2, 2, 0, 0, 3, -2, 0, 1, -2, 3, -1, 3, 2, -4, -4, -1, -2, -1, 0, 1, -1, 1, 3, 2, 3, -3, 1, -2, -1, 2, -1, -4, -4, 3, 0, -3, -3, -2, 2, 1, -2, -3, -3, 3, -2, -1, 1, 2, 2, 3, -1, 0, -2, -2, -3, 0, 3, 0, -2, -2, -2, 3, 1, 1, 2, -1, -4, -4, 1, 1, -1, -3, 0, 3, 2, -2, 2, 3, -1, 0, 3, 3, -3, -2, 3, 3, 2, -2, 2, 1, 2, 3, -2, 3, -3, 2, -4, -4, 3, -3, -4, 1, -3, -2, -3, 3, 3, -2, 3, 2, 3, 1, 0, 3, 2, 1, -2, -1, 1, -4, -1, 3, -4, -3, 0, 2, -1, 0, -3, -4, -3, 0, -2, 3, -2, 1, 2, -1, 1, -2, -3, -1, 2, -1, -3, 0, 3, 2, 3, -1, 0, 1, -3, -4, -1, 2, 2, 2, -1, -3, 2, -3, -4, 1, 0, 1, 3, 2, -1, 0, 3, 0, 3, 0, 2, 3, 1, 1, 0, 3, 3, 3, 3, -3, 1, 2, -2, 2, -3, 0, -4, 1, 1, -3, 2, 1, 0, -2, -1, -2, 0, -3, -4, -1, -3, 2, -1, 3, -4, 2, -1, 1, -1, 1, -4, -4, -4, 1, 2, 1, -2, -2, -3, 1, -2, -4, 1, -3, 2, 3, 1, -4, -2, 3, 3, 3, 3, -2, 1, 2, 1, 3, 2, -1, -3, 2, -2, -4, -2, -1, 2, 1, 2, -4, -4, -2, 0, -1, -4, -2, -3, 1, -2, 0, -4, 2, 2, -3, 1, -1, 3, -2, -2, 2, 3, 1, -4, -2, -2, 2, -2, 2, -3, 2, -3, -1, -3, 2, 0, 0, -3, 1, 3, -1, 0, -4, -2, 2, -1, -3, 3, -3, 2, -1, 0, 1, -3, -1, -4, -3, 2, -4, -4, 2, 1, -2, -4, 1, 1, 3, -2, -4, -1, -2, 0, -1, -1, -1, 2, 2, -4, -2, -4, -3, -4, -4, -2, -3, 2, -3, -4, 2, -2, 2, -4, -2, -4, 2, 3, 3, 1, -2, -4, 2, 3, 3, 1, -3, -2, 0, 0, -2, -3, 3, -4, -2, 0, -3, 0, -2, -3, -3, 0, 1, -4, 0, 2, -2, -4, -4, -4, -1, -4, -1, -2, 0, 2, -2, -4, 1, -3, 3, 0, 3, 3, -2, -2, -4, 2, -2, 3, -2, 1, 3, -2, -4, 3, 2, -2, 3, 0, -1, 3, 1, 2, -2, 1, 1, -2, -4, 3, -4, 1, -4, 2, -3, -4, -2, -4, -4, 1, 2, -1, 3, -3, -3, -3, 1, 3, 2, 0, -2, -2, 2, -1, -1, -2, 2, -1, 3, 2, -1, -3, -2, -2, -3, 2, 3, -3, -1, -4, 3, -2, -2, 1, 2, -1, -1, 1, 0, 0, -2, -4, 3, 2, 1, 1, -4, 1, 0, -4, 2, 0, 3, -2, -3, 2, 2, -3, -3, 3, -2, -3, 2, -3, -4, 0, 3, -3, 2, -3, 2, 3, -1, 2, -4, 3, 3, 3, 2, 0, -2, 3, -2, -1, 3, 3, 3, 2, 0, 3, -4, 1, 3, 0, -2, -1, -1, 1, -2, -4, -4, -4, -2, 2, 0, -2, -1, -4, -2, 3, 1, -4, -1, -4, -4, -2, 3, 3, 1, -4, 2, 1, 0, -4, -2, 0, -4, -2, -1, -3, 3, 0, 1, 1, 2, 1, 0, 2, -4, -3, 3, 1, 3, 2, 3, 1, -3, -2, -1, 1, 0, -1, -4, 2, -4, -2, -2, 3, 1, 2, -3, 2, -1, -1, -3, 3, -4, 0, 3, -3, -3, -1, -2, -2, 3, 2, -1, 2, -1, 2, 2, -1, -1, 1, 3, -2, -1, -4, -2, -1, -3, 2, -4, -1, 1, 1, 3, -2, 3, -1, 2, 2, 3, -3, 2, -3, -1, -4, -3, -3, 0, -1, 0, 1, 0, 1, -2, 1, -2, -3, -4, 3, -3, 2, -4, -2, -2, 2, -1, 3, 3, -3, -2, -3, 1, 0, -2, -4, 3, -4, -4, -4, 3, 2, 0, 1, 0, -2, 3, 1, 1, -3, 0, 0, 1, -3, 0, 2, 1, 3, 2, 2, -2, 0, -3, -2, -2, -4, 3, 1, 2, 0, -2, 1, -1, -2, -4, -4, -2, -2, -4, 0, 1, 3, 1, 0, 0, -3, -1, 1, -3, -3, -1, 3, -1, 0, 0, -4, 2, 1, 3, -4, 1, 1, 3, 3, 0, 3, -4, 1, 0, 3, -2, -1, 2, 2, 1, 0, -3, 1, -3, -3, -3, 0, 2, -1, -2, -2, -4, 0, -2, 1, -1, -3, -2, 1, -4, 1, 3, -1, 3, -4, 3, -2, -4, 0, -2, -3, -2, 3, 0)
  val input1   = ArrayBuffer(-2, -1, 0, -1, -3, -4, -4, 2, 2, -4, -3, -2, 2, 0, -4, -1, 2, 1, 0, 3, 2, -4, -1, -2, -1, -3, -4, -2, 0, 1, 0, 1, -4, -2, -1, 2, 0, 3, 2, 2, 3, 1, 2, 1, -1, 1, 2, -4, -4, 3, 3, -2, -4, 3, -1, -4, 3, -2, -3, -1, -3, -2, -1, 1, -2, -4, 3, -1, -2, 3, -3, -1, 3, -3, -4, -2, -2, -2, -1, -3, 3, -4, -3, -3, -3, -4, -3, 3, 3, -3, -1, -3, -1, -3, 1, -2, 0, 1, -1, -1, 2, -1, 3, 3, 0, 3, -4, 1, -4, 0, 0, -3, 2, 2, 0, 2, -3, 0, -2, 1, -2, -3, -3, 2, 0, -2, -1, -4, 1, 0, 2, 2, -3, 0, 1, 2, -1, -4, 0, -1, 0, -1, -4, -1, -2, -1, -4, -3, 0, 0, 3, -3, 1, -3, 2, 1, -3, 2, 2, 2, 2, -4, 1, 1, -4, -3, 2, -4, 2, -3, 3, -2, 2, -3, 0, -3, -2, 0, 1, 0, -1, -3, 2, 2, 1, -1, 2, -3, 2, -3, -3, 3, 3, 1, 1, -2, -4, -4, 3, 0, 2, 0, -2, -2, 2, 0, -4, 1, 3, 1, 3, -4, 0, -2, 0, -2, -1, -2, -3, -1, -3, 3, 1, 1, 3, 1, -3, 1, -2, -3, -4, 0, -3, 1, 0, 1, -2, 3, -3, 0, 0, 3, 2, -3, -3, 1, 3, 3, 1, 2, -2, 1, -1, -4, -1, 2, -4, -3, -4, -1, 1, 3, 2, -4, 3, 2, -4, -3, 0, 2, -4, -1, 2, 2, -2, -4, -1, 1, 2, 2, 3, -2, -3, -3, -3, -4, -1, -4, -4, 2, 1, 0, 3, 1, -3, -2, 1, 3, 3, 3, 2, 2, 3, -1, 3, 0, -2, -2, -2, -4, 1, -1, -4, -4, -2, -3, 2, -3, 3, -2, -4, 3, -4, -3, -4, -2, -1, 2, 1, -3, 2, -3, -1, 3, 2, 1, 3, 2, 1, 3, 1, 1, 1, -3, -1, -2, 0, -2, -3, 0, 1, -3, 2, -2, 3, 2, -3, 2, 3, -1, -2, -4, -2, -1, 2, 2, 2, 0, -2, 3, 0, 3, 2, -4, 2, -4, 2, -3, -3, -4, 2, 2, -3, -2, 2, -4, 2, -2, 3, -2, 1, 3, -3, 3, -4, 2, 1, 3, 3, 3, -4, -1, -3, 3, -2, -1, 3, 3, -2, 0, 0, 3, 0, 3, 0, -2, 0, -4, 2, 1, -3, 0, 2, -3, 3, -3, -1, 1, -1, -3, 3, -3, 1, -1, 0, -2, 0, -3, -1, 1, 0, 2, -1, 0, -4, 2, -1, 2, -4, -4, -2, 1, 0, -4, -3, -4, 2, 2, -4, -4, -4, -3, -3, 1, 0, 3, -3, 0, 2, 2, -3, -2, -2, -2, -2, -2, 2, -3, 3, 1, 0, -3, 2, -1, 1, 1, -3, -1, -3, 1, 3, -4, 3, -1, -1, 2, 1, -1, 2, -1, 2, 3, -4, 3, 3, 0, 0, -3, 0, 2, 2, 3, 3, 3, 2, -4, -1, 0, -2, -1, 2, -3, 2, 3, 1, 1, 3, -4, 0, -3, 2, -3, 0, -1, 3, -1, 1, -2, -2, -4, 0, -4, -1, 3, 1, 1, 0, 2, -3, -4, 1, 0, 3, 0, 1, 0, 2, -4, 2, 1, 3, -4, -2, 0, 0, 2, -4, 0, 0, -4, 3, -3, 2, 1, 2, 0, 1, -3, -4, 2, 3, 3, -1, -2, -2, -2, 3, -2, 1, 3, -1, 1, -3, -3, -4, 3, -1, 0, 2, 3, -3, -2, -2, 2, 3, 0, 1, -2, 1, 0, -3, -2, 2, 0, 0, -3, 1, -1, -2, 3, -4, 3, -4, -2, 1, 0, -4, 2, 3, -2, 2, 0, 1, -2, 0, -4, -3, -4, 1, -2, 0, 1, -4, -3, 2, 1, 2, 0, -2, -2, 1, 1, -1, 0, 3, -3, -2, 1, 0, 3, 2, 0, -4, 2, 0, 2, 1, -3, 2, 1, -2, -4, -1, -1, 1, 1, 2, 1, 3, -3, -3, -3, 1, -1, -4, -2, -2, 1, 2, 0, 2, 0, -3, 3, -2, 1, -3, 1, 1, 2, 0, 1, -1, -4, 2, -2, -1, -3, 1, 3, 2, -4, 3, 0, 2, 1, 1, 0, -1, 2, -4, -3, -1, -4, -2, -2, 3, -3, -3, 0, 3, 3, -2, -4, -2, -4, -1, -1, -4, 2, 1, -4, -3, 0, -1, -3, 1, -2, -1, 3, 2, -4, -3, -4, 0, 1, -4, -3, 1, 2, 3, -1, 0, 3, 3, -3, -2, 0, -4, -4, -4, -4, 1, 3, 3, -1, -3, 2, -2, -2, 0, 2, -3, 3, -3, 3, 2, -4, -2, 2, -2, 3, -1, 0, 3, 0, -1, 0, 1, -1, 1)
  val input2   = ArrayBuffer(3, 1, -1, -4, 2, 3, -2, 1, 1, 0, -2, -2, 3, 1, -3, 1, -2, 0, 0, -2, -3, 2, 3, 0, 3, 2, 1, -1, 2, -3, -4, -1, 0, -3, 0, 0, -3, -1, -3, -1, -2, 2, -3, 1, -2, 1, -1, -1, 1, 2, 1, -1, -2, 0, -4, 0, -3, 1, -4, 3, -3, -3, 1, 2, -1, 0, 0, -2, -4, 1, 2, 0, 3, -3, -2, 3, 2, 3, 1, -1, 3, -2, 2, -4, -2, -2, -1, 0, 0, -4, 1, 2, -4, -4, -3, -1, 1, 2, -2, -1, -4, -4, -2, -4, 2, 1, -4, 1, 3, 1, 1, -3, -2, 0, 2, 2, 3, -2, -2, -3, 0, 3, -1, -2, 2, 2, -1, -4, -3, -2, 0, -3, -1, -2, 3, -4, 1, 3, -4, 3, 2, -1, -4, 0, -1, 0, 3, 1, 3, -1, 2, -1, -3, -1, -2, 3, -2, 2, -4, -4, -3, 2, 0, -3, 0, -2, 1, -1, -2, -2, 2, 3, 3, -1, 2, 1, -4, -2, 0, -1, 1, 0, -2, -2, -3, -3, 2, 2, 1, -4, 2, -3, 3, -4, 1, 2, 2, -2, 2, -4, 0, -1, -4, -1, 2, -4, -3, 3, -1, -1, 1, 1, 1, -1, 1, -3, -3, 3, -4, 0, 0, 1, -1, 2, 1, -4, -3, -2, -2, 1, -2, 1, -2, 2, -1, -4, -1, 0, -4, -3, 3, -3, 3, -4, -3, 3, 2, -2, -2, -4, -1, -2, -2, -1, 1, -3, 2, 0, 0, 1, -2, 3, 1, -3, 2, 0, -1, 2, -1, -2, 1, 0, -2, 2, -2, -2, 2, -2, -3, -4, -4, 3, 3, 3, 1, 1, 1, 1, 1, -4, 0, 1, 2, 2, -1, 3, -1, -4, -1, 0, -1, 3, -3, -3, 2, 1, 1, 2, -4, -2, -4, 2, -2, -3, 1, -3, -2, -1, -3, -1, -3, -4, 3, -4, 3, 3, 3, 3, 3, 0, 3, -4, -2, -3, -1, 0, -4, -4, 2, -3, -1, -4, -1, -3, 3, -2, -4, 3, 0, 0, 3, 0, 0, 2, 0, -1, -2, -3, 2, 0, 0, -3, -3, 3, -4, -4, -1, 2, -4, -1, 1, -4, -3, -4, 0, -2, 1, -1, 0, 0, -2, -1, -2, -2, 1, 1, -2, -4, 0, 3, 3, -2, 2, -3, -2, -4, -3, 0, 3, -1, 0, 2, -3, 2, -3, -3, 0, 0, 0, 0, 3, 2, 3, 0, 2, -2, 3, -2, 2, -1, 2, 3, 0, 2, -1, 2, 1, 3, 3, -4, 3, 2, -1, 1, -1, -1, 2, 0, 0, 0, -2, 3, -1, 2, 1, 1, -3, -3, -1, 3, -2, 2, -4, 3, -1, -1, 1, -2, -2, -1, -2, -4, -2, 3, -3, 1, -1, 0, 3, 3, 0, 0, 0, -3, -1, 0, 3, 2, -4, -3, 2, -2, 2, 1, -4, 1, -2, -3, 1, 1, -4, -2, 2, 0, -1, 0, -2, -1, 1, -3, -3, -1, 3, -4, 2, 1, 0, 3, -1, 3, 2, 3, 2, 2, -1, -1, -1, -2, -1, 2, -1, 2, 2, -1, 3, -2, 1, -3, 1, 1, -3, -4, -2, 3, -3, -4, -3, -4, -2, -3, 3, 1, 1, 2, 2, -4, 3, -1, -4, -1, -4, -3, -1, 1, 2, 2, -2, 3, 2, -4, -3, -3, 0, 1, -2, -2, 2, -1, 3, 0, -4, 2, 0, 1, 0, -1, 1, -3, -2, 3, 1, 2, 0, 2, 0, 1, 2, 2, -4, 0, 3, 2, -1, -3, -2, -3, -2, -3, 3, -3, -4, -2, 3, 0, 0, -2, -2, 1, -3, 2, 3, -2, -3, 2, -3, 2, -3, -3, 2, -4, -3, 1, 2, -2, -1, 0, -2, -2, -4, -4, 2, 2, -4, -4, -4, -1, -2, 1, -1, -3, -1, -3, -2, -4, 0, 1, -3, -2, -1, 2, 1, -3, -1, 1, -3, -4, 3, -2, 2, -1, 0, -2, -2, -1, -2, -1, -4, 1, 1, -1, 0, 2, 3, 0, -1, 1, -4, -3, -2, -1, 3, 1, -2, 0, -2, -1, 2, -2, 2, 1, 0, -3, 3, 3, -2, -3, 0, -1, 1, -1, -3, -3, -2, 0, -4, 0, 2, 1, -3, -4, 2, 3, 1, -2, -4, -4, -2, 2, -1, -3, 3, 2, -2, 0, 0, -4, 3, -4, 3, 0, -4, 1, 0, 1, 0, -3, 1, -2, -2, -3, -1, -1, 0, 1, 1, -4, 0, -3, 0, -4, -1, 0, -1, -3, -1, -1, 3, 2, -1, 2, 1, 3, -1, 0, -3, -2, -2, -1, -3, 3, 0, 0, -1, 1, -3, 3, -1, 0, -1, 0, -4, -4, -1, -2, 3, 2, 1, -3, 2, 0, 2, 0, -4, 0, 0, 0, 0, -2, -2, 3, 3)
  val input3   = ArrayBuffer(0, -3, -4, -4, -4, -1, 3, 0, -4, 2, 0, 1, 1, -4, 3, -4, -4, 3, 0, 1, 1, 3, 2, 2, 2, 1, 1, 3, -2, -1, 2, -3, 2, -1, 0, -4, -4, 2, -4, -4, 2, -4, -1, -1, -1, -4, -3, 3, 3, 2, 2, 0, -1, 3, -1, 3, 0, -3, -2, -4, -3, 2, 1, 3, 2, 1, 1, 0, -1, -2, 1, 3, -2, 3, 3, 2, -4, 2, 2, -4, 1, 2, 0, 1, 0, 0, 1, 2, 2, -4, 2, 0, -3, -1, 1, -1, -2, 3, -3, 3, -3, 3, -4, -2, 0, -3, -4, -2, 1, 0, 3, -4, -3, 2, 1, -3, -2, 0, 3, -3, 1, 0, -3, -2, 0, 2, -1, -1, 3, -3, -1, -3, 0, -2, 0, -1, -1, 1, -4, 3, -3, 1, 2, 3, 3, -2, 3, -4, 0, 3, 0, 1, -1, 1, -1, -1, 2, 2, -2, 1, -2, -1, -3, 1, -3, 1, 0, 3, -1, -1, -2, -3, -3, -2, 1, 3, 2, -1, 0, 2, 3, 3, 1, 0, 2, -1, 3, -2, -1, 1, 2, 0, -4, 0, -3, 2, -3, -1, -4, -2, 1, -3, -4, -3, 1, 2, -1, -2, 0, 1, 2, 1, -1, -4, -4, -1, 0, 0, 2, 2, 1, -4, 1, 2, -3, 3, 0, 1, 1, 2, -3, 2, -2, -1, -4, 3, 0, -1, 2, -1, 3, 3, -3, 0, 3, 2, -1, 0, -3, 0, 0, 0, 0, 3, 2, -3, -4, 1, 3, -2, 0, 0, 0, 3, 0, -3, 3, 0, 3, -1, -4, -3, -4, -1, 3, 0, -3, 1, 2, -4, -1, -3, 1, -1, -3, -2, 0, -4, -1, -1, 0, 2, 3, 2, -3, 2, 0, -1, -1, -3, 0, 1, 0, -1, 0, 3, 3, -1, -3, -4, 0, -2, -2, -4, -2, 3, 3, 2, 3, -4, 3, 0, -1, -1, 2, 2, -1, -2, -1, -3, -1, 1, -4, -1, 1, 1, -2, 0, 1, -1, -2, 0, 3, 2, -4, 0, 3, -4, 0, -2, 2, 0, -2, 2, 1, -3, 3, 1, -4, -1, -4, 2, 2, 2, -2, -2, 1, -4, -2, 1, 0, 0, 2, 0, -4, -1, -2, -4, 1, 0, 1, 0, -4, 3, 3, -1, -3, -4, -4, -3, -3, 3, 1, -1, -1, -2, -4, -1, 2, -3, 2, 0, -4, 2, -1, -2, 2, -4, 3, 1, -1, -3, -3, -4, -4, 0, 0, 2, 2, -4, -4, -4, 1, 2, -3, 3, -4, 0, -2, -2, -2, 0, -2, -2, 3, -3, -3, 1, -3, -1, -2, 0, 3, -2, 3, -2, 3, -4, 0, -3, 0, -3, -4, -1, 3, -4, -1, 1, -4, 0, -3, -2, -4, -3, -2, -3, -1, -1, 0, 0, -2, 3, -2, -3, 2, -3, 3, -2, 3, -4, 1, 2, -2, 3, 1, -1, -2, 1, -3, 1, -4, 0, 1, 0, -1, -2, 0, 1, 3, -4, -3, 0, -4, -3, -2, 0, -4, -4, 1, 0, -4, -3, 2, 3, 2, -1, 2, -1, -3, -4, 3, 1, -4, 1, -3, 0, 2, -1, -1, 2, -2, -3, -3, -1, -3, -3, -3, 0, -2, 1, 2, -2, 3, 1, 2, 1, -1, 1, -1, 2, -4, -1, 2, -2, 2, -4, -4, 3, -1, 0, 1, 2, -4, -4, 0, -4, -2, -4, 0, 1, 3, 1, 2, 0, -3, 2, 1, -3, 3, 3, -2, -1, 1, 3, 3, -3, -3, -1, -4, 1, -1, 3, -4, -2, 1, 3, 0, -4, 3, 0, 3, -4, 0, 1, 0, -3, 3, 0, 3, 0, 0, 2, 3, 2, 3, -2, 1, 1, -2, -2, -1, 1, -3, -4, 0, 2, 1, 1, 2, -2, 0, 2, 3, 0, 0, 3, -2, -1, 3, -4, -3, 3, 0, 0, -1, 0, -2, 1, -1, 2, 3, -1, 0, -2, 0, 0, 1, -2, -2, 3, 2, 1, 3, 2, 1, 2, 0, 1, 1, 1, -1, 0, 1, 1, 0, 0, -3, 2, 3, 1, 2, 1, 0, -3, 1, 1, -3, -3, -3, 1, -3, -4, 3, 0, 1, -1, -1, 2, 3, 0, 0, 1, 2, -1, 1, 0, 1, -4, 3, -4, -2, 1, -2, 1, 3, -4, -2, 2, -1, -3, 3, 1, -1, -4, -3, -2, -4, -3, 1, 2, 2, -4, 3, 0, 0, -2, 3, -1, -4, 1, -2, 1, -2, 2, -4, -2, -4, -2, 1, -2, -4, -2, 1, 0, -1, 0, -3, -1, 3, 3, 0, 0, -4, 2, -4, 0, -1, -3, 0, 1, 2, -1, 1, -3, 2, 0, -2, 2, -4, -1, -1, 2, -3, 0, -2, -4, -1, 1, 2, 2, -4, -3, 2, 0, -1, 2, -4, 2, 2, 3, -2)
  val desired   = ArrayBuffer(-3, -5, -4, -3, -6, 0, 1, -1, -1, -1, -5, -6, -3, -4, 0, -5, -1, -3, -1, 1, 3, 0, -5, 3, -4, 1, -4, -4, -2, -5, 2, 0, -2, -5, -6, 1, 0, -5, -1, -6, -4, -4, -5, 0, -3, -2, 0, -7, 0, -1, -1, -8, -4, 2, -7, 0, -6, -1, -5, -2, -7, -7, 0, -5, -5, -1, -1, -1, -4, -5, 1, -6, -2, -8, -2, 0, 1, -6, -2, -3, -2, -2, -3, -3, 0, -4, -4, 3, -2, -7, -6, -2, 0, -1, -4, -4, 3, 0, -3, -5, -2, -3, -6, -1, -3, 1, -5, 3, 2, 2, -2, -5, -4, 0, 2, -7, -5, 1, -7, -7, -2, -6, -3, -8, 3, 1, -6, 0, -4, -4, -1, -5, 1, -1, 2, -2, -2, -5, -7, -4, -4, -1, -8, 0, -1, -3, -2, -4, -5, -3, -2, -6, 1, -1, -4, -2, -5, -2, -3, -4, -6, -1, 1, -7, 1, -1, -3, -5, 0, 0, 2, -4, -1, -7, 2, -2, 0, -7, -1, -4, -2, 0, -3, -3, 1, -3, 2, -6, 1, -1, -2, -1, -2, -4, -2, 2, -5, -5, 2, -4, 3, -3, -8, 0, -3, -1, -3, -1, -5, -5, 0, 1, -7, 0, -2, -3, -5, -5, -1, -1, -1, -4, -4, -2, 0, -2, 0, -5, -4, -6, -4, 1, 0, -3, -3, -4, -4, -2, -1, -6, -5, -3, 0, -3, -2, -1, -4, 1, -4, -1, -3, -5, 0, -5, 3, -4, -5, 0, -4, -1, 0, -2, -1, -7, 1, -4, -3, -2, -1, 1, -5, 1, -2, -2, -4, -5, -1, -4, -4, -2, 1, 0, 2, -1, 2, 0, 0, -2, -5, 1, -1, 0, -1, 0, -6, -5, -3, -5, 1, -3, -1, -5, -2, -4, 0, 0, -3, -6, -1, -7, -4, -4, -7, -6, -2, 0, -1, -4, -2, -5, -3, -3, -3, 0, -3, -4, 0, -7, -3, -1, -5, -3, -3, 0, -5, -3, -2, 1, -2, -7, -5, -3, -2, -1, -5, -3, 0, 0, -2, -4, 2, -4, -4, 0, -2, 0, -4, -3, -5, -1, -5, -4, -1, -7, -2, -4, 2, -2, -4, -5, -3, -4, -4, -2, -4, -2, -7, -1, -2, -1, -3, -6, 1, -6, -4, -3, -2, 0, 1, -6, -2, -5, 1, -3, -1, 0, -2, 0, -4, -7, -2, -3, -7, -4, -3, 0, -3, -1, -1, -4, 1, -4, -3, 1, 0, -1, -3, -4, -3, 0, -1, -4, 3, -1, -2, 3, 1, -2, -3, -5, -6, 2, -2, -1, -1, -6, 1, -2, -5, 3, -7, 1, -7, 2, -1, -7, -3, -3, -1, 1, -5, -6, -4, -3, -1, -7, -2, -1, -3, -5, -6, -2, -5, 0, -3, 1, -3, 1, 3, -3, -6, -5, -1, -2, -6, -5, 0, -5, 1, -4, -1, -5, -6, 0, 3, -4, -1, -5, 0, -4, -3, -7, 3, -3, -6, 2, -1, -1, 1, -8, -5, -4, 2, -5, -5, -1, -1, -8, 0, 4, -4, -5, 2, -4, 0, 0, 1, -2, -5, -8, 2, 0, -5, 1, -5, -1, 2, 0, -3, -1, -6, -5, -2, 0, -1, 1, -2, -5, -6, -1, -2, -1, -1, 2, -2, -2, -1, -3, -7, -2, -6, -7, 1, -4, -3, 1, -2, -1, -3, -1, 0, -7, -3, 0, 0, -3, -3, -5, -1, 0, -1, 1, 3, -2, -6, -2, -1, -2, -3, -2, 0, -1, -3, -1, -1, -6, -3, 0, -8, 1, -5, 1, -2, -1, -1, -4, -7, -6, 2, -2, -3, -6, 0, -1, -3, -2, -4, -1, 1, -1, -6, -1, 0, -3, -4, -3, -3, -1, 1, -1, -2, -1, -1, -3, 3, 2, -2, -3, -4, 1, 4, 0, -1, -3, 3, -7, -2, -1, -4, -2, -3, 2, -5, -1, 2, -3, -2, -1, -1, -5, 0, -5, -4, 0, 1, -5, -2, -8, -5, -3, 2, -4, -1, 0, -4, -5, -2, -1, 0, -7, -7, -3, 0, 0, -3, -3, -6, -4, -4, -4, 1, -5, -1, -5, -5, 0, -6, -4, 1, 1, -6, -3, -3, 3, -4, -3, -1, -5, -3, 1, -3, -2, 0, -1, 2, -2, -3, -2, -2, -5, 0, 0, -5, 0, -7, 0, -7, -1, 0, -1, 2, -1, -8, -4, -1, -1, -1, -5, -3, -1, -1, -2, -3, 0, -1, -5, 0, -6, -6, -1, -6, -7, -5, 4, -5, 0, -4, -3, 0, -3, -1, -2, 0, -1, 0, 0, -5, 1, -7, 2, -4, 1, -5, -5, -2, -3, -7, -6, -8, 2, -5, -4, 0, 2, 2, -6, -4, -5, -5, -6, -4, -4, -4, -4, -1, -4, -3, 0, 3, -2, 0, -5, 0, 0, -4, -4, 0, -2, 0, 1, -4)
  for (i <- 0 until steps) {

    // Connect to DUT
    dut.io.tx0.poke(input0(i).S(3.W))
    dut.io.tx1.poke(input1(i).S(3.W))
    dut.io.tx2.poke(input2(i).S(3.W))
    dut.io.tx3.poke(input3(i).S(3.W))
    dut.io.txValid.poke(true.B)
    dut.io.desired.poke(desired(i).S(8.W))
    dut.clock.step()

    // Print diagnostic output
    println(s"$i, ${originalData(i)}, ${desired(i)}, ${dut.io.desiredCancelled.peek().litValue.toInt}")
  }
}

  }

	// "Random Incoming Rx Data" in {
  //   test(
  //     new TopModuleBlock(
  //       echoTapCount = 18,  // Number of taps for echo canceller
  //       nextTapCount = 4,  // Number of taps for NEXT cancellers
  //       segSizeEcho = 9,    // Number of segments for hybrid FIR
  //       segSizeNext = 2,
  //       echoGammaFactor = 0,
  //       echoMuFactor = 0,
  //       nextGammaFactor = 0, 
  //       nextMuFactor = 0
  //       )
  //   ) // 20-bit coefficients, 4 taps
  //   .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
  //     val steps = 500      
  //     // Signal containers
  //     val perfectRemoteTx = scala.collection.mutable.ArrayBuffer[Int]()
  //     val localTxNoise0 = scala.collection.mutable.ArrayBuffer[Int]()
  //     val localTxNoise1 = scala.collection.mutable.ArrayBuffer[Int]()
  //     val localTxNoise2 = scala.collection.mutable.ArrayBuffer[Int]()
  //     val localTxNoise3 = scala.collection.mutable.ArrayBuffer[Int]()
  //     val receivedNoisySignal = scala.collection.mutable.ArrayBuffer[Int]()
  //     val cleanedOutputs = scala.collection.mutable.ArrayBuffer[Int]()

  //     // 1. Generate independent signals
  //     var remoteSignal = Random.between(-4, 3)  // Perfect data we want to recover
  //     var localNoise0 = Random.between(-4, 3)    // Local TX interference
  //     var localNoise1 = Random.between(-4, 3)    // Local TX interference
  //     var localNoise2 = Random.between(-4, 3)    // Local TX interference
  //     var localNoise3 = Random.between(-4, 3)    // Local TX interference

  //     for (i <- 0 until steps) {
  //       // remoteSignal = (remoteSignal + Random.between(-3, 4)).max(-16).min(16)
  //       // localNoise0 = (localNoise0 + Random.between(-2, 3)).max(-16).min(15)
  //       // localNoise1 = (localNoise1 + Random.between(-2, 3)).max(-16).min(15)
  //       // localNoise2 = (localNoise2 + Random.between(-2, 3)).max(-16).min(15)
  //       // localNoise3 = (localNoise3 + Random.between(-2, 3)).max(-16).min(15)

  //       remoteSignal = Random.between(-4, 3)
  //       localNoise0 = Random.between(-4, 3)
  //       localNoise1 = Random.between(-4, 3)
  //       localNoise2 = Random.between(-4, 3)
  //       localNoise3 = Random.between(-4, 3)

  //       // val channelNoise = (Random.nextGaussian() * noiseAmplitude).round.toInt
  //       val receivedSignal = remoteSignal + (localNoise0 >> 1) + (localNoise1 >> 2) + (localNoise2 >> 2) + (localNoise2 >> 2) // + channelNoise  // Scale local noise

  //       // dut.io.din.poke(localNoise.S(6.W))       // Local TX interference we know about
  //       // dut.io.desired.poke(receivedSignal.S(8.W)) // Received signal (remote + noise)
  //       // dut.io.dinValid.poke(true.B)
  //       // dut.clock.step()

  //       dut.io.tx0.poke(localNoise0.S(6.W))
  //       dut.io.tx1.poke(localNoise1.S(6.W))
  //       dut.io.tx2.poke(localNoise2.S(6.W))
  //       dut.io.tx3.poke(localNoise3.S(6.W))
  //       dut.io.txValid.poke(true.B)
  //       dut.io.desired.poke(receivedSignal.S(8.W))
  //       dut.clock.step()

  //       // perfectRemoteTx += remoteSignal
  //       // localTxNoise0 += localNoise0
  //       // localTxNoise1 += localNoise1
  //       // localTxNoise2 += localNoise2
  //       // localTxNoise3 += localNoise3
  //       // receivedNoisySignal += receivedSignal
  //       cleanedOutputs += (dut.io.desiredCancelled.peek().litValue.toInt)

  //       println(s"$i, $remoteSignal, $receivedSignal, ${cleanedOutputs.last}")
  //     }
  //   }
  // }

  "same data" in {
    test(
      new TopModuleBlock(
        echoTapCount = 18,  // Number of taps for echo canceller
        nextTapCount = 4,  // Number of taps for NEXT cancellers
        segSizeEcho = 9,    // Number of segments for hybrid FIR
        segSizeNext = 2,
        echoGammaFactor = 0,
        echoMuFactor = 0,
        nextGammaFactor = 0, 
        nextMuFactor = 0
      )
    ) // 20-bit coefficients, 4 taps
    .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
        val lines = Source.fromFile("random_signals.csv").getLines().drop(1) // skip header
        val data = lines.map(_.split(",").map(_.toInt)).toArray
        val cleanedOutputs = scala.collection.mutable.ArrayBuffer[Int]()

        for (i <- data.indices) {
          val Array(remote, tx0, tx1, tx2, tx3) = data(i)
          val recieved = remote + (tx0>>1) + (tx1 >> 2) + (tx2 >> 2) + (tx3 >> 2)
          // println(s"${tx0>>1} ${tx1>>2}")
          // println(s"${remote} ${recieved}")
          dut.io.tx0.poke(tx0.S(6.W))
          dut.io.tx1.poke(tx1.S(6.W))
          dut.io.tx2.poke(tx2.S(6.W))
          dut.io.tx3.poke(tx3.S(6.W))
          dut.io.txValid.poke(true.B)
          dut.io.desired.poke(recieved.S(8.W))
          dut.clock.step()
          cleanedOutputs += (dut.io.desiredCancelled.peek().litValue.toInt)
          println(s"$i, $remote, $recieved, ${cleanedOutputs.last}")
        }
      }
    }

    "Random Incoming Rx Data" in {
    test(
      new TopModuleBlock(
        echoTapCount = 18,  // Number of taps for echo canceller
        nextTapCount = 4,  // Number of taps for NEXT cancellers
        segSizeEcho = 9,    // Number of segments for hybrid FIR
        segSizeNext = 2,
        echoGammaFactor = 0,
        echoMuFactor = 0,
        nextGammaFactor = 0, 
        nextMuFactor = 0
      )
    ) // 20-bit coefficients, 4 taps
    .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val steps = 500      
      // Signal containers
      val perfectRemoteTx = scala.collection.mutable.ArrayBuffer[Int]()
      val localTxNoise0 = scala.collection.mutable.ArrayBuffer[Int]()
      val localTxNoise1 = scala.collection.mutable.ArrayBuffer[Int]()
      val localTxNoise2 = scala.collection.mutable.ArrayBuffer[Int]()
      val localTxNoise3 = scala.collection.mutable.ArrayBuffer[Int]()
      val receivedNoisySignal = scala.collection.mutable.ArrayBuffer[Int]()
      val cleanedOutputs = scala.collection.mutable.ArrayBuffer[Int]()

      // 1. Generate independent signals
      var remoteSignal = Random.between(-4, 4)  // Perfect data we want to recover
      var localNoise0 = Random.between(-4, 4)    // Local TX interference
      var localNoise1 = Random.between(-4, 4)    // Local TX interference
      var localNoise2 = Random.between(-4, 4)    // Local TX interference
      var localNoise3 = Random.between(-4, 4)    // Local TX interference

      for (i <- 0 until steps) {
        remoteSignal = Random.between(-3, 4)
        localNoise0 = Random.between(-3, 4)
        localNoise1 = Random.between(-3, 4)
        localNoise2 = Random.between(-3, 4)
        localNoise3 = Random.between(-3, 4)

        // val channelNoise = (Random.nextGaussian() * noiseAmplitude).round.toInt
        val receivedSignal = remoteSignal + (localNoise0 >> 2) + (localNoise1 >> 2) + (localNoise2 >> 2) + (localNoise2 >> 2) // + channelNoise  // Scale local noise

        // dut.io.din.poke(localNoise.S(6.W))       // Local TX interference we know about
        // dut.io.desired.poke(receivedSignal.S(8.W)) // Received signal (remote + noise)
        // dut.io.dinValid.poke(true.B)
        // dut.clock.step()

        dut.io.tx0.poke(localNoise0.S(6.W))
        dut.io.tx1.poke(localNoise1.S(6.W))
        dut.io.tx2.poke(localNoise2.S(6.W))
        dut.io.tx3.poke(localNoise3.S(6.W))
        dut.io.txValid.poke(true.B)
        dut.io.desired.poke(receivedSignal.S(8.W))
        dut.clock.step()

        perfectRemoteTx += remoteSignal
        localTxNoise0 += localNoise0
        localTxNoise1 += localNoise1
        localTxNoise2 += localNoise2
        localTxNoise3 += localNoise3
        receivedNoisySignal += receivedSignal
        cleanedOutputs += (dut.io.desiredCancelled.peek().litValue.toInt)

        println(s"$i, $remoteSignal, $receivedSignal, ${cleanedOutputs.last}")
      }
    }
  }
}