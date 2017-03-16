import java.util.UUID

val ids = Stream.continually(UUID.randomUUID()).take(5)
ids.sorted.foreach(printUUIDInfo)

def printUUIDInfo(uuid: UUID) = {
  def withPadding(s: String): String =
    s"%64s".format(s).replace(' ', '0')

  def withSign(in: String): String =
    ((if (in.head == '1') '0' else '1') + in.tail).toString

  val stringRep = uuid.toString
  val msb = withSign(withPadding(uuid.getMostSignificantBits.toBinaryString))

  println(s"$stringRep (msb: '$msb')")
}