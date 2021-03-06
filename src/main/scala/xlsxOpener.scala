
import java.io.{BufferedInputStream, File, FileInputStream, FileNotFoundException, IOException, InputStream}
import java.util.zip.{ZipFile, ZipInputStream}
import scala.util.{Try, Using, Failure}
import scala.util.control.NonFatal

object xlsxOpener {

  def getNames( file: File)(f: String => Unit ): Try[Unit] = {
    Using(new ZipInputStream( new BufferedInputStream( new FileInputStream(file)))) { zin =>
      Iterator.continually({
        zin.getNextEntry
      }).takeWhile( _ != null).foreach( zipEntry => f( zipEntry.getName) )
    }.recoverWith {
      case NonFatal(e) =>
        System.err.println(e.getMessage)
        Failure(e)
    }
  }

//  def openAll( filename: String, filterByName: (String) => Boolean)(f: (InputStream) => Unit ): Try[Unit] = {
//
//    Using(new ZipInputStream(new FileInputStream(filename))) { zin =>
//      Iterator.continually({
//        zin.getNextEntry
//      }).takeWhile(_ != null).filter(e => filterByName(e.getName)).tapEach { e =>
//        f(zin)
//      }.foreach( _ => ())
//    }.recoverWith {
//      case e: Exception =>
//        System.err.println(e.getMessage)
//        Failure(e)
//    }
//  }

  //  @throws[FileNotFoundException]
  //  @throws[IOException]
  def openOne( fileName: String, nameToOpen: String)(f: (InputStream) => Unit ): Try[Unit] = {

    Using( new ZipFile(fileName)) {
      zin =>
        val entry = zin.getEntry(nameToOpen)
        f(zin.getInputStream(entry))
    }.recoverWith {
      case NonFatal(e) =>
        System.err.println(e)
        Failure(e)
    }
  }

}