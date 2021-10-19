import SAXParser.parse

import java.io.{FileInputStream, FileNotFoundException, IOException}
import java.util.zip.ZipInputStream

object zipOpener {

  def open( fname: String ):Either[Throwable, Unit] = {
    val fin = new FileInputStream(fname)
    val zin = new ZipInputStream(fin)

    try {
      Iterator.continually({
        zin.getNextEntry
      }).takeWhile( _ != null ).filter{ entry =>
        entry.getName.contains("worksheet") && entry.getName.endsWith("xml")
      }.tapEach { e =>
        parse(zin)
      }.to(Iterable)

      Right()
    } catch {
      case e: FileNotFoundException =>
        Left(e)
      case e: IOException =>
        Left(e)
    } finally {
      zin.close()
      fin.close()
    }
  }

}
