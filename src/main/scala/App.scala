import Helper.convertToFileURL
import SAXParser.Handlers.SberDealsXLSXParserHandler._
import models.Deal

import java.io.{BufferedOutputStream, FileNotFoundException, FileOutputStream, PrintStream}
import java.lang.System.console
import scala.util.{Failure, Try, Using}

case class UnknownCommandException( message: String) extends Exception
case class UnknownException(message: String) extends Exception
case class PrintHelp( message: String =
                      s"""blablabla
                         |""".stripMargin) extends Exception

case class Task( command: Option[String], file: Option[String], output: Option[String])

object Task{
  def apply(): Task = Task(None, None, None)
}

object App {

  def splitArgsIntoTuple(s: String):(Option[String],Option[String]) = {
    val res = s.split("=").toList
    assert(res.length <= 2 && res.nonEmpty)
    (res.headOption, res.tail.headOption)
  }

  def argsIntoTask(taskE: Either[Exception,Task], arg: (Option[String],Option[String])): Either[Exception,Task] = {
    taskE flatMap { task =>
      arg match {
        case (Some("--cmd"), opt1@Some(_)) => Right(task.copy(command = opt1))
        case (Some("--file"), opt2:Some[String]) => Right(task.copy(file = opt2))
        case (Some("--output"), opt3@Some(_)) => Right(task.copy(output = opt3))
        case (Some("--help"), _) => Left( PrintHelp())
        case _ => Left( PrintHelp())
      }
    }
  }

  def main(args: Array[String]): Unit = {

    val taskE: Either[Exception, Task] =
      args.map(splitArgsIntoTuple).foldLeft[Either[Exception, Task]](Right(Task()))(argsIntoTask)

    for {
      task <- taskE
      filename <- task.file.toRight(new FileNotFoundException("filename must be specified"))
    } yield Using(

      task.output match {
        case Some(filename) => new PrintStream(new BufferedOutputStream(new FileOutputStream(filename)))
        case None => System.out

      }) { output: PrintStream =>

      task.command match {
        case Some("echo") => xlsxOpener.getNames(filename) {
          output.println
        }
        case Some("echopage") => xlsxOpener.openOne(filename, dataXmlFileName) {
          xmlFile => ()
        }
        case Some("sber_deals") => xlsxOpener.openOne(filename, dataXmlFileName) {
          xmlFile =>


            val dealsHandler = new DealsHandler {
              def addOne(deal: Deal): Unit = output.println(deal.toString)
            }

            val handler = new Handler(dealsHandler)
            SAXParser.parse(xmlFile, handler)
        }
        case _ => Failure( UnknownCommandException(task.command.getOrElse("command must be specified, try --help instead")))
      }

    }.flatten.toEither

  }
}

