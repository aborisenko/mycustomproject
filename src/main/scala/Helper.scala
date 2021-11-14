import java.io.File

object Helper {

  def convertToFileURL(filename:String):String = {
    var path = new File(filename).getAbsolutePath
    if (File.separatorChar != '/') {
      path = path.replace(File.separatorChar, '/');
    }

    //    if (!path.startsWith("/")) {
    //      path = "/" + path;
    //    }

    path
  }

}
