
import java.io.{FileInputStream, FileNotFoundException, IOException, InputStream}
import java.util.zip.ZipInputStream

object zipParser {

  def parser( fname: String, f: (InputStream) => Unit ):Either[Throwable, Unit] = {
    val fin = new FileInputStream(fname)
    val zin = new ZipInputStream(fin)

    try {
      Iterator.continually({
        zin.getNextEntry
      }).takeWhile( _ != null ).filter{ entry =>
        entry.getName.contains("worksheet") && entry.getName.endsWith("xml")
      }.tapEach { e =>
        f(zin)
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
