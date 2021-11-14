
import java.io.{BufferedInputStream, File, FileInputStream, FileNotFoundException, IOException, InputStream}
import java.util.zip.{ZipFile, ZipInputStream}
import scala.util.{Try, Using}

object xlsxOpener {

  def getNames( filename: String)(f: String => Unit ): Try[Unit] = {
    Using(new ZipInputStream( new BufferedInputStream( new FileInputStream(filename)))) { zin =>
      Iterator.continually({
        zin.getNextEntry
      }).takeWhile( _ != null).map( zipEntry => f( zipEntry.getName) )
    }
  }

  def openAll( filename: String, filterByName: (String) => Boolean)(f: (InputStream) => Unit ): Try[Unit] = {

    Using(new ZipInputStream(new FileInputStream(filename))) { zin =>
      Iterator.continually({
        zin.getNextEntry
      }).takeWhile(_ != null).filter(e => filterByName(e.getName)).tapEach { e =>
        f(zin)
      }.to(Iterable)
    }
  }

  //  @throws[FileNotFoundException]
  //  @throws[IOException]
  def openOne( fileName: String, nameToOpen: String)(f: (InputStream) => Unit ): Try[Unit] = {

    Using( new ZipFile(fileName)) {
      zin =>
        val entry = zin.getEntry(nameToOpen)
        f(zin.getInputStream(entry))
    }
  }

}